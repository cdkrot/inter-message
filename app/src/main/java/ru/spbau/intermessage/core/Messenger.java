package ru.spbau.intermessage.core;

import ru.spbau.intermessage.core.Message;
import ru.spbau.intermessage.net.Network;
import ru.spbau.intermessage.net.WifiNetwork;

import ru.spbau.intermessage.util.*;
import ru.spbau.intermessage.crypto.ID;
import ru.spbau.intermessage.store.IStorage;

import java.util.Set;
import java.util.HashSet;

public class Messenger extends ServiceCommon {
    public Messenger(IStorage store) {
        storage = store;
    }
    
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
    protected NNetwork network;
    public final ID identity = ID.create();
    protected IStorage storage;

    protected void special() {
        network.work();
    }

    protected void interrupt() {
        network.interrupt();
    }
    
    protected void handleRequest(RequestCommon req) {
        System.out.println("handling request");
        if (req instanceof ListenerRequest) {
            ListenerRequest reqc = (ListenerRequest)req;
            if (reqc.add)
                listeners.add(reqc.listener);
            else
                listeners.remove(reqc.listener);
        }

        if (req instanceof SendMessageRequest) {
            System.out.println("Messenger: trying to send");
            SendMessageRequest reqc = (SendMessageRequest)req;

            WriteHelper helper = new WriteHelper(new ByteVector());
            
            helper.writeString(reqc.message.type);
            helper.writeLong(reqc.message.timestamp);
            helper.writeBytes(reqc.message.data);

            network.send(null, helper.getData(), null);
        }
    }
    
    protected void onMessage(User chat, User user, Message msg) {
        synchronized (this) {
            for (EventListener listener: listeners)
                listener.onMessage(chat, user, msg);
        }
    }
    
    protected void warmUp() {
        network = new WifiNNetwork();
        network.begin(this, store);
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
