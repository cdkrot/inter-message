package ru.spbau.intermessage.net;

import java.nio.channels.*;
import java.nio.*;
import java.io.*;
import java.net.*;
import java.util.*;

import ru.spbau.intermessage.util.ByteVector;

/**
 * This class illustrates one of the inadequate aspects of java.
 *
 * It is recommended to don't read this class for kids, sensitive people, and all others.
 *
 * Live a healthy life, stay away from java.
 */
public class WifiNetwork implements Network {
    private static final int listenPort = 5202;
    private static final int udpPort = 5203;
    private static final byte[] magic = {69, 77, 83, 71}; // EMSG.
    
    private Selector epoll;
    private Queue<Request> queue = new LinkedList<Request>();
    
    private IncomeListener listener;
    private InetAddress bcast;
    
    private class Request {
        public Request() {}
        public Request(String to, ByteVector what, Callback call) {
            this.to = to;
            this.what = what;
            this.call = call;
        }
        
        public String to;
        public ByteVector what;
        public Callback call;
    }
    
    private class Helper {
        public Helper(SocketChannel sck) {
            token = null;
            
            sock = sck;
            pending = new LinkedList<ByteVector>();
            callbacks = new LinkedList<Callback>();
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
        }

        public SelectionKey token;
        
        public SocketChannel sock;

        public Queue<ByteVector> pending;
        public Queue<Callback> callbacks;
        
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
            callbacks = new LinkedList<Callback>();
        }
        
        public DatagramChannel sock;
        public Queue<ByteVector> pending;
        public Queue<Callback> callbacks;
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
    
    private InetAddress getBroadcastOld() {
        System.out.println("Searching for broadcast");
        
        try {
            Enumeration<NetworkInterface> list = NetworkInterface.getNetworkInterfaces();

            while (list.hasMoreElements()) {
                NetworkInterface iface = list.nextElement();

                System.out.println("Found " + iface.getDisplayName());
                
                if (iface == null) continue;
                if (iface.isLoopback()) continue;
                if (!iface.isUp()) continue;
                if (iface.getDisplayName().contains("Virtual")) continue;

                for (InterfaceAddress addr: iface.getInterfaceAddresses())
                    if (addr != null)
                        if (addr.getBroadcast() != null)
                            return addr.getBroadcast();
            }
        } catch (IOException ex) {
            return null;
        }

        return null;
    }
    
    public WifiNetwork() {}

    private UDPHelper udphelper;
    
    public void open(IncomeListener the_listener) {
        try {
            epoll = Selector.open();
            listener = the_listener;
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
            udp.register(epoll, udp.validOps(), udphelper);
        } catch (IOException ex) {
            throw new RuntimeException(ex);                
        }
        
        new Thread() {
            public void run() {
                System.out.println("Starting network");
                try {
                    while (true) {
                        synchronized (this) {
                            while (! queue.isEmpty()) {
                                String address = queue.peek().to;
                                ByteVector data = queue.peek().what;
                                Callback call = queue.peek().call;

                                System.out.println("XXXX");
                                
                                queue.poll();
                                
                                if (address != null) {
                                    SocketChannel client = SocketChannel.open(new InetSocketAddress(address, listenPort));
                                    
                                    client.configureBlocking(false);
                                    Helper helper = new Helper(client);
                                    helper.pending.add(data);
                                    helper.callbacks.add(call);
                                    helper.token = client.register(epoll, client.validOps(), helper);
                                } else {
                                    System.out.println("New UDP bcast");
                                    WifiNetwork.this.udphelper.pending.add(data);
                                    WifiNetwork.this.udphelper.callbacks.add(call);
                                }
                            }

                            if (!epoll.isOpen())
                                break;
                            
                            epoll.select();
                            
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
                                
                                if (s.isWritable() && s.attachment() instanceof Helper) {
                                    Helper helper = (Helper)s.attachment();
                                    doWrite(helper);
                                }

                                if (s.isWritable() && (s.attachment() instanceof UDPHelper)) {
                                    UDPHelper helper = (UDPHelper)s.attachment();
                                    doWrite(helper);
                                }
                                
                                if (s.isReadable() && s.attachment() instanceof Helper) {
                                    Helper helper = (Helper)s.attachment();
                                    
                                    if (!doRead(helper)) {
                                        iter.remove();
                                        helper.token.cancel();
                                        helper.sock.close();
                                        continue;
                                    }
                                }

                                if (s.isReadable() && s.attachment() instanceof UDPHelper) {
                                    UDPHelper helper = (UDPHelper)s.attachment();

                                    doRead(helper);
                                }

                                iter.remove();
                            }
                        }
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }.start();
    }

    private void doWrite(UDPHelper helper) {
        if (!helper.pending.isEmpty()) {
            try {
                System.out.println("Trying to send UDP bcast");
                ByteVector vec = helper.pending.peek();

                
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

                helper.pending.poll();
                Callback call = helper.callbacks.poll();
                if (call != null)
                    call.completed(true);
                
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
            for (int i = 0; i != buf.position(); ++i)
                System.out.printf("%d ", (byte)buf.get(i));

            for (NetworkInterface netint: Collections.list(NetworkInterface.getNetworkInterfaces())) {
                Enumeration<InetAddress> list = netint.getInetAddresses();
                for (InetAddress self: Collections.list(list))
                    if (self.equals(addr.getAddress()))
                        return;
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
                
                listener.recieved(addr.getHostName(), true, res);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private void doWrite(Helper helper) {
        try {
            if (helper.outbuf_pos == helper.outbuf.position()) {
                helper.outbuf_pos = 0;
                helper.outbuf.clear();
            }
            
            int r;

            while (helper.outbuf.remaining() > 0 && (r = helper.getOutput()) != -1) {
                helper.outbuf.put((byte)r);
            }

            if (helper.outbuf_pos != helper.outbuf.position())
                helper.outbuf_pos += helper.sock.write(helper.outbuf);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private boolean doRead(Helper helper) {
        try {
            helper.inbuf.clear();
            int cnt = helper.sock.read(helper.inbuf);
            if (cnt == -1) {
                return false;
            }
            
            for (int pos = 0; pos != helper.inbuf.position(); ++pos)
                if (!helper.onInput(helper.inbuf.get(pos))) {
                    return false;
                }
            return true;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public void send(String address, ByteVector data, Callback call) {
        synchronized (this) {
            System.out.println("New send");
            queue.add(new Request(address, data, call));
            epoll.wakeup();
        }
    }
    
    public void close() {
        synchronized (this) {
            try {
                epoll.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
