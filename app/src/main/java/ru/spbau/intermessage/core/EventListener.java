package ru.spbau.intermessage.core;

public interface EventListener {
    void onMessage(User chat, User user, Message message);
}
