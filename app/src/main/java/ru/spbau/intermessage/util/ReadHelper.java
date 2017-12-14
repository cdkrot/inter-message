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
        short res = 0;
        for (int i = 1; i >= 0; --i)
            res |= ((readByte() & 0xFF) << (8 * i));

        return res;
    }
    
    public int readInt() {
        int res = 0;
        for (int i = 3; i >= 0; --i)
            res |= ((readByte() & 0xFF) << (8 * i));

        return res;
    }

    public long readLong() {
        long res = 0;
        for (int i = 7; i >= 0; --i)
            res |= ((readByte() & (long)0xFF) << (long)(8 * i));

        return res;
    }

    public String readString() {
        byte[] bts = readBytes();

        if (bts == null)
            return null;

        return Util.bytesToString(bts);
    }

    public byte[] readBytes() {
        if (available() < 4)
            return null;

        int cnt = readInt();
        if (available() < cnt)
            return null;

        if (cnt < 0)
            return null;
        
        byte[] res = new byte[cnt];
        for (int i = 0; i != cnt; ++i)
            res[i] = readByte();

        return res;
    }

    public boolean skip(byte[] bts) {
        if (available() < bts.length)
            return false;

        for (int i = 0; i != bts.length; ++i)
            if (readByte() != bts[i])
                return false;

        return true;
    }
}
