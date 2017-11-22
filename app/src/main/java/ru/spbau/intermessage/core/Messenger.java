package ru.spbau.intermessage.core;

import ru.spbau.intermessage.core.Message;
import ru.spbau.intermessage.net.Network;
import ru.spbau.intermessage.net.WifiNetwork;

import ru.spbau.intermessage.util.*;

import java.util.Set;
import java.util.HashSet;

public class Messenger extends ServiceCommon {
    
    private static class ListenerRequest extends RequestCommon {
        public ListenerRequest(boolean a, EventListener l) {
            add = a;
            listener = l;
        }
        
        public boolean add;
        public EventListener listener;
    };

    private static class SendMessageRequest extends RequestCommon {
        public SendMessageRequest(User ch, Message msg) {
            chat = ch;
            message = msg;
        }
        
        public User chat;
        public Message message;
    }
    
    protected Set<EventListener> listeners = new HashSet<EventListener>();
    protected Network network;

    // private enum SyncState {
    //     HANDSHAKE_A, // initiator
    //     HANDSHAKE_B, // second
    //     REQUEST_SENT, // initiator
    //     REQUEST_REPLIED, // second
    //     OBJECT_SEND, // initiator
    //     OBJECT_CONFIRMED // second
    // };
    
    // private class SyncPrimitive {      
    //     public User chat;
    //     public User sender;
    //     public int value;

    //     public void write(ByteVector vec) {
    //         8
    //     }
    // };
 
    // private class SyncProcess {
    //     public SyncProcess() {}
    //     public SyncProcess(SyncState st) {state = st;}
        
    //     public SyncState state; // last finished operation.
        
    //     public ArrayList<SyncPrimitive> requests;
    // };
    
    // protected HashMap<User, SyncProcess> sync;
    
    protected void handleRequest(RequestCommon req) {
        if (req instanceof ListenerRequest) {
            ListenerRequest reqc = (ListenerRequest)req;
            if (reqc.add)
                listeners.add(reqc.listener);
            else
                listeners.remove(reqc.listener);
        }
    }
    
    // protected void onData(User user, byte[] data) {
    //     // TODO: insert cryptography here
        
    //     if (data == null or data.length == 0)
    //         return;

    //     int code = data[0];
    //     switch (code) {
    //     case 0:
    //         if (data.length == 1) {
    //             if (sync.contains(user))
    //                 sync.remove(user); // discard old sync -_-.

    //             sync.add(user, new SyncProcess(SyncState.HANDSHAKE_B));
    //             byte[] snd = new byte[1];
    //             snd[0] = 1;
    //             network.send(user, snd);
    //         }
            
    //         break;
    //     case 1:
    //         if (data.length == 1) {
    //             byte[] dta;
                
    //         }
    //     }
    // }

    protected void warmUp() {
        network = new WifiNetwork();
        network.open(new Network.IncomeListener() {
                public void recieved(String from, boolean bcast, ByteVector dta) {
                    System.out.printf("Incoming packet from %s, type %d, len %d\n", from, (bcast ? 1 : 0), dta.size());

                    for (int i = 0; i != Math.min(100, dta.size()); ++i)
                        System.out.printf("%d ", dta.get(i));

                    System.out.println("");
                };
            });
    }
    
    protected void onClose() {
        network.close();
    }
    
    public void registerEventListener(EventListener listener) {
        postRequest(new ListenerRequest(true, listener));
    }
    
    public void deleteEventListener(EventListener listener) {
        postRequest(new ListenerRequest(false, listener));
    }
    
    public User[] getUsersInChat(User chat) {
        throw new UnsupportedOperationException("TODO");
    }

    public void sendMessage(User chat, Message message) {
        postRequest(new SendMessageRequest(chat, message));
    }

    public Message[] getMessagesFromChat(User chat, Object restrictions) {
        throw new UnsupportedOperationException("TODO");
    }
};
