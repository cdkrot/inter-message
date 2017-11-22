package ru.spbau.intermessage.util;

public class ReadHelper {
    private ByteVector vec;
    private int pos;
    
    public ReadHelper(ByteVector v) {
        vec = v;
        pos = 0;
    }

    public ReadHelper(ByteVector v, int position) {
        vec = v;
        pos = position;
    }

    public int available() {
        return vec.size() - pos;
    }
    
    public byte readByte() {
        return vec.get(pos++);
    }

    public short readShort() {
        short b1 = readByte();
        short b2 = readByte();

        return (short)((b1 << 8) | b2);
    }
    
    public int readInt() {
        int b1 = readShort();
        int b2 = readShort();

        return (b1 << 16) | b2;
    }

    public long readLong() {
        long b1 = readShort();
        long b2 = readShort();

        return (b1 << 16) | b2;
    }

    public String readString() {
        byte[] bts = readBytes();

        if (bts == null)
            return null;

        return Util.bytesToString(bts);
    }

    public byte[] readBytes() {
        return null;
    }

}
