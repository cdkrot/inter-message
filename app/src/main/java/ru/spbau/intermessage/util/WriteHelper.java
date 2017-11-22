package ru.spbau.intermessage.util;

public class WriteHelper {
    private ByteVector vec;
    
    public WriteHelper(ByteVector v) {
        vec = v;
    }

    public ByteVector getData() {
        return vec;
    }

    public void writeByte(byte b) {
        vec.pushBack(b);
    }

    public void writeShort(short sh) {
        vec.pushBack((byte)(sh >> 8));
        vec.pushBack((byte)sh);
    }
    
    public void writeInt(int i) {
        vec.pushBack((byte)(i >> 24));
        vec.pushBack((byte)(i >> 16));
        vec.pushBack((byte)(i >> 8));
        vec.pushBack((byte)i);
    }

    public void writeLong(long l) {
        writeInt((int)(l >> 32));
        writeInt((int)l);
    }

    public void writeString(String str) {
        writeInt(str.length());
        for (byte b: Util.stringToBytes(str))
            writeByte(b);
    }
}
