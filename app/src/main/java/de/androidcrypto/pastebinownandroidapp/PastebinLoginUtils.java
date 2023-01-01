package de.androidcrypto.pastebinownandroidapp;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.jpaste.pastebin.account.PastebinAccount;
import org.jpaste.pastebin.exceptions.LoginException;

public class PastebinLoginUtils {

    /**
     * This utility class handles all the login stuff to Pastebin.com
     */

    private static final String TAG = "PastebinLoginUtils";

    private static Context mContext;
    public static PastebinAccount account = null;
    public static String pastebinUserKey = "";

    /**
     * This method logs in to a Pastebin.com account using previously stored credentials
     * (DeveloperKey, UserName and UserPassword).
     * @param context
     * @return
     * 0 = successful login
     * 1 = login aborted, no credentials stored
     * 2 = login aborted, login not successful
     */
    public static int loginToPastebin(Context context) {
        mContext = context;
        Log.d(TAG, "loginToPastebin using stored credentials");
        StorageUtils storageUtils = new StorageUtils(mContext);
        if (!storageUtils.checkForCredentials()) {
            Log.e(TAG, "loginToPastebin aborted, no credentials stored");
            return 1;
        }
        // delete an old userKey
        storageUtils.setUserKey("");
        pastebinUserKey = loginToPastebinInternal(
                storageUtils.getDeveloperKey(),
                storageUtils.getUserName(),
                storageUtils.getUserPassword()
        );
        if (TextUtils.isEmpty(pastebinUserKey)) {
            Log.e(TAG, "loginToPastebin failed (wrong DeveloperKey, UserName and/or UserPassword ?)");
            return 2;
        }
        // store the userKey
        storageUtils.setUserKey(pastebinUserKey);
        return 0;
    }

    /**
     * This method logs in to a Pastebin.com account using DeveloperKey, UserName and UserPassword
     * @param developerKey
     * @param userName
     * @param userPassword
     * @return the userKey on success or '' on failure
     */
    private static String loginToPastebinInternal(String developerKey, String userName, String userPassword) {
        account = new PastebinAccount(developerKey, userName, userPassword);
        // fetches an user session id
        try {
            account.login();
        } catch (LoginException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
            return "";
        }
        return account.getUserSessionId();
    }

    /**
     * This method logs in to a Pastebin.com account using previously stored userKey
     * @param context
     * @return
     * 0 = successful login
     * 1 = login aborted, no user key stored
     * 2 = login aborted, login not successful
     */
    public static int loginToPastebinUserKey(Context context) {
        mContext = context;
        Log.d(TAG, "loginToPastebin using stored userKey");
        StorageUtils storageUtils = new StorageUtils(mContext);
        if (!storageUtils.isUserKeyAvailable()) {
            Log.e(TAG, "loginToPastebin aborted, no userKey stored");
            return 1;
        }
        pastebinUserKey = storageUtils.getUserKey();
        account = new PastebinAccount(pastebinUserKey);
        if (TextUtils.isEmpty(account.getUserSessionId())) {
            Log.e(TAG, "loginToPastebin failed (wrong UserKey ?)");
            return 2;
        }
        return 0;
    }


    /**
     * This method checks if we can ping to www.google.com - if yes we do have an active internet connection
     * returns true if there is an active internet connection
     * returns false if there is no active internet connection
     * https://stackoverflow.com/a/45777087/8166854 by sami rahimi
     */
    public static boolean deviceIsOnline() {
        try {
            Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.com");
            int returnVal = p1.waitFor();
            boolean reachable = (returnVal==0);
            return reachable;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
            return false;
        }
    }

}
