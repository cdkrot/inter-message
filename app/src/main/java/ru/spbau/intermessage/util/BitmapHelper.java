package ru.spbau.intermessage.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ru.spbau.intermessage.Intermessage;

public class BitmapHelper {

    private static final int IMAGE_SIZE = 512;

    public static Bitmap bitmapFromBytes(byte[] bytes) {
        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        return scaleBitmap(bmp);
    }

    public static Bitmap scaleBitmap(Bitmap bmp) {
        double ratio = bmp.getHeight() / bmp.getWidth();
        return Bitmap.createScaledBitmap(bmp, IMAGE_SIZE, (int) (IMAGE_SIZE * ratio), false);
    }

    public static byte[] bytesFromBitmap(Bitmap bmp) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public static File createImageFile() throws IOException {
        File storageDir = Intermessage.getAppContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile("tmp_intermessage", ".jpg", storageDir);
    }
}
