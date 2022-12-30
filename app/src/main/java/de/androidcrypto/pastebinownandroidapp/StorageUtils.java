package de.androidcrypto.pastebinownandroidapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StorageUtils {

    /**
     * This is a utility class to securely store credentials like developer key, user name and user password using
     * SharedEncryptedPreferences
     *
     * Security warning: as we are working with EncryptedSharedPreferences the Masterkey is stored in Android's keystore
     * that is not backuped. If your smartphone is unusable for any reason you do not have anymore access to the stored
     * data
     *
     */

    private static final String TAG = "StorageUtils";

    private final Context mContext;
    private String masterKeyAlias;
    private SharedPreferences sharedPreferences; // for credentials
    private boolean libraryIsReady = false;

    // used for credentials
    private final String ENCRYPTED_PREFERENCES_FILENAME = "secret_shared_prefs";
    private final String DEVELOPER_KEY_NAME = "developer_key";
    private final String USER_NAME = "user_name";
    private final String PASSWORD_NAME = "user_password";
    private final String USER_ACCOUNT = "user_account";






    public StorageUtils(Context context) {
        Log.d(TAG, "StorageUtils construction");
        this.mContext = context;
        try {
            masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            sharedPreferences = setupSharedPreferences(mContext, masterKeyAlias, ENCRYPTED_PREFERENCES_FILENAME);
            libraryIsReady = true;
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Error on initialization of StorageUtils: " + e.getMessage());
            libraryIsReady = false;
            e.printStackTrace();
        }
    }

    private SharedPreferences setupSharedPreferences (Context context, String keyAlias, String preferencesFilename) throws GeneralSecurityException, IOException {
        return EncryptedSharedPreferences.create(
                preferencesFilename,
                keyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }

    public boolean isStorageLibraryReady() {
        return libraryIsReady;
    }

    /**
     * developer key utils
     */

    public boolean isDeveloperKeyAvailable() {
        if (TextUtils.isEmpty(getDeveloperKey())) {
            Log.d(TAG, "developer key is not available");
            return false;
        } else {
            Log.d(TAG, "developer key is available");
            return true;
        }
    }

    public boolean setDeveloperKey(String devKey) {
        if (TextUtils.isEmpty(devKey)) {
            Log.e(TAG, "developerKey is empty, storage aborted");
            return false;
        }
        try {
            sharedPreferences.edit().putString(DEVELOPER_KEY_NAME, devKey).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error on key storage: " + e.getMessage());
            return false;
        }
        Log.d(TAG, "developer key sucessful stored");
        return true;
    }

    public String getDeveloperKey() {
        return sharedPreferences.getString(DEVELOPER_KEY_NAME, "");
    }

    /**
     * user name utils
     */

    public boolean isUserNameAvailable() {
        if (TextUtils.isEmpty(getUserName())) {
            Log.d(TAG, "user name is not available");
            return false;
        } else {
            Log.d(TAG, "user name is available");
            return true;
        }
    }

    public boolean setUserName(String userName) {
        if (TextUtils.isEmpty(userName)) {
            Log.e(TAG, "user name is empty, storage aborted");
            return false;
        }
        try {
            sharedPreferences.edit().putString(USER_NAME, userName).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error on user name storage: " + e.getMessage());
            return false;
        }
        Log.d(TAG, "user name sucessful stored");
        return true;
    }

    public String getUserName() {
        return sharedPreferences.getString(USER_NAME, "");
    }

    /**
     * user password utils
     */

    public boolean isUserPasswordAvailable() {
        if (TextUtils.isEmpty(getUserPassword())) {
            Log.d(TAG, "user password is not available");
            return false;
        } else {
            Log.d(TAG, "user password is available");
            return true;
        }
    }

    public boolean setUserPassword(String userPassword) {
        if (TextUtils.isEmpty(userPassword)) {
            Log.e(TAG, "user password is empty, storage aborted");
            return false;
        }
        try {
            sharedPreferences.edit().putString(PASSWORD_NAME, userPassword).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error on user password storage: " + e.getMessage());
            return false;
        }
        Log.d(TAG, "user password sucessful stored");
        return true;
    }

    public String getUserPassword() {
        return sharedPreferences.getString(PASSWORD_NAME, "");
    }

    /**
     * This method checks that a developer key, a user name and user password were stored
     * returns TRUE if all are set or FALSE when one or more are not set
     */

    public boolean checkForCredentials() {
        if (!isDeveloperKeyAvailable()) {
            Log.d(TAG, "the developer key is not available");
            return false;
        }
        if (!isUserNameAvailable()) {
            Log.d(TAG, "the user name is not available");
            return false;
        }
        if (!isUserPasswordAvailable()) {
            Log.d(TAG, "the user password is not available");
            return false;
        }
        return true;
    }

    /**
     * user account utils
     */

    public boolean isUserAccountAvailable() {
        if (TextUtils.isEmpty(getUserAccount())) {
            Log.d(TAG, "user account is not available");
            return false;
        } else {
            Log.d(TAG, "user account is available");
            return true;
        }
    }

    public boolean setUserAccount(String userAccount) {
        if (TextUtils.isEmpty(userAccount)) {
            Log.e(TAG, "user account is empty, storage aborted");
            return false;
        }
        try {
            sharedPreferences.edit().putString(USER_ACCOUNT, userAccount).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error on user account storage: " + e.getMessage());
            return false;
        }
        Log.d(TAG, "user account sucessful stored");
        return true;
    }

    public String getUserAccount() {
        return sharedPreferences.getString(USER_ACCOUNT, "");
    }

    /**
     * section for utils
     */

    public static String base64Encoding(byte[] input) {
        return Base64.encodeToString(input, Base64.NO_WRAP);
    }

    public static byte[] base64Decoding(String input) {
        return Base64.decode(input, Base64.NO_WRAP);
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuffer result = new StringBuffer();
        for (byte b : bytes) result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }

    public static byte[] hexToBytes(String str) {
        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(str.substring(2 * i, 2 * i + 2),
                    16);
        }
        return bytes;
    }

    public static String hexToBase64(String hexString) {
        return base64Encoding(hexToBytes(hexString));
    }

    public static String base64ToHex(String base64String) {
        return bytesToHex(base64Decoding(base64String));
    }
}
