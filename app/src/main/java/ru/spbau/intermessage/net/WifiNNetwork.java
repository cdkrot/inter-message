package ru.spbau.intermessage.net;

import ru.spbau.intermessage.core.Messenger;
import ru.spbau.intermessage.store.IStorage;

import java.nio.channels.*;
import java.nio.*;
import java.io.*;
import java.net.*;
import java.util.*;

import ru.spbau.intermessage.util.ByteVector;

public class WifiNNetwork implements NNetwork {
    private Messenger msg;
    private IStorage store;

    private static final int listenPort = 5202;
    private static final int udpPort = 5203;
    private static final byte[] magic = {69, 77, 83, 71}; // EMSG.
    
    private Selector epoll;
    private InetAddress bcast;
    
    private class Helper {
        public Helper(SocketChannel sck, Messenger msg_, IStorage store_, ILogic logic_) {
            token = null;
            
            sock = sck;
            
            off = -6;
            recv = null;
            income = -1;
            magicAccepter = 0;

            outbuf = ByteBuffer.allocate(4096);
            inbuf = ByteBuffer.allocate(4096);
            outbuf.order(ByteOrder.BIG_ENDIAN);
            inbuf.order(ByteOrder.BIG_ENDIAN);

            outbuf.clear();
            inbuf.clear();

            logic = logic_;
        }

        public boolean writing;
        
        public ILogic logic;
        
        public SelectionKey token;
        
        public SocketChannel sock;

        public ByteVector pending;
        
        public int off;

        public ByteVector recv;
        public int income;

        public int magicAccepter = 0;

        public ByteBuffer outbuf, inbuf;
        public int outbuf_pos = 0;
        
        public int getOutput() {
            if (pending.isEmpty())
                return -1;

            if (off < -2) {
                return magic[(off++) + 6];
            }
            
            if (off == -2) {
                off++;
                return pending.peek().size() / 256;
            }

            if (off == -1) {
                off++;
                return pending.peek().size() % 256;
            }

            int res = pending.peek().get(off++);
            if (off == pending.peek().size()) {
                off = -6;

                pending.poll();
                Callback call = callbacks.poll();
                if (call != null)
                    call.completed(true);
            }
            return res;
        }
        public boolean onInput(byte b) {
            System.out.printf("Get %d, recv: %d, income: %d, magicAcc: %d\n", (int)b, (recv == null ? -1 : recv.size()), income, magicAccepter);
            if (magicAccepter == -1) {
                recv.pushBack(b);
                if (recv.size() == income) {
                    InetSocketAddress addr = (InetSocketAddress)sock.socket().getLocalSocketAddress();
                    listener.recieved(addr.getHostName(), false, recv);
                    
                    recv = null;
                    income = -1;
                    magicAccepter = 0;
                }

                return true;
            }
            
            if (magicAccepter < 4) {
                if (magic[magicAccepter] != b)
                    return false;
                magicAccepter += 1;
                return true;
            }

            if (magicAccepter == 4) {
                income = b;
                magicAccepter += 1;
                return true;
            }

            if (magicAccepter == 5) {
                income *= 256;
                income += b;
                magicAccepter = -1;
                recv = new ByteVector();
                return true;
            }
            return false;
        }
    };

    private class UDPHelper {
        public UDPHelper(DatagramChannel chan) {
            sock = chan;
            pending = new LinkedList<ByteVector>();
        }
        
        public DatagramChannel sock;
    }

