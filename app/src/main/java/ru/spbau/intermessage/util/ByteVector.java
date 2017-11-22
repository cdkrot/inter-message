package ru.spbau.intermessage.util;

public class ByteVector {
    private byte[] storage = null;
    int sz = 0;
    
    public ByteVector() {
        storage = new byte[8];
    }

    public ByteVector(int n) {
        storage = new byte[n];
        sz = n;
    }

    public void reserve(int n) {
        if (storage.length < n)
            setCapacity(n > 2 * storage.length ? n : 2 * storage.length);
    }

    public void setCapacity(int newcap) {
        byte[] nst = new byte[newcap];

        if (newcap < sz)
            sz = newcap;
        
        for (int i = 0; i != sz; ++i)
            nst[i] = storage[i];

        storage = nst;
    }
    
    public void resize(int n) {
        reserve(n);
        if (sz >= n)
            sz = n;
        else {
            for (int i = sz; i != n; ++i)
                storage[i] = 0;
            sz = n;
        }
    }

    public void pushBack(byte b) {
        resize(sz + 1);
        storage[sz - 1] = b;
    }

    public void popBack() {
        resize(sz - 1);
    }

    public void clear() {
        resize(0);
    }

    public byte get(int i) {
        return storage[i];
    }

    public void set(int i, byte b) {
        storage[i] = b;
    }

    public int size() {
        return sz;
    }

    public int capacity() {
        return storage.length;
    }

    /* direct access to the vector */
    public byte[] data() {
        return storage;
    }
};
