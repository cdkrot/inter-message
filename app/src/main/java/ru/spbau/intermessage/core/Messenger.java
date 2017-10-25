package ru.spbau.intermessage.core;

class Messenger {
    int registerEventListener(EventListener listener) {
        throw new UnsupportedOperationException("TODO");
    }
    
    void deleteEventListener(int id) {
        throw new UnsupportedOperationException("TODO");
    }
    
    User[] getUsersInChat(User chat) {
        throw new UnsupportedOperationException("TODO");
    }

    void sendMessage(User chat, Message message) {
        throw new UnsupportedOperationException("TODO");
    }

    Message[] getMessagesFromChat(User chat, Object restrictions) {
        throw new UnsupportedOperationException("TODO");
    }
};
