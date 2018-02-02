package ru.spbau.intermessage.crypto;

import android.util.Log;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import org.apache.commons.codec.binary.Hex;

import ru.spbau.intermessage.core.User;

public class ID {
    private KeyPair keys;

    /**
     * Safe constructor. Keys will be cleaned after construction
     * @param priv private key
     * @param pub public key
     */
    public ID(char[] priv, char[] pub) {
        try {
            byte[] publicBytes = Hex.decodeHex(pub);
            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(publicBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(pubKeySpec);

            byte[] privateBytes = Hex.decodeHex(priv);
            PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(privateBytes);
            keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(privKeySpec);

            keys = new KeyPair(publicKey, privateKey);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            Arrays.fill(priv, 'a');
            Arrays.fill(pub, 'a');
        }
    }

    /**
     * Unsafe constructor. Use char[] instead of String.
     * @param priv private key
     * @param pub public key
     */
    public ID(String priv, String pub) {
        this(priv.toCharArray(), pub.toCharArray());
    }

    public ID(KeyPair pair) {
        keys = pair;
    }

    public static ID create() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            KeyPair pair = generator.generateKeyPair();
            return new ID(pair);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public String priv() {
        return new String(Hex.encodeHex(keys.getPrivate().getEncoded()));
    }

    public String pub() {
        return new String(Hex.encodeHex(keys.getPublic().getEncoded()));
    }

    public PublicKey getPublicKey() {
        return keys.getPublic();
    }

    public PrivateKey getPrivateKey() {
        return keys.getPrivate();
    }

    public User user() {
        return new User(pub());
    }
};