package de.androidcrypto.pastebinownandroidapp;

import java.util.Date;

public class FileModelOrg {
    private final String fileName;
    private final long fileSize;
    private final String visibilityType; // PUBLIC, PRIVATE
    private final String contentType; // UNENCRYPTED, ENCRYPTED
    private final long timestamp;
    private final Date date;
    private final String url;

    public FileModelOrg(String fileName, long fileSize, String visibilityType, String contentType, long timestamp, Date date, String url) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.visibilityType = visibilityType;
        this.contentType = contentType;
        this.timestamp = timestamp;
        this.date = date;
        this.url = url;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getVisibilityType() {
        return visibilityType;
    }

    public String getContentType() {
        return contentType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Date getDate() {
        return date;
    }

    public String getUrl() {
        return url;
    }
}
