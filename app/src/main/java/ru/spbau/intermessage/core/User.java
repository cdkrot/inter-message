package ru.spbau.intermessage.core;

public class User {
    public User() {}
    public User(String s) {publicKey = s;}
    
    public String publicKey;
    
    public static Chat load(ReadHelper helper) {
        String s = helper.readString();
        if (s == null)
            return null;
        return new User(s);
    }

    public void write(WriteHelper helper) {
        helper.writeString(s);
    }
}