    private InetAddress getBroadcast() {
        try {
            byte[] bts = new byte[4];
            bts[0] = bts[1] = bts[2] = bts[3] = (byte)255;
            return InetAddress.getByAddress(bts);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
  
    private UDPHelper udphelper;
    private UDPLogic udplogic;
    
    private void doWrite(UDPHelper helper) {
        ByteVector vec = udplogic.bcast();

        if (pending != null) {
            try {
                System.out.println("Trying to send UDP bcast");
                ByteBuffer buf = ByteBuffer.allocate(vec.size() + 6);
                buf.clear();
                for (int i = 0; i != magic.length; ++i)
                    buf.put(magic[i]);
                buf.put((byte)(vec.size() / 256));
                buf.put((byte)(vec.size() % 256));
                
                buf.put(vec.data(), 0, vec.size());

                buf.flip();
                
                int r = helper.sock.send(buf, new InetSocketAddress(bcast, udpPort));
                System.out.printf("Send %d bytes\n", r);

                System.out.println("done");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void doRead(UDPHelper helper) {
        try {
            ByteBuffer buf = ByteBuffer.allocate(8192);
            InetSocketAddress addr = (InetSocketAddress)helper.sock.receive(buf);

            System.out.println("incoming udp");

            for (NetworkInterface netint: Collections.list(NetworkInterface.getNetworkInterfaces())) {
                Enumeration<InetAddress> list = netint.getInetAddresses();
                for (InetAddress self: Collections.list(list))
                    if (self.equals(addr.getAddress()))
                        return;

                // ignoring udp's from ourself.
            }
            
            if (buf.position() >= 6) {
                for (int i = 0; i != magic.length; ++i)
                    if (buf.get(i) != magic[i])
                        return;

                int len = buf.get(4) * 256 + buf.get(5);
                if (len != buf.position() - 6)
                    return;

                ByteVector res = new ByteVector(len);
                for (int i = 0; i != len; ++i)
                    res.set(i, buf.get(6 + i));

                udplogic.recieve(addr.getHostName(), res);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private boolean handle(Helper helper) {
        if (!helper.token.isValid()) {
            helper.logic.disconnect();
            return false;
        }
        
        if (helper.writing && helper.token.isWritable()) {
            try {
                if (helper.outbuf_pos == helper.outbuf.position()) {
                    helper.outbuf_pos = 0;
                    helper.outbuf.clear();

                    int r;
                    
                    while (helper.outbuf.remaining() > 0 && (r = helper.getOutput()) != -1) {
                        helper.outbuf.put((byte)r);
                    }

                    helper.outbuf.flip();
                }
                
                if (helper.outbuf_pos != helper.outbuf.position())
                    helper.outbuf_pos += helper.sock.write(helper.outbuf);
                else
                    helper.writing = false;
                
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            try {
                helper.inbuf.clear();
                int cnt = helper.sock.read(helper.inbuf);
                if (cnt == -1)
                    return false;
                
                for (int pos = 0; pos != helper.inbuf.position(); ++pos)
                    if (!helper.onInput(helper.inbuf.get(pos))) {
                        return false;
                    }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        return true;
    }
            
    public void begin(Messenger msg_, IStorage store_) {
        msg = msg_;
        store = store_;

        try {
            epoll = Selector.open();
            bcast = getBroadcast();
            
            System.out.println(bcast.getHostName());
            
            ServerSocketChannel sock = ServerSocketChannel.open();
            sock.configureBlocking(false);
            sock.socket().bind(new InetSocketAddress(listenPort));
            sock.register(epoll, sock.validOps(), sock);
            
            DatagramChannel udp = DatagramChannel.open();
            udp.configureBlocking(false);
            udp.socket().bind(new InetSocketAddress(udpPort));
            udp.socket().setBroadcast(true);
            udphelper = new UDPHelper(udp);
            udplogic = new UDPLogic(msg, store);
            udp.register(epoll, udp.validOps(), udphelper);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void create(String addr, ILogic logic) {
        SocketChannel sock = SocketChannel.open();
        sock.configureBlocking(false);
        sock.socket().connect(new InetSocketAddress(addr, listenPort));
        sock.register(epoll, sock.validOps(), new Helper(logic));
    }
    
    public void work() {
        System.err.println("Starting work");
        synchronized (this) {
            System.err.println("Really Starting work");

            epoll.select();
            
            System.err.println("Selected something");
            
            Iterator<SelectionKey> iter = epoll.selectedKeys().iterator();
            
            while (iter.hasNext()) {
                SelectionKey s = iter.next();
                
                if (s.isAcceptable()) {
                    ServerSocketChannel sck = (ServerSocketChannel)s.attachment();
                    
                    SocketChannel client = sck.accept();
                        client.configureBlocking(false);
                        Helper helper = new Helper(client);
                        helper.token = client.register(epoll, client.validOps(), helper);
                }
                
                if (s.attachment() instanceof Helper)
                    if (!handle((Helper)helper)) {
                        iter.remove();
                        helper.token.cancel();
                        helper.sock.close();
                        continue;
                        }
                
                if (s.isWritable() && (s.attachment() instanceof UDPHelper))
                    doWrite((UDPHelper)s.attachment());
                
                if (s.isReadable() && (s.attachment() instanceof UDPHelper)) {
                    doRead((UDPHelper)s.attachment());
                }
                
                iter.remove();
                }
        }
    }

    public void interrupt() {
        System.err.println("interrupting");
        synchronized (this) {
            epoll.wakeup();
        }
        System.err.println("interrupt done");
    }
    
    public void close() {
        try {
            epoll.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
