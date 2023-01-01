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
import java.util.Date;

public class InternalStorageUtils {

    /**
     * This class is responsible for reading and writing files from and to internal storage
     * There are two base folders for the storage
     * unencrypted files: pastes/unencrypted/
     * encrypted files: pastes/encrypted
     */

    private static final String TAG = "InternalStorageUtils";
    public static final String ENCRYPTED_CONTENT = "ENCRYPTED CONTENT:";
    public static final String UNENCRYPTED_CONTENT = "UNENCRYPTED CONTENT:";
    public static final String VISIBILITY_TYPE_PUBLIC = "PUBLIC";
    public static final String VISIBILITY_TYPE_PRIVATE = "PRIVATE";
    public static final String TIMESTAMP_CONTENT = "TIMESTAMP CONTENT:"; // DO NOT CHANGE
    public static final String URL_HEADER = "URL";
    private final String BASE_FOLDER = "pastes";
    private final String UNENCRYPTED_FOLDER = "unencrypted";
    private final String ENCRYPTED_FOLDER = "encrypted";
    private final String TIMESTAMP_SEPARATOR = "###"; // separates the filename from timestamp in full filename

    Context mContext;

    public InternalStorageUtils(@NonNull Context mContext) {
        Log.d(TAG, "InternalStorageUtils constructed");
        this.mContext = mContext;
    }

    /**
     * This method generates a filenameString from a paste title where all blanks in the name
     * are converted to '_'. As there are multiple pastes possible this function does add the
     * timestamp, separated with an '#'.
     * A file extension '.txt' is added as well
     */
    private String getFilenameString(@NonNull String pasteTitle, @NonNull String timestampString) {
        if (TextUtils.isEmpty(pasteTitle)) {
            Log.e(TAG, "the paste title is empty so i can not get a file name");
            return "";
        }
        if (TextUtils.isEmpty(timestampString)) {
            Log.e(TAG, "the time stamp is empty so i can not get a file name");
            return "";
        }
        String tempFilename = pasteTitle.replaceAll(" ", "_");
        return tempFilename + TIMESTAMP_SEPARATOR
                + timestampString + TIMESTAMP_SEPARATOR + ".txt";
    }

