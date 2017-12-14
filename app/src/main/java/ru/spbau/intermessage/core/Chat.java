package ru.spbau.intermessage.core;

public class Chat {
    public Chat() {}
    public Chat(String id_) {id = id_;}
    
    public String id;

    public static Chat load(ReadHelper helper) {
        String s = helper.readString();
        if (s == null)
            return null;
        return new Chat(s);
    }
}
