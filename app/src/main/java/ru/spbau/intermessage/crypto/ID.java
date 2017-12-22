package ru.spbau.intermessage.crypto;

import ru.spbau.intermessage.core.User;

public class ID {
    private ID(String priv, String pub) {
        privkey = priv;
        pubkey = pub;
    }
    
    private String privkey;
    private String pubkey;
    
    public static ID create(String tmp) {
        return new ID(tmp, tmp);
    }

    public String priv() {
        return privkey;
    }

    public String pub() {
        return pubkey;
    }

    public User user() {
        return new User(pubkey);
    }
};