    /**
     * This method generates a readable filenameString from a fileName from internal storage
     * All '_' are converted to ' ', the timestamp is removed and the extension '.txt' is removed as well
     */
    private String getFilenameReadableString(@NonNull String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            Log.e(TAG, "the file name is empty so i can not get a  readablefile name");
            return "";
        }
        // split the filename
        String[] parts = fileName.split(TIMESTAMP_SEPARATOR);
        Log.i(TAG, "### fileName: " + fileName);
        Log.i(TAG, "### parts.length: " + parts.length);
        Log.i(TAG, "### parts[0]: " + parts[0]);
        if (parts.length > 1) {
            Log.i(TAG, "### parts[1]: " + parts[1]);
            Log.i(TAG, "### parts[2]: " + parts[2]);
        }
        //System.out.println("### parts.length: " + parts.length);
        if (parts.length != 3) {
            Log.e(TAG, "the filename was not constructed by the app, aborted");
            return "";
        }
        return parts[0].replaceAll("_", " ") + parts[2];
    }

    public boolean writePasteInternal(@NonNull String filename, @NonNull String content, @NonNull String timestamp, @NonNull boolean contentIsEncrypted, @NonNull boolean contentIsPrivate, @NonNull String url) {
        Log.d(TAG, "writePasteInternal, filename " + filename);
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
        String contentHeaderType;
        String contentHeaderUrl = ":" + URL_HEADER + ":" + url;
        if (contentIsPrivate) {
            contentHeaderType = VISIBILITY_TYPE_PRIVATE;
        } else {
            contentHeaderType = VISIBILITY_TYPE_PUBLIC;
        }
        if (contentIsEncrypted) {
            filePath = new File(basePath, ENCRYPTED_FOLDER);
            contentHeader = ENCRYPTED_CONTENT + contentHeaderType + contentHeaderUrl + "\n";
        } else {
            filePath = new File(basePath, UNENCRYPTED_FOLDER);
            contentHeader = UNENCRYPTED_CONTENT + contentHeaderType + contentHeaderUrl + "\n";
        }
        String timestampString = TIMESTAMP_CONTENT + timestamp + "\n";
        return writeToInternalStorage(
                filePath,
                getFilenameString(filename, timestamp),
                contentHeader + timestampString + content);
    }

    public String loadPasteInternal(@NonNull String filename, @NonNull boolean contentIsEncrypted, @NonNull String timestampString) {
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
        return readFromInternalStorage(filePath.getAbsolutePath(), getFilenameString(filename, timestampString));
    }

    // returns the filenames
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

    // datafields: fileName, fileSize, contentHeaderType (PRIVATE OR PUBLIC),
    // contentType (ENCRYPTED or UNENCRYPTED), timestamp (long), date (Date), url
    public ArrayList<FileModel> listPastesInternalModel(@NonNull boolean contentIsEncrypted) {
        FileModel fileModel;
        File basePath = new File(BASE_FOLDER);
        // create the directory
        //boolean basePathExists = basePath.mkdirs();
        File filePath;
        if (contentIsEncrypted) {
            filePath = new File(basePath, ENCRYPTED_FOLDER);
        } else {
            filePath = new File(basePath, UNENCRYPTED_FOLDER);
        }
        return listInternalFilesModel(filePath.getAbsolutePath());
    }

    // todo add an url field for the link with Pastebin.com


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
        Log.d(TAG, "readFromInternalStorage, filename: " + filename);
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

    private String readFromInternalStorage (@NonNull File file) {
        Log.d(TAG, "readFromInternalStorage, file: " + file.getAbsolutePath());
        if (file == null) {
            Log.e(TAG, "read from storage aborted, file is null");
            return "";
        }
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
                fileNames.add(getFilenameReadableString(files[i].getName()));
            }
        }
        return fileNames;
    }

    /**
     * This method lists all filenames and returns a ArrayList of files of FileModel class
     * for this it needs to read all entries
     * @param path is the folder that is listed
     * @return
     */
    private ArrayList<FileModel> listInternalFilesModel(@NonNull String path) {
        ArrayList<FileModel> tempList = new ArrayList<>();
        File internalStorageDir = new File(mContext.getFilesDir(), path);
        File[] files = internalStorageDir.listFiles();
        try {
        if (files.length == 0) {
            Log.d(TAG, "there are no files stored internally");
            return new ArrayList<>();
        } } catch (NullPointerException e) {
            Log.d(TAG, "there are no files stored internally");
            return new ArrayList<>();
        }
        ArrayList<String> fileNames = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                String fileName = getFilenameReadableString(files[i].getName());
                long fileSize = files[i].length();

                System.out.println("* fileName org: " + files[i].getName());
                System.out.println("* fileName kor: " + fileName);
                System.out.println("* internalStorageDir: " + internalStorageDir.getAbsolutePath());

                // get the contents
                File file = files[i];
                String fileContent = readFromInternalStorage(file);
                if (TextUtils.isEmpty(fileContent)) {
                    Log.e(TAG, "file is empty, reading aborted");
                    return new ArrayList<>();
                }
                // split the full content into lines
                String[] parts = fileContent.split("\n");
                // example contents
                // parts[0] UNENCRYPTED CONTENT:PUBLIC:URL:https://pastebin.com/xxx
                // parts[1] TIMESTAMP CONTENT:1672589504195
                // parts[2] ... content ...

                // todo This does not work, it does not list any file !

                if (parts.length > 0) {
                    String[] partsLine0 = parts[0].split(":");
                    if (partsLine0.length != 4) {
                        Log.e(TAG, "the file is not written by the app, it cannot been analyzed");
                        //break;
                    } else {
                        String contentType;
                        if (partsLine0[0].contains(UNENCRYPTED_CONTENT)) {
                            Log.d(TAG, "paste is UNENCRYPTED");
                            contentType = "UNENCRYPTED";
                        } else if (partsLine0[0].contains(ENCRYPTED_CONTENT)) {
                            Log.d(TAG, "paste is ENCRYPTED");
                            contentType = "ENCRYPTED";
                        } else {
                            Log.d(TAG, "paste is of UNDEFINED CONTENT TYPE");
                            contentType = "UNDEFINED";
                        }
                        String visibilityType;
                        if (partsLine0[1].contains(VISIBILITY_TYPE_PUBLIC)) {
                            Log.d(TAG, "paste is PUBLIC");
                            visibilityType = VISIBILITY_TYPE_PUBLIC;
                        } else if (partsLine0[1].contains(VISIBILITY_TYPE_PRIVATE)) {
                            Log.d(TAG, "paste is PRIVATE");
                            visibilityType = VISIBILITY_TYPE_PRIVATE;
                        } else {
                            Log.d(TAG, "paste is PRIVATE");
                            visibilityType = "UNDEFINED";
                        }
                        String url;
                        if (partsLine0[2].contains(URL_HEADER)) {
                            url = partsLine0[3];
                            Log.d(TAG, "paste URL is " + url);
                        } else {
                            Log.e(TAG, "the file does not have an URL");
                            url = "";
                            //break;
                        }
                        // now check if there is a second line
                        if (parts.length > 1) {
                            String[] partsLine1 = parts[1].split(":");
                            String timestamp;
                            if (partsLine1[0].contains(TIMESTAMP_CONTENT)) {
                                timestamp = partsLine1[1];
                                Log.d(TAG, "paste timestamp is " + timestamp);
                            } else {
                                Log.e(TAG, "the file does not have a timestamp");
                                timestamp = "";
                                //break;
                            }
                            Date date = new Date(Long.parseLong(timestamp));
                            FileModel fileModel = new FileModel(
                                    fileName,
                                    fileSize,
                                    visibilityType,
                                    contentType,
                                    Long.parseLong(timestamp),
                                    date,
                                    url
                            );
                            tempList.add(fileModel);
                        }
                    }
                } else {
                    Log.e(TAG, "the file is not written by the app, it cannot been analyzed");
                }
            }
        }
        return tempList;
    }

    private FileModel getFileCredentials(File file) {
        Log.d(TAG, "getFileCredentials from file");
        if (file == null) {
            Log.e(TAG, "file is null, aborted");
            return null;
        }
        FileModel fileModel = null;
        String fileName = getFilenameReadableString(file.getName());
        long fileSize = file.length();
        // get the complete content
        //String contentFullString = loadPasteInternal(filename, false, "1672577506489");

        /*
                fileModel = new FileModel(
                        fileName,
                        fileSize,
                        visibilityType,
                        contentType,
                        timestamp,
                        date,
                        url
                );

                 */
        return fileModel;
    }

}
