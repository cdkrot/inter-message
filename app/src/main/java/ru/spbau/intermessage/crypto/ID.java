package ru.spbau.intermessage.crypto;

import java.security.SecureRandom;

import ru.spbau.intermessage.core.User;

public class ID {
    public ID(String priv, String pub) {
        privkey = priv;
        pubkey = pub;
    }

    private String privkey;
    private String pubkey;

    public static ID create() {
        SecureRandom secureRandom = new SecureRandom();
        String publicKey = Long.toHexString(Double.doubleToLongBits(secureRandom.nextDouble()));
        String privateKey = Long.toHexString(Double.doubleToLongBits(secureRandom.nextDouble()));
        return new ID(privateKey, publicKey);
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
}