package ru.spbau.intermessage.util;

public class Util {
    public static byte[] stringToBytes(String str) {
        try {
            return str.getBytes("utf-8");
        } catch (Exception ex) {
            // java sucks.
            throw new RuntimeException(ex);
        }
    }
}
