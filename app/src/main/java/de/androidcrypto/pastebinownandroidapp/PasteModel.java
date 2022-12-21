package de.androidcrypto.pastebinownandroidapp;

import java.util.Date;

public class PasteModel {

    private String pasteKey;
    private Date pasteDate;
    private String pasteTitle;
    private int pasteSize;
    private String pasteExpireDate;
    private int pastePrivate; // 0=public, 1=unlisted, 2=private
    private String formatLong;
    private String formatShort;
    private String pasteUrl;
    private int pasteHits;

    public PasteModel(String pasteKey, Date pasteDate, String pasteTitle, int pasteSize, String pasteExpireDate, int pastePrivate, String formatLong, String formatShort, String pasteUrl, int pasteHits) {
        this.pasteKey = pasteKey;
        this.pasteDate = pasteDate;
        this.pasteTitle = pasteTitle;
        this.pasteSize = pasteSize;
        this.pasteExpireDate = pasteExpireDate;
        this.pastePrivate = pastePrivate;
        this.formatLong = formatLong;
        this.formatShort = formatShort;
        this.pasteUrl = pasteUrl;
        this.pasteHits = pasteHits;
    }

    public String getPasteKey() {
        return pasteKey;
    }

    public Date getPasteDate() {
        return pasteDate;
    }

    public String getPasteTitle() {
        return pasteTitle;
    }

    public long getPasteSize() {
        return pasteSize;
    }

    public String getPasteExpireDate() {
        return pasteExpireDate;
    }

    public int getPastePrivate() {
        return pastePrivate;
    }

    public String getFormatLong() {
        return formatLong;
    }

    public String getFormatShort() {
        return formatShort;
    }

    public String getPasteUrl() {
        return pasteUrl;
    }

    public int getPasteHits() {
        return pasteHits;
    }
}
