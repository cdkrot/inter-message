package ru.spbau.intermessage.core;

import ru.spbau.intermessage.gui.Message;
import ru.spbau.intermessage.net.Network;
import ru.spbau.intermessage.net.WifiNetwork;

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
    protected Network network = new WifiNetwork();
    
    protected void handleRequest(RequestCommon req) {
        if (req instanceof ListenerRequest) {
            ListenerRequest reqc = (ListenerRequest)req;
            if (reqc.add)
                listeners.add(reqc.listener);
            else
                listeners.remove(reqc.listener);
        }
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
