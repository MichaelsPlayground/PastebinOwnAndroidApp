package de.androidcrypto.pastebinownandroidapp;

import java.util.Date;

public class FileModel {
    private final String fileName;
    private final long fileSize;
    private final String contentHeaderType;
    private final String contentType;
    private final long timestamp;
    private final Date date;
    private final String url;

    public FileModel(String fileName, long fileSize, String contentHeaderType, String contentType, long timestamp, Date date, String url) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.contentHeaderType = contentHeaderType;
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

    public String getContentHeaderType() {
        return contentHeaderType;
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
