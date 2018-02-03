package ru.spbau.intermessage.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;

public class BitmapHelper {

    //TODO

    private static final int WIDTH = 100;
    private static final int HEIGHT = 100;

    public static Bitmap bitmapFromBytes(byte[] bytes) {
        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        return Bitmap.createScaledBitmap(bmp, WIDTH, HEIGHT, false);
    }

    public static byte[] bytesFromBitmap(Bitmap bmp) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }
}
