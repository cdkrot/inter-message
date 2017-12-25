package ru.spbau.intermessage.core;

public interface EventListener {
    void onMessage(Chat chat, User user, Message message);
}
