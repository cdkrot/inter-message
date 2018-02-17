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

    public static String bytesToString(byte[] bt) {
        try {
            return new String(bt, "utf-8");
        } catch (Exception ex) {
            // java sucks.
            throw new RuntimeException(ex);
        }
    }

    public static byte[] decodeHex(String s) {
        if (s.length() % 2 == 1)
            throw new IllegalArgumentException();

        s = s.toLowerCase();
        byte[] res = new byte[s.length() / 2];

        for (int i = 0; i != s.length(); ++i) {
             if ('0' <= s.charAt(i) && s.charAt(i) <= '9')
                 res[i / 2] = (byte)(16 * res[i / 2] + s.charAt(i) - '0'); // java, please die.
             else if ('a' <= s.charAt(i) && s.charAt(i) <= 'f')
                 res[i / 2] = (byte)(16 * res[i / 2] + s.charAt(i) - 'a' + 10);
             else
                 throw new IllegalArgumentException();
        }

        return res;
    }
    
    public static String toHex(byte[] s) {
        StringBuilder builder = new StringBuilder();

        for (byte b: s) {
             char ch = (char)(((char)b) & (char)255);
             builder.append((ch / 16) <= 9 ? (char)('0' + (ch / 16)) : (char)('a' + (ch / 16) - 10));
             builder.append((ch % 16) <= 9 ? (char)('0' + (ch % 16)) : (char)('a' + (ch % 16) - 10));
        }

        return builder.toString();
    }
}
