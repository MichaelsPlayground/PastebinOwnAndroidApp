package de.androidcrypto.pastebinownandroidapp;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class InternalStorageUtils {

    /**
     * This class is responsible for reading and writing files from and to internal storage
     * There are two base folders for the storage
     * unencrypted files: pastes/unencrypted/
     * encrypted files: pastes/encrypted
     */

    private static final String TAG = "InternalStorageUtils";
    public static final String ENCRYPTED_CONTENT = "ENCRYPTED CONTENT";
    public static final String UNENCRYPTED_CONTENT = "UNENCRYPTED CONTENT";
    public static final String TIMESTAMP_CONTENT = "TIMESTAMP CONTENT:"; // DO NOT CHANGE
    private final String BASE_FOLDER = "pastes";
    private final String UNENCRYPTED_FOLDER = "unencrypted";
    private final String ENCRYPTED_FOLDER = "encrypted";

    Context mContext;

    public InternalStorageUtils(@NonNull Context mContext) {
        Log.d(TAG, "InternalStorageUtils constructed");
        this.mContext = mContext;
    }

    /**
     * This method generates a filenameString from a paste title where all blanks
     * are converted to '_' and a ending '.txt' is added
     */
    private String getFilenameString(@NonNull String pasteTitle) {
        if (TextUtils.isEmpty(pasteTitle)) {
            Log.e(TAG, "the paste title is empty so i can not get a file name");
            return "";
        }
        String tempFilename = pasteTitle.replaceAll(" ", "_");
        return tempFilename + ".txt";
    }

    public boolean writePasteInternal(@NonNull String filename, @NonNull String content, @NonNull String timestamp, @NonNull boolean contentIsEncrypted) {
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
                getFilenameString(filename),
                contentHeader + timestampString + content);
    }

    public String loadPasteInternal(@NonNull String filename, @NonNull boolean contentIsEncrypted) {
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
        return readFromInternalStorage(filePath.getAbsolutePath(), getFilenameString(filename));
    }

    public ArrayList<String> listPastesInternal(@NonNull boolean contentIsEncrypted) {
        File basePath = new File(BASE_FOLDER);
        // create the directory
        //boolean basePathExists = basePath.mkdirs();
        File filePath;
        if (contentIsEncrypted) {
            filePath = new File(basePath, ENCRYPTED_FOLDER);
        } else {
            filePath = new File(basePath, UNENCRYPTED_FOLDER);
        }
        return listInternalFiles(filePath.getAbsolutePath());
    }

    private boolean writeToInternalStorage(@NonNull File basePath, @NonNull String filename, @NonNull String content) {
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

    private String readFromInternalStorage (@NonNull String path, @NonNull String filename) {
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

    /**
     * This method returns the timestamp from the (stripped) content
     * The full content contains a header line that needs to get stripped of before
     * using this method
     * The timestamp is a string behind the text 'TIMESTAMP CONTENT:'
     * @param content
     * @return the timestamp String
     */
    public String returnTimestampFromContent(@NonNull String content) {
        System.out.println("*** content:" + content + "###");
        int indexEnd = content.indexOf('\n');
        // check for indexEnd = -1 means there is no line break
        if (indexEnd == -1) return "";
        int indexStartBeginner = content.indexOf(TIMESTAMP_CONTENT);
        if (indexStartBeginner != 0) {
            Log.e(TAG, "there is no timestamp in content");
            return "";
        }
        // System.out.println("*** indexEnd: " + indexEnd);
        // System.out.println("*** indexBeginner: " + indexStartBeginner);
        return content.substring(indexStartBeginner + TIMESTAMP_CONTENT.length(), indexEnd);
    }

    /**
     * This method lists all filenames and returns a ArrayList of files
     * @param path is the folder that is listed
     * @return
     */
    private ArrayList<String> listInternalFiles(@NonNull String path) {
        //ArrayList<String> tempList = new ArrayList<>();
        File internalStorageDir = new File(mContext.getFilesDir(), path);
        File[] files = internalStorageDir.listFiles();
        ArrayList<String> fileNames = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                fileNames.add(files[i].getName());
            }
        }
        return fileNames;
    }
}
