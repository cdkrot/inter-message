package ru.spbau.intermessage.core;

import android.support.annotation.Nullable;

import ru.spbau.intermessage.util.ByteVector;
import ru.spbau.intermessage.util.ReadHelper;
import ru.spbau.intermessage.util.WriteHelper;

public class Message {
    public Message() {}
    public Message(String tp, long tm, byte[] dt) {
        type = tp;
        timestamp = tm;
        data = dt;
    }
    
    public String type;
    public long timestamp;
    public byte[] data;

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

        return msg;
    }
    
    public void write(WriteHelper writer) {
        writer.writeString(type);
        writer.writeLong(timestamp);
        writer.writeBytes(data);
    }
};
