package ru.spbau.intermessage.core;

import android.support.annotation.Nullable;

import ru.spbau.intermessage.util.ReadHelper;
import ru.spbau.intermessage.util.WriteHelper;
import ru.spbau.intermessage.util.Util;

public class User {
    public final byte[] pubKey;
    public final String publicKey;
    
    public User(byte[] s) {
        pubKey = s;
        if (pubKey.length != 32)
            throw new IllegalArgumentException();
        publicKey = Util.toHex(s);
    }

    public User(String s) {
        pubKey = Util.decodeHex(s);
        if (pubKey.length != 32)
            throw new IllegalArgumentException();

        publicKey = Util.toHex(pubKey);
    }
    
    @Nullable
    public static User read(ReadHelper helper) {
        if (helper.available() < 32)
            return null;

        byte[] data = new byte[32];
        for (int i = 0; i != 32; ++i)
            data[i] = helper.readByte();
        
        return new User(data);
    }

    public void write(WriteHelper helper) {
        helper.writeBytesSimple(pubKey);
    }

    public boolean equals(User other) {
        return publicKey.equals(other.publicKey);
    }

    public String toString() {
        return publicKey;
    }

    public int compareTo(User other) {
        return toString().compareTo(other.toString());
    }
}
