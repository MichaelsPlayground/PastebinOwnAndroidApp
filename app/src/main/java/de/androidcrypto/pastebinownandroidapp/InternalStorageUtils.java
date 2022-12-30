package de.androidcrypto.pastebinownandroidapp;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class InternalStorageUtils {

    /**
     * This class is responsible for reading and writing files from and to internal storage
     * There are two base folders for the storage
     * unencrypted files: storage/unencrypted/
     * encrypted files: storage/encrypted
     */

    private static final String TAG = "InternalStorageUtils";
    public static final String ENCRYPTED_CONTENT = "ENCRYPTED CONTENT";
    public static final String UNENCRYPTED_CONTENT = "UNENCRYPTED CONTENT";
    public static final String TIMESTAMP_CONTENT = "TIMESTAMP CONTENT:";
    private final String BASE_FOLDER = "pastes";
    private final String UNENCRYPTED_FOLDER = "unencrypted";
    private final String ENCRYPTED_FOLDER = "encrypted";

    Context mContext;

    public InternalStorageUtils(Context mContext) {
        Log.d(TAG, "InternalStorageUtils constructed");
        this.mContext = mContext;
    }

    public boolean writePasteInternal(String filename, String content, String timestamp, boolean contentIsEncrypted) {
        if (TextUtils.isEmpty(filename)) {
            Log.e(TAG, "storage aborted, filename is empty");
            return false;
        }
        if (TextUtils.isEmpty(content)) {
            Log.e(TAG, "storage aborted, content is empty");
            return false;
        }
        if (TextUtils.isEmpty(timestamp)) {
            Log.e(TAG, "storage aborted, timestamp is empty");
            return false;
        }
        File basePath = new File(BASE_FOLDER);
        // create the directory
        boolean basePathExists = basePath.mkdirs();
        File filePath;
        String contentHeader;
        if (contentIsEncrypted) {
            filePath = new File(basePath, ENCRYPTED_FOLDER);
            contentHeader = ENCRYPTED_CONTENT + "\n";
        } else {
            filePath = new File(basePath, UNENCRYPTED_FOLDER);
            contentHeader = UNENCRYPTED_CONTENT + "\n";
        }
        String timestampString = TIMESTAMP_CONTENT + timestamp + "\n";
        return writeToInternalStorage(
                filePath,
                filename,
                contentHeader + timestampString + content);
    }

    public String loadPasteInternal(String filename, boolean contentIsEncrypted) {
        if (TextUtils.isEmpty(filename)) {
            Log.e(TAG, "load from storage aborted, filename is empty");
            return "";
        }
        File basePath = new File(BASE_FOLDER);
        // create the directory
        //boolean basePathExists = basePath.mkdirs();
        File filePath;
        if (contentIsEncrypted) {
            filePath = new File(basePath, ENCRYPTED_FOLDER);
        } else {
            filePath = new File(basePath, UNENCRYPTED_FOLDER);
        }
        return readFromInternalStorage(filePath.getAbsolutePath(), filename);
    }

    private boolean writeToInternalStorage(File basePath, String filename, String content) {
        if (TextUtils.isEmpty(basePath.getAbsolutePath())) {
            Log.e(TAG, "storage aborted, path is empty");
            return false;
        }
        if (TextUtils.isEmpty(filename)) {
            Log.e(TAG, "storage aborted, filename is empty");
            return false;
        }
        if (TextUtils.isEmpty(content)) {
            Log.e(TAG, "storage aborted, content is empty");
            return false;
        }
        File dir = new File(mContext.getFilesDir(), basePath.getAbsolutePath());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        FileWriter writer = null;
        try {
            File file = new File(dir, filename);
            writer = new FileWriter(file);
            writer.append(content);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
        return true;
    }

    private String readFromInternalStorage (String path, String filename) {
        if (TextUtils.isEmpty(path)) {
            Log.e(TAG, "read from storage aborted, path is empty");
            return "";
        }
        if (TextUtils.isEmpty(filename)) {
            Log.e(TAG, "read from storage aborted, filename is empty");
            return "";
        }
        File dir = new File(mContext.getFilesDir(), path);
        File file = new File(dir, filename);
        // check that file is existing
        if (!file.exists()) {
            Log.e(TAG, "read from storage aborted, file is not existing");
            return "";
        }
        int length = (int) file.length();
        byte[] bytes = new byte[length];
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            in.read(bytes);
            in.close();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return "";
        }
    }

}
