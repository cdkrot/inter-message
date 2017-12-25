package ru.spbau.intermessage.crypto;

import ru.spbau.intermessage.core.User;

public class ID {
    public ID(String priv, String pub) {
        privkey = priv;
        pubkey = pub;
    }
    
    private String privkey;
    private String pubkey;
    
    public static ID create() {
        return new ID("228", "410");
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
