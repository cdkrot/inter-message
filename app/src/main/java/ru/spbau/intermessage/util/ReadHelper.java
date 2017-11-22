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
        throw new UnsupportedOperationException("");
    }
    
    public int readInt() {
        throw new UnsupportedOperationException("");
    }

    public long readLong() {
        throw new UnsupportedOperationException("");
    }

    // returns NULL if end of data reached.
    public String readString() {
        throw new UnsupportedOperationException("");
    }
}
