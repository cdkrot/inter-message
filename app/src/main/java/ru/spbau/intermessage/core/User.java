package ru.spbau.intermessage.core;

import ru.spbau.intermessage.util.ReadHelper;
import ru.spbau.intermessage.util.WriteHelper;

public class User {
    public User() {}
    public User(String s) {publicKey = s;}
    
    public String publicKey;
    
    public static User read(ReadHelper helper) {
        String s = helper.readString();
        if (s == null)
            return null;
        return new User(s);
    }

    public void write(WriteHelper helper) {
        helper.writeString(publicKey);
    }

    public boolean equals(User other) {
        return publicKey.equals(other.publicKey);
    }
}
