package ru.spbau.intermessage.core;

class Message {
    public Message() {}
    public Message(String tp, long tm, byte[] dt) {
        type = tp;
        timestamp = tm;
        data = dt;
    }
    
    public String type;
    public long timestamp;
    public byte[] data;
};
