package ru.spbau.intermessage.core;

public interface EventListener {
    void onMessage(Chat chat, String uname, User user, Message message);
    void onChatAddition(Chat chat);
}
