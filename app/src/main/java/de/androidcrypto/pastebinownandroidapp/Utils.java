package de.androidcrypto.pastebinownandroidapp;

import android.content.Context;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    public static String createTimestamp() {
        return new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date());
    }

    public static void createFileInInternalStorage(Context context, String filename) {
        String sampledata = "This file was created on "
                + createTimestamp()
                + "\nSecond line of file " + filename;

        File file = new File(context.getFilesDir(), filename);
        try {
            FileWriter writer = new FileWriter(file);
            writer.append(sampledata);
            writer.flush();
            writer.close();
            //Toast.makeText(MainActivity.this, "Saved your text", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }

    }

    public static String bytesToHex(byte[] bytes) {
        StringBuffer result = new StringBuffer();
        for (byte b : bytes)
            result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }
}
