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
        public Helper(SocketChannel sck, ILogic logic_, boolean firstwrite) {
            token = null;
            
            sock = sck;
            
            outbuf = ByteBuffer.allocate(4096);
            inbuf = ByteBuffer.allocate(4096);
            outbuf.order(ByteOrder.BIG_ENDIAN);
            inbuf.order(ByteOrder.BIG_ENDIAN);

            outbuf.clear();
            outbuf.flip();
            inbuf.clear();

            logic = logic_;
            writing = firstwrite;

            if (writing) {
                pending = logic.feed(null);
            }

            System.err.println("CREATED NEW SOCKET + " + writing);
        }

        public boolean writing;        
        public ILogic logic;
        
        public SelectionKey token;
        
        public SocketChannel sock;

        public int off = -6;

        public ByteVector recv = null;
        public ByteVector pending = null;
        
        public int income = -1;

        public int magicAccepter = 0;

        public ByteBuffer outbuf, inbuf;
        
        public int getOutput() {
            if (pending == null)
                return -1;
            
            if (off < -2) {
                return magic[(off++) + 6];
            }
            
            if (off == -2) {
                off++;
                return pending.size() / 256;
            }

            if (off == -1) {
                off++;
                return pending.size() % 256;
            }

            int res = pending.get(off++);
            if (off == pending.size()) {
                off = -6;
                pending = null;
                magicAccepter = 0;
            }
            return res;
        }
        public boolean onInput(byte b) {
            if (writing)
                return false;
            
            System.out.printf("Get %d, recv: %d, income: %d, magicAcc: %d\n", (int)b, (recv == null ? -1 : recv.size()), income, magicAccepter);
            if (magicAccepter == -1) {
                recv.pushBack(b);
                if (recv.size() == income) {
                    pending = logic.feed(recv);

                    if (pending == null) {
                        System.err.println("LOGIC EOF");
                        return false; // END OF LINE.
                    } else {
                        off = -6;
                        writing = true;
                        return true;
                    }
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

    private InetAddress getBroadcast() {
        try {
            byte[] bts = new byte[4];
            bts[0] = bts[1] = bts[2] = bts[3] = (byte)255;
            return InetAddress.getByAddress(bts);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
  
    private DatagramChannel udpsock;
    private UDPLogic udplogic;
    
    private void doUDPWrite() {
        ByteVector vec = udplogic.bcast();

        if (vec != null) {
            try {
                ByteBuffer buf = ByteBuffer.allocate(vec.size() + 6);
                buf.clear();
                for (int i = 0; i != magic.length; ++i)
                    buf.put(magic[i]);
                buf.put((byte)(vec.size() / 256));
                buf.put((byte)(vec.size() % 256));
                
                buf.put(vec.data(), 0, vec.size());

                buf.flip();
                
                int r = udpsock.send(buf, new InetSocketAddress(bcast, udpPort));
                System.out.printf("Send bcast %d bytes\n", r);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void doUDPRead() {
        try {
            ByteBuffer buf = ByteBuffer.allocate(8192);
            InetSocketAddress addr = (InetSocketAddress)udpsock.receive(buf);

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
        if (!helper.token.isValid())
            return false;

        if (helper.token.isConnectable()) {
            try {
                return helper.sock.finishConnect();
            } catch (IOException ex) {
                return false;
            }
        }
        
        if (helper.writing && helper.token.isWritable()) {
            System.err.println("WRITING");
            try {
                if (helper.outbuf.remaining() == 0) {
                    helper.outbuf.clear();

                    int r;
                    System.err.print("OUT:");
                    while (helper.outbuf.remaining() > 0 && (r = helper.getOutput()) != -1) {
                        System.err.print(" " + r);
                        helper.outbuf.put((byte)r);
                    }
                    System.err.println("");

                    helper.outbuf.flip();
                }
                
                if (helper.outbuf.remaining() > 0)
                    helper.sock.write(helper.outbuf);
                else
                    helper.writing = false;
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } else if (!helper.writing && helper.token.isReadable()) {
            System.err.println("READING");
            try {
                helper.inbuf.clear();
                int cnt = helper.sock.read(helper.inbuf);
                if (cnt == -1)
                    return false;

                System.err.print("IN:");
                
                for (int pos = 0; pos != helper.inbuf.position(); ++pos) {
                    if (!helper.onInput(helper.inbuf.get(pos))) {
                        System.err.println("... FAIL");
                        return false;
                    }
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
            
            udpsock = DatagramChannel.open();
            udpsock.configureBlocking(false);
            udpsock.socket().bind(new InetSocketAddress(udpPort));
            udpsock.socket().setBroadcast(true);
            udplogic = new UDPLogic(msg, store);
            udpsock.register(epoll, udpsock.validOps(), udpsock);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void create(String addr, ILogic logic) {
        try {
            SocketChannel sock = SocketChannel.open();
            sock.bind(null);
            sock.configureBlocking(false);
            sock.connect(new InetSocketAddress(addr, listenPort));

            // creating "listening" connection.
            Helper helper = new Helper(sock, logic, true);
            helper.token = sock.register(epoll, sock.validOps(), helper);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void work() {
        try {
            workDo();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public void workDo() throws IOException {
        synchronized (this) {
            epoll.select();
            
            Iterator<SelectionKey> iter = epoll.selectedKeys().iterator();
            
            while (iter.hasNext()) {
                SelectionKey s = iter.next();
                
                if (s.isAcceptable()) {
                    ServerSocketChannel sck = (ServerSocketChannel)s.attachment();
                    
                    SocketChannel client = sck.accept();
                    client.configureBlocking(false);
                    // create "sending" connection.
                    Helper helper = new Helper(client, new Logic(msg, store), false);
                    helper.token = client.register(epoll, client.validOps(), helper);
                }

                if (s.attachment() instanceof Helper) {
                    Helper helper = (Helper)(s.attachment());

                    if (!handle(helper)) {
                        iter.remove();

                        System.err.println("EPICFAIL");

                        helper.token.cancel();
                        helper.logic.disconnect();
                        helper.sock.close();
                        continue;
                    }
                }
                
                if (s.isWritable() && (s.attachment() instanceof DatagramChannel))
                    doUDPWrite();
                
                if (s.isReadable() && (s.attachment() instanceof DatagramChannel))
                    doUDPRead();
                
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
