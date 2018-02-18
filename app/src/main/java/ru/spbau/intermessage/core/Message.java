package ru.spbau.intermessage.core;

import android.support.annotation.Nullable;

import java.security.interfaces.RSAPublicKey;

import ru.spbau.intermessage.crypto.ID;
import ru.spbau.intermessage.util.ByteVector;
import ru.spbau.intermessage.util.ReadHelper;
import ru.spbau.intermessage.util.WriteHelper;

public class Message {
    public Message() {}
    public Message(String tp, long tm, byte[] dt, byte[] sig) {
        type = tp;
        timestamp = tm;
        data = dt;
        signature = sig;
    }

    public Message(String tp, long tm, byte[] dt, ID identity) {
        type = tp;
        timestamp = tm;
        data = dt;

        WriteHelper writer = new WriteHelper(new ByteVector());
        writer.writeString(type);
        writer.writeLong(timestamp);
        writer.writeBytes(data);
        signature = identity.getSignature(writer.getData());
    }

    public String type;
    public long timestamp;
    public byte[] data;
    public byte[] signature;

    @Nullable
    public static Message read(ReadHelper reader) {
        Message msg = new Message();
        
        msg.type = reader.readString();
        if (msg.type == null)
            return null;

        if (reader.available() < 8)
            return null;
        msg.timestamp = reader.readLong();

        msg.data = reader.readBytes();
        if (msg.data == null)
            return null;

        msg.signature = reader.readBytes();
        if (msg.signature == null)
            return null;

        return msg;
    }
    
    public void write(WriteHelper writer) {
        writer.writeString(type);
        writer.writeLong(timestamp);
        writer.writeBytes(data);
        writer.writeBytes(signature);
    }

    public boolean verifySignature(RSAPublicKey pubkey) {
        WriteHelper writer = new WriteHelper(new ByteVector());
        writer.writeString(type);
        writer.writeLong(timestamp);
        writer.writeBytes(data);

        return ID.verifySignature(pubkey, writer.getData(), signature);
    }
};
