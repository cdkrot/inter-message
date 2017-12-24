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
        writeByte((byte)(sh >> 8));
        writeByte((byte)sh);
    }
    
    public void writeInt(int i) {
        writeShort((short)(i >> 16));
        writeShort((short)(i));
    }

    public void writeLong(long l) {
        writeInt((int)(l >> 32));
        writeInt((int)l);
    }

    public void writeString(String str) {
        writeBytes(Util.stringToBytes(str));
    }

    public void writeBytesSimple(byte[] arr) {
        for (int i = 0; i != arr.length; ++i)
            writeByte(arr[i]);
    }
    
    public void writeBytes(byte[] arr) {
        writeInt(arr.length);
        for (byte b: arr)
            writeByte(b);
    }
}
