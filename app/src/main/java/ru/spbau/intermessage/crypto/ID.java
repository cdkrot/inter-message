package ru.spbau.intermessage.crypto;

public class ID {
    private ID(String priv, String pub) {
        privkey = priv;
        pubkey = pub;
    }
    
    private String privkey;
    private String pubkey;
    
    public static ID create() {
        String s = Long.toString(System.currentTimeMillis());
        return new ID(s, s);
    }

    public String priv() {
        return privkey;
    }

    public String pub() {
        return pubkey;
    }
};
