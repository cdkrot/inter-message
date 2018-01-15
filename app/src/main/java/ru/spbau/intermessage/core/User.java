package ru.spbau.intermessage.core;

import android.support.annotation.Nullable;

import ru.spbau.intermessage.util.ReadHelper;
import ru.spbau.intermessage.util.WriteHelper;

public class User {
    public final String publicKey;

    public User(String s) {
        publicKey = s;
    }

    @Nullable
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
