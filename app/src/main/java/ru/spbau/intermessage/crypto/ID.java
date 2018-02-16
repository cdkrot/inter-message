package ru.spbau.intermessage.crypto;

import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;

import ru.spbau.intermessage.core.User;
import ru.spbau.intermessage.util.Util;
import ru.spbau.intermessage.util.WriteHelper;
import ru.spbau.intermessage.util.ReadHelper;
import ru.spbau.intermessage.util.ByteVector;

import javax.crypto.*;
import javax.crypto.spec.*;

public class ID {
    public RSAPrivateKey privkey;
    public RSAPublicKey pubkey;
    
    private byte[] fingerprint;

    private static KeyGenerator aesKeyGen;

    static {
        try {
            aesKeyGen = KeyGenerator.getInstance("AES");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public ID(RSAPrivateKey privkey, RSAPublicKey pubkey) {
        this.privkey = privkey;
        this.pubkey = pubkey;
        this.fingerprint = getFingerprint(pubkey);
    }
    
    public ID(String pubkey, String privkey) {
        try {
            System.err.println("pub: " + pubkey);
            System.err.println("priv: " + privkey);
            PKCS8EncodedKeySpec privspec = new PKCS8EncodedKeySpec(Util.decodeHex(privkey));
            X509EncodedKeySpec pubspec = new X509EncodedKeySpec(Util.decodeHex(pubkey));
            
            KeyFactory factory = KeyFactory.getInstance("RSA");
            this.privkey = (RSAPrivateKey)factory.generatePrivate(privspec);
            
            factory = KeyFactory.getInstance("RSA");
            this.pubkey  = (RSAPublicKey)factory.generatePublic(pubspec);
        } catch (Exception ex) {
            // if java has not got RSA it is her own problem.
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
    
    public static ID create() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(4096);
            
            KeyPair pair = keyGen.generateKeyPair();
            return new ID((RSAPrivateKey)pair.getPrivate(),(RSAPublicKey)pair.getPublic());
        } catch (Exception ex) {
            // if java has not got RSA it is her own problem.
            throw new RuntimeException(ex);
        }
    }

    public String priv() {
        return Util.toHex(privkey.getEncoded());
    }

    public String pub() {
        return Util.toHex(pubkey.getEncoded());
    }

    public User user() {
        return new User(fingerprint);
    }

    public void writePubkey(WriteHelper writer) {
        writer.writeBytes(pubkey.getEncoded());
    }

    public static RSAPublicKey readPubkey(ReadHelper reader) {
        try {
            return (RSAPublicKey)(KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(reader.readBytes())));
        } catch (Exception ex) {
            return null;
        }
    }
    
    public ByteVector decode(ByteVector src) {
        try {
            ReadHelper reader = new ReadHelper(src);
            byte[] aesbytes = reader.readBytes();
            if (aesbytes == null)
                return null;

            Cipher rsa = Cipher.getInstance("RSA");
            rsa.init(Cipher.DECRYPT_MODE, privkey);

            Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            aesCipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(rsa.doFinal(aesbytes), "AES"));

            byte[] data = reader.readBytes();
            if (data == null || reader.available() > 0)
                return null;

            return ByteVector.wrap(aesCipher.doFinal(data));
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static ByteVector encode(RSAPublicKey pub, ByteVector src) {
        if (src == null)
            return null;
        
        try {
            WriteHelper writer = new WriteHelper(new ByteVector());

            Cipher rsa = Cipher.getInstance("RSA");

            SecretKey aeskey = aesKeyGen.generateKey();
            Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            aesCipher.init(Cipher.ENCRYPT_MODE, aeskey);

            rsa.init(Cipher.ENCRYPT_MODE, pub);
            writer.writeBytes(rsa.doFinal(aeskey.getEncoded()));
            writer.writeBytes(aesCipher.doFinal(src.data(), 0, src.size()));

            return writer.getData();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public static byte[] getFingerprint(RSAPublicKey key) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(key.getEncoded());
            return md.digest();
        } catch (Exception ex) {
            // if java has not got sha-256 it is her own problem.
            throw new RuntimeException(ex);
        }
    }

    public static byte[] getSecureRandom(int count) {
        byte[] res = new byte[count];
        (new SecureRandom()).nextBytes(res);
        return res;
    }
};
