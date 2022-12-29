package de.androidcrypto.pastebinownandroidapp;

import android.os.Build;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionUtils {

    /**
     * This class is responsible for en- and decrypting data using the AES algorithm in
     * GCM mode. The encryption key is derived from a passphrase using the algorithm
     * HMACSHA256
     */

    private static final String TAG = "EncryptionUtils";

    private final int PBKDF2_ITERATIONS = 10000; // fixed as minimum
    private final String ENCRYPTION_TRANSFORMATION_GCM = "AES/GCM/NoPadding";
    private final int saltLength = 32;
    private final int nonceLength = 12;

    public EncryptionUtils() {
        Log.d(TAG, "EncryptionUtils construction");
    }

    private String doEncryptionAesGcmPbkdf2(char[] passphraseChar, byte[] plaintextByte) {
        // generate 32 byte random salt for pbkdf2
        SecureRandom secureRandom = new SecureRandom();
        byte[] salt = new byte[saltLength];
        secureRandom.nextBytes(salt);
        // generate 12 byte random nonce for AES GCM
        byte[] nonce = new byte[nonceLength];
        secureRandom.nextBytes(nonce);
        byte[] secretKey = new byte[0];
        SecretKeyFactory secretKeyFactory = null;
        // we are deriving the secretKey from the passphrase with PBKDF2 and using
        // the hash algorithm Hmac256, this is built in from SDK >= 26
        // for older SDKs we are using the own PBKDF2 function
        // api between 23 - 25
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &
                Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            try {
                // uses 3rd party PBKDF function to get PBKDF2withHmacSHA256
                // PBKDF2withHmacSHA256	is available API 26+
                byte[] passphraseByte = charArrayToByteArray(passphraseChar);
                secretKey = PBKDF.pbkdf2("HmacSHA256", passphraseByte, salt, PBKDF2_ITERATIONS, 32);
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
                Log.e("APP_TAG", "generateAndStoreSecretKeyFromPassphrase error: " + e.toString());
                return "";
            }
        }
        // api 26+ has HmacSHA256 available
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                KeySpec keySpec = new PBEKeySpec(passphraseChar, salt, PBKDF2_ITERATIONS, 32 * 8);
                secretKey = secretKeyFactory.generateSecret(keySpec).getEncoded();
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                e.printStackTrace();
                return "";
            }
        }
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "AES");
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(16 * 8, nonce);
        Cipher cipher = null;
        byte[] ciphertext = new byte[0];
        try {
            cipher = Cipher.getInstance(ENCRYPTION_TRANSFORMATION_GCM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gcmParameterSpec);
            ciphertext = cipher.doFinal(plaintextByte);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
            return "";
        }
        // concatenating salt, nonce and ciphertext
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(salt);
            outputStream.write(nonce);
            outputStream.write(ciphertext);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return base64Encoding(outputStream.toByteArray());
    }

    private String doDecryptionAesGcmPbkdf2(char[] passphraseChar, String ciphertextBase64) {
        // split the complete ciphertext into salt, nonce and ciphertext
        byte[] ciphertextComplete = new byte[0];
        try {
            ciphertextComplete = base64Decoding(ciphertextBase64);
        } catch (IllegalArgumentException exception) {
            return "ERROR: The input data (ciphertext) was corrupted.";
        }
        ByteBuffer bb = ByteBuffer.wrap(ciphertextComplete);
        byte[] salt = new byte[saltLength];
        byte[] nonce = new byte[nonceLength];
        byte[] ciphertext = new byte[(ciphertextComplete.length - saltLength - nonceLength)];
        bb.get(salt, 0, salt.length);
        bb.get(nonce, 0, nonce.length);
        bb.get(ciphertext, 0, ciphertext.length);
        SecretKeyFactory secretKeyFactory = null;
        byte[] secretKey = new byte[0];
        // we are deriving the secretKey from the passphrase with PBKDF2 and using
        // the hash algorithm Hmac256, this is built in from SDK >= 26
        // for older SDKs we are using the own PBKDF2 function
        // api between 23 - 25
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &
                Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            try {
                // uses 3rd party PBKDF function to get PBKDF2withHmacSHA256
                // PBKDF2withHmacSHA256	is available API 26+
                byte[] passphraseByte = charArrayToByteArray(passphraseChar);
                secretKey = PBKDF.pbkdf2("HmacSHA256", passphraseByte, salt, PBKDF2_ITERATIONS, 32);
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
                Log.e("APP_TAG", "generateAndStoreSecretKeyFromPassphrase error: " + e.toString());
                return "";
            }
        }
        // api 26+ has HmacSHA256 available
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                KeySpec keySpec = new PBEKeySpec(passphraseChar, salt, PBKDF2_ITERATIONS, 32 * 8);
                secretKey = secretKeyFactory.generateSecret(keySpec).getEncoded();
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                e.printStackTrace();
                return "";
            }
        }
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "AES");
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(16 * 8, nonce);
        Cipher cipher = null;
        byte[] decryptedtextByte = new byte[0];
        try {
            cipher = Cipher.getInstance(ENCRYPTION_TRANSFORMATION_GCM);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmParameterSpec);
            decryptedtextByte = cipher.doFinal(ciphertext);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
            return "ERROR: The passphrase may be wrong or the ciphertext is corrupted";
        }
        return new String(decryptedtextByte, StandardCharsets.UTF_8);
    }

    // https://stackoverflow.com/a/9670279/8166854
    byte[] charArrayToByteArray(char[] chars) {
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
                byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(chars, '\u0000'); // clear sensitive data
        Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data
        return bytes;
    }

    private static String base64Encoding(byte[] input) {
        return Base64.encodeToString(input, Base64.NO_WRAP);
    }

    private static byte[] base64Decoding(String input) {
        return Base64.decode(input, Base64.NO_WRAP);
    }
}
