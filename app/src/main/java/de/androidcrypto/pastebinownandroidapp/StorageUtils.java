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







    private SharedPreferences sharedOwnPreferences; // for own keys and encryption
    private SharedPreferences shared3rdPartyPreferences; // for storage of 3rd party public keys




    private boolean libsodiumUtilsAvailable = false;
    private int lastCryptoBoxKeyPairNumber = 0;




    private final String PRIVATE_KEY_NAME = "private_key_";
    private final String PUBLIC_KEY_NAME = "public_key_";
    private final String KEY_GENERATION_TIMESTAMP = "key_timestamp_";
    private final String KEY_GENERATION_TIMESTAMP_STRING = "key_timestamp_string_";
    private final String LAST_KEYPAIR_NUMBER = "last_keypair_number";

    // used for 3rd party public keys
    private final String PREFERENCES_FILENAME_3RDPARTY = "3rdpty_secret_shared_prefs";
    private final String PUBLIC_KEY_NAME_3RDPARTY = "public_key_";
    // note: the name is public_key_aliasname_number



    public StorageUtils(Context context) {
        Log.d(TAG, "StorageUtils construction");
        this.mContext = context;
        try {
            masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            sharedPreferences = setupSharedPreferences(mContext, masterKeyAlias, ENCRYPTED_PREFERENCES_FILENAME);
            libraryIsReady = true;


            sharedOwnPreferences = setupSharedPreferences(mContext, masterKeyAlias, ENCRYPTED_PREFERENCES_FILENAME);
            shared3rdPartyPreferences = setupSharedPreferences(mContext, masterKeyAlias, PREFERENCES_FILENAME_3RDPARTY);
            /*
            sharedPreferences = EncryptedSharedPreferences.create(
                    PREFERENCES_FILENAME,
                    masterKeyAlias,
                    mContext,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );*/
            // init Lazysodium
            //ls = new LazySodiumAndroid(new SodiumAndroid());
            // get the last generated keyPair number
            /*
            lastCryptoBoxKeyPairNumber = getLastCryptoBoxKeyPairNumberFromPreferences();
            if (lastCryptoBoxKeyPairNumber == 0) {
                Log.d(TAG, "generate the first keyPair");
                generateNewKeyPair();
            }
            lastCryptoBoxKeyPairNumber = getLastCryptoBoxKeyPairNumberFromPreferences();
            libsodiumUtilsAvailable = true;
            Log.d(TAG, "LibsodiumUtils available, actual lastCryptoBoxKeyPairNumber: " + lastCryptoBoxKeyPairNumber);

             */
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Error on initialization of StorageUtils: " + e.getMessage());
            libraryIsReady = false;

            libsodiumUtilsAvailable = false;
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
     * OLD
     */

    public boolean isLibsodiumUtilsAvailable() {
        Log.d(TAG, "isLibsodiumUtilsAvailable");
        return libsodiumUtilsAvailable;
    }

    public int getLastCryptoBoxKeyPairNumber() {
        Log.d(TAG, "getLastCryptoBoxKeyPairNumber");
        return lastCryptoBoxKeyPairNumber;
    }

    public long getKeyGenerationTimestamp(int keyNumber) {
        Log.d(TAG, "getKeyGenerationTimestamp");
        if (keyNumber < 1) {
            Log.e(TAG, "asking for an invalid key (key number is smaller than 1)");
            return 0;
        }
        if (keyNumber > lastCryptoBoxKeyPairNumber) {
            Log.e(TAG, "asking for an invalid key (key number larger than lastCryptoBoxKeyPairNumber)");
            return 0;
        }
        return sharedOwnPreferences.getLong(KEY_GENERATION_TIMESTAMP + "_" + String.valueOf(keyNumber), 0);
    }

    public String getKeyGenerationTimestampString(int keyNumber) {
        Log.d(TAG, "getKeyGenerationTimestampString");
        if (keyNumber < 1) {
            Log.e(TAG, "asking for an invalid key (key number is smaller than 1)");
            return "";
        }
        if (keyNumber > lastCryptoBoxKeyPairNumber) {
            Log.e(TAG, "asking for an invalid key (key number larger than lastCryptoBoxKeyPairNumber)");
            return "";
        }
        return sharedOwnPreferences.getString(KEY_GENERATION_TIMESTAMP_STRING + "_" + String.valueOf(keyNumber), "");
    }
/*
    public int generateNewKeyPair() {
        Log.d(TAG, "generate new KeyPair");
        KeyPair newKeyPair = generateCryptoBoxKeypairLazysodium();
        if (newKeyPair == null) {
            Log.e(TAG, "The keyPair could not get generated");
            return -1;
        }
        try {
            long actualTime = new Date().getTime();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String actualTimeString = dateFormat.format(actualTime);
            String privateKeyBase64 = getCryptoBoxPrivateKeyBase64Lazysodium(newKeyPair);
            String publicKeyBase64 = getCryptoBoxPublicKeyBase64Lazysodium(newKeyPair);
            // we are going to store the data
            lastCryptoBoxKeyPairNumber++;
            sharedOwnPreferences.edit().putString(PRIVATE_KEY_NAME + "_" + String.valueOf(lastCryptoBoxKeyPairNumber), privateKeyBase64).apply();
            sharedOwnPreferences.edit().putString(PUBLIC_KEY_NAME + "_" + String.valueOf(lastCryptoBoxKeyPairNumber), publicKeyBase64).apply();
            sharedOwnPreferences.edit().putLong(KEY_GENERATION_TIMESTAMP + "_" + String.valueOf(lastCryptoBoxKeyPairNumber), actualTime).apply();
            sharedOwnPreferences.edit().putString(KEY_GENERATION_TIMESTAMP_STRING + "_" + String.valueOf(lastCryptoBoxKeyPairNumber), actualTimeString).apply();
            sharedOwnPreferences.edit().putInt(LAST_KEYPAIR_NUMBER, lastCryptoBoxKeyPairNumber).apply();
            Log.d(TAG, "new keyPair generated and stored, number: " + String.valueOf(lastCryptoBoxKeyPairNumber));
            return lastCryptoBoxKeyPairNumber;
        } catch (Exception e) {
            Log.e(TAG, "Error on key generation and storage: " + e.getMessage());
            return -1;
        }
    }
*/
    private int getLastCryptoBoxKeyPairNumberFromPreferences() {
        Log.d(TAG, "getLastCryptoBoxKeyPairNumberFromPreferences");
        return sharedOwnPreferences.getInt(LAST_KEYPAIR_NUMBER, 0);
    }

    public String getPublicKeyBase64(int keyNumber) {
        Log.d(TAG, "getPublicKeyBase64");
        if (keyNumber < 1) {
            Log.e(TAG, "asking for an invalid key (key number is smaller than 1)");
            return null;
        }
        if (keyNumber > lastCryptoBoxKeyPairNumber) {
            Log.e(TAG, "asking for an invalid key (key number larger than lastCryptoBoxKeyPairNumber)");
            return null;
        }
        return sharedOwnPreferences.getString(PUBLIC_KEY_NAME + "_" + String.valueOf(keyNumber), "");
    }

    public String get3rdPartyPublicKeyBase64(String aliasName, int keyNumber) {
        Log.d(TAG, "get3rdPartyPublicKeyBase64");
        if (keyNumber < 1) {
            Log.e(TAG, "asking for an invalid key (key number is smaller than 1)");
            return null;
        }
        //
        /*
        if (keyNumber > lastCryptoBoxKeyPairNumber) {
            Log.e(TAG, "asking for an invalid key (key number larger than lastCryptoBoxKeyPairNumber)");
            return null;
        }
         */
        // note: the name is public_key_aliasname_number
        String keyName = PUBLIC_KEY_NAME_3RDPARTY + "_" + aliasName + "_" + String.valueOf(keyNumber);
        return shared3rdPartyPreferences.getString(keyName, "");
    }
/*
    public boolean set3rdPartyPublicKeyBase64(String aliasName, int keyNumber, String publicKeyBase64) {
        Log.d(TAG, "set3rdPartyPublicKeyBase64");
        if (TextUtils.isEmpty(aliasName)) {
            Log.e(TAG, "aliasname is empty");
            return false;
        }
        if (keyNumber < 1) {
            Log.e(TAG, "asking for an invalid key (key number is smaller than 1)");
            return false;
        }
        if (TextUtils.isEmpty(publicKeyBase64)) {
            Log.e(TAG, "publicKeyBase64 is empty");
            return false;
        }
        // note: the name is public_key_aliasname_number
        String keyName = PUBLIC_KEY_NAME_3RDPARTY + "_" + aliasName + "_" + String.valueOf(keyNumber);
        try {
            shared3rdPartyPreferences.edit().putString(keyName, publicKeyBase64).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error on key storage: " + e.getMessage());
            return false;
        }
        return true;
    }
*/


    /**
     *
     * @param plaintext String to encrypt
     * @param privateKeyNumber own = senders private key
     * @param publicKeyBase64 receipients public key in Base64 encoding
     * @return nonce:ciphertext in hex encoding
     */
/*
    public String encryptCryptoBox(String plaintext, int privateKeyNumber, String publicKeyBase64) {
        Log.d(TAG, "encryptCryptoBox");
        if (TextUtils.isEmpty(plaintext)) {
            Log.e(TAG, "plaintext is empty");
            return "";
        }
        if (TextUtils.isEmpty(publicKeyBase64)) {
            Log.e(TAG, "publicKeyBase64 is empty");
            return "";
        }
        if (privateKeyNumber < 1) {
            Log.e(TAG, "asking for an invalid key (key number is smaller than 1)");
            return "";
        }
        if (privateKeyNumber > lastCryptoBoxKeyPairNumber) {
            Log.e(TAG, "asking for an invalid key (key number larger than lastCryptoBoxKeyPairNumber)");
            return null;
        }
        try {
            String privateKeyBase64 = sharedOwnPreferences.getString(PRIVATE_KEY_NAME + "_" + String.valueOf(privateKeyNumber), "");
            if (privateKeyBase64.equals("")) {
                Log.e(TAG, "no privateKey found for privateKeyNumber " + privateKeyNumber);
                return "";
            }
            return encryptCryptoBoxHexLazysodium(privateKeyBase64, publicKeyBase64, plaintext);
        } catch (Exception e) {
            Log.e(TAG, "Error on encryption");
            return "";
        }
    }
*/
    /**
     *
     * @param ciphertext String to decrypt
     * @param privateKeyNumber own = receivers private key
     * @param publicKeyBase64 senders public key in Base64 encoding
     * @return decryptedtext
     */
    /*
    public String decryptCryptoBox(String ciphertext, int privateKeyNumber, String publicKeyBase64) {
        Log.d(TAG, "decryptCryptoBox");
        if (TextUtils.isEmpty(ciphertext)) {
            Log.e(TAG, "ciphertext is empty");
            return "";
        }
        if (TextUtils.isEmpty(publicKeyBase64)) {
            Log.e(TAG, "publicKeyBase64 is empty");
            return "";
        }
        if (privateKeyNumber < 1) {
            Log.e(TAG, "asking for an invalid key (key number is smaller than 1)");
            return "";
        }
        if (privateKeyNumber > lastCryptoBoxKeyPairNumber) {
            Log.e(TAG, "asking for an invalid key (key number larger than lastCryptoBoxKeyPairNumber)");
            return null;
        }
        try {
            String privateKeyBase64 = sharedOwnPreferences.getString(PRIVATE_KEY_NAME + "_" + String.valueOf(privateKeyNumber), "");
            if (privateKeyBase64.equals("")) {
                Log.e(TAG, "no privateKey found for privateKeyNumber " + privateKeyNumber);
                return "";
            }
            return decryptCryptoBoxHexLazysodium(privateKeyBase64, publicKeyBase64, ciphertext);
        } catch (Exception e) {
            Log.e(TAG, "Error on decryption");
            return "";
        }
    }
*/
    /**
     * section for Lazysodium
     */
/*
    private KeyPair generateCryptoBoxKeypairLazysodium() {
        try {
            Box.Lazy box = (Box.Lazy) ls;
            return box.cryptoBoxKeypair();
        } catch (SodiumException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getCryptoBoxPrivateKeyBase64Lazysodium(com.goterl.lazysodium.utils.KeyPair keyPair) {
        return base64Encoding(keyPair.getSecretKey().getAsBytes());
    }

    private String getCryptoBoxPublicKeyBase64Lazysodium(com.goterl.lazysodium.utils.KeyPair keyPair) {
        return base64Encoding(keyPair.getPublicKey().getAsBytes());
    }*/

    /**
     * This is the CryptoBox encryption, it needs the publicKeyA from receipient and privateKeyB from sender (both in Base64 encoding)
     *
     * @param privateKeyB from sender in Base64 encoding
     * @param publicKeyA from receipient in Base64 encoding
     * @param plaintext
     * @return the nonce and ciphertext in hex encoding, separated by ":" (nonce:ciphertext)
     */
    /*
    private String encryptCryptoBoxHexLazysodium(String privateKeyB, String publicKeyA, String plaintext) {
        try {
            Box.Lazy box = (Box.Lazy) ls;
            // get the keys
            com.goterl.lazysodium.utils.Key keyA = Key.fromBytes(base64Decoding(publicKeyA));
            com.goterl.lazysodium.utils.Key keyB = Key.fromBytes(base64Decoding(privateKeyB));
            KeyPair encryptionKeyPair = new KeyPair(keyA, keyB);
            byte[] nonce = ls.randomBytesBuf(SecretBox.NONCEBYTES);
            // box.cryptoBoxEasy returns a hex encoded string but not a Base64 encoded one
            return bytesToHex(nonce) + ":" + box.cryptoBoxEasy(plaintext, nonce, encryptionKeyPair);
        } catch (SodiumException e) {
            e.printStackTrace();
            return "";
        }
    }*/

    /**
     * This is the CryptoBox decryption, it needs the publicKeyB from sender and privateKeyA from receipient (both in Base64 encoding)
     *
     * @param privateKeyA from receipient
     * @param publicKeyB from sender
     * @param completeCiphertext as nonce:ciphertext (each in hex encoding)
     * @return the decrypted value/string
     */
    /*
    private String decryptCryptoBoxHexLazysodium(String privateKeyA, String publicKeyB, String completeCiphertext) {
        try {
            String[] parts = completeCiphertext.split(":", 0);
            if (parts.length != 2) return "";
            Box.Lazy box = (Box.Lazy) ls;
            // get the keys
            com.goterl.lazysodium.utils.Key keyA = Key.fromBytes(base64Decoding(privateKeyA));
            com.goterl.lazysodium.utils.Key keyB = Key.fromBytes(base64Decoding(publicKeyB));
            KeyPair decryptionKeyPair = new KeyPair(keyB, keyA);
            return box.cryptoBoxOpenEasy(parts[1], hexToBytes(parts[0]), decryptionKeyPair);
        } catch (SodiumException e) {
            e.printStackTrace();
            return "";
        }
    }
*/
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
