package de.androidcrypto.pastebinownandroidapp;

import static de.androidcrypto.pastebinownandroidapp.InternalStorageUtils.URL_DEFAULT;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.nio.charset.StandardCharsets;
import java.util.Date;

public class InternalStorageActivity extends AppCompatActivity {

    private static final String TAG = "InternalStorageAct";

    com.google.android.material.textfield.TextInputEditText content;
    Button save, loadUnencrypted, loadEncrypted, generateTestPastes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internal_storage);

        content = findViewById(R.id.etISAContent);
        save = findViewById(R.id.btnISASave);
        loadUnencrypted = findViewById(R.id.btnISALoadUnencrypted);
        loadEncrypted = findViewById(R.id.btnISALoadEncrypted);
        generateTestPastes = findViewById(R.id.btnISAGenerateTestPastes);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "save");

                String filename = "test";
                String sampleUnencryptedString = "This is a sample UNencrypted paste.\n" +
                        "This is using an AES-256 encryption in GCM mode.\n" +
                        "The encryption key is derived by running a PBKDF2 key derivation using " +
                        "(at minimum) 10000 iterations and a HMAC256SHA256 algorithm.";
                String sampleString = "This is a sample encrypted paste.\n" +
                        "This is using an AES-256 encryption in GCM mode.\n" +
                        "The encryption key is derived by running a PBKDF2 key derivation using " +
                        "(at minimum) 10000 iterations and a HMAC256SHA256 algorithm.";
                String samplePassphraseString = "1234";
                EncryptionUtils eu = new EncryptionUtils();

                String sampleCiphertextString = eu.doEncryptionAesGcmPbkdf2(
                        samplePassphraseString.toCharArray(),
                        sampleString.getBytes(StandardCharsets.UTF_8));
                System.out.println("sampleCiphertextString:\n" + sampleCiphertextString);

                // get the actual timestamp
                long timestamp = new Date().getTime();
                // use a dummy timestamp
                //long timestamp = Long.parseLong("1672577506489");
                InternalStorageUtils internalStorageUtils = new InternalStorageUtils(view.getContext());
                // write an unencrypted string
                boolean writeSuccess = internalStorageUtils.writePasteInternal(
                        filename,
                        sampleUnencryptedString,
                        String.valueOf(timestamp),
                        false,
                        false,
                        "https://pastebin.com/xxx");
                Log.i(TAG, "unencrypted writeSuccess: " + writeSuccess);
                // write an encrypted string
                writeSuccess = internalStorageUtils.writePasteInternal(
                        filename,
                        sampleCiphertextString,
                        String.valueOf(timestamp),
                        true,
                        false,
                        "https://pastebin.com/abcdef");
                Log.i(TAG, "encrypted writeSuccess: " + writeSuccess);

            }
        });

        loadUnencrypted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "load unencrypted");
                String filename = "test";
                InternalStorageUtils internalStorageUtils = new InternalStorageUtils(view.getContext());
                String contentFullString = internalStorageUtils.loadPasteInternal(filename, false, "1672577506489");
                content.setText(getRawContent2Lines(contentFullString));
                // get the actual timestamp
                //long timestamp = new Date().getTime();
                //String timestampPaste = "1672403428006";
                //String timestampPaste = "1772403428006";
                // the method to retrieve the timestamp from a stored paste is in InternalStorageUtils
                // to use the method we need to strip off the first line of the content
                String tempContent = getRawContent(contentFullString);
                String timestampPaste = internalStorageUtils.returnTimestampFromContent(tempContent);
                System.out.println("*** timestampPaste: " + timestampPaste);
                //String timestampPasteOld = "1572403428006";
                String timestampPasteOld = "1672403428006";
                //int resultTimestampCompare = compareTimestamps(String.valueOf(timestamp), timestampPaste);
                int resultTimestampCompare = compareTimestamps(timestampPasteOld, timestampPaste);
                String resultTimestampCompareString = "undefined";
                if (resultTimestampCompare == 0) resultTimestampCompareString = "equal";
                if (resultTimestampCompare == -1) resultTimestampCompareString = "newer";
                if (resultTimestampCompare == 1) resultTimestampCompareString = "older";
                Toast.makeText(InternalStorageActivity.this, "Paste Timestamp is " + resultTimestampCompareString, Toast.LENGTH_LONG).show();
            }
        });

        loadEncrypted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "load encrypted");
                String filename = "test";
                InternalStorageUtils internalStorageUtils = new InternalStorageUtils(view.getContext());
                String encryptedContent = internalStorageUtils.loadPasteInternal(filename, true, "1672577506489");
                EncryptionUtils encryptionUtils = new EncryptionUtils();
                if (!TextUtils.isEmpty(encryptedContent)) {
                    // strip off the first two lines
                    String rawEncryptedContent = getRawContent2Lines(encryptedContent);
                    System.out.println("rawEncryptedContent:" + rawEncryptedContent + "###");
                    String unencryptedContent = encryptionUtils.doDecryptionAesGcmPbkdf2(
                            "1234".toCharArray(), rawEncryptedContent
                    );
                    if (TextUtils.isEmpty(unencryptedContent)) {
                        content.setText("could not decrypt content");
                    } else {
                        content.setText(unencryptedContent);
                    }
                }
            }
        });

        generateTestPastes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "generate test pastes");
                String password = "1234";
                for (int i = 1; i < 3; i++) {
                    String pasteTitleString = "Test paste " + i;
                    String pasteTextString = "This is the text for paste " + i;
                    long timestamp = new Date().getTime();
                    boolean pasteIsPrivate = false;
                    boolean pasteIsEncrypted = false;

                    InternalStorageUtils internalStorageUtils = new InternalStorageUtils(view.getContext());
                    // write an unencrypted string
                    boolean writeSuccess = internalStorageUtils.writePasteInternal(
                            pasteTitleString,
                            pasteTextString,
                            String.valueOf(timestamp),
                            pasteIsEncrypted,
                            pasteIsPrivate,
                            URL_DEFAULT);

                    pasteIsPrivate = true;
                    timestamp = new Date().getTime();
                    writeSuccess = internalStorageUtils.writePasteInternal(
                            pasteTitleString,
                            pasteTextString,
                            String.valueOf(timestamp),
                            pasteIsEncrypted,
                            pasteIsPrivate,
                            "https://pastebin.com/asdfg");

                    pasteIsPrivate = false;
                    pasteIsEncrypted = true;
                    timestamp = new Date().getTime();
                    EncryptionUtils encryptionUtils = new EncryptionUtils();
                    String ciphertext = encryptionUtils.doEncryptionAesGcmPbkdf2(password.toCharArray(), pasteTextString.getBytes(StandardCharsets.UTF_8));
                    internalStorageUtils = new InternalStorageUtils(view.getContext());
                    writeSuccess = internalStorageUtils.writePasteInternal(
                            pasteTitleString,
                            ciphertext,
                            String.valueOf(timestamp),
                            pasteIsEncrypted,
                            pasteIsPrivate,
                            URL_DEFAULT);

                    pasteIsPrivate = true;
                    timestamp = new Date().getTime();
                    internalStorageUtils = new InternalStorageUtils(view.getContext());
                    writeSuccess = internalStorageUtils.writePasteInternal(
                            pasteTitleString,
                            ciphertext,
                            String.valueOf(timestamp),
                            pasteIsEncrypted,
                            pasteIsPrivate,
                            "https://pastebin.com/asdfg");
                }
                Toast.makeText(InternalStorageActivity.this, "test pastes created", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * This method strips off the first line of content
     *
     * @param content
     * @return the content excluding the first line
     */
    private String getRawContent(@NonNull String content) {
        return content.substring(content.indexOf('\n') + 1);
    }

    /**
     * This method strips off the first two lines of content
     *
     * @param content
     * @return the content excluding the first two lines
     */
    private String getRawContent2Lines(@NonNull String content) {
        String tempContent = content.substring(content.indexOf('\n') + 1);
        return tempContent.substring(tempContent.indexOf('\n') + 1);
    }


    /**
     * This method compares two timestamps
     * if both are equals it return 0
     * if a < b it returns - 1
     * if a > b it returns 1
     * if a not digits only it returns 90
     * if b not digits only it returns 91
     * some other errors it returns 92
     */
    private int compareTimestamps(@NonNull String a, @NonNull String b) {
        if (TextUtils.isEmpty(a)) {
            Log.e(TAG, "timestamp A is empty");
            return 90;
        }
        if (TextUtils.isEmpty(b)) {
            Log.e(TAG, "timestamp B is empty");
            return 91;
        }
        if (!TextUtils.isDigitsOnly(a)) {
            Log.e(TAG, "timestamp A is not digits only");
            return 90;
        }
        if (!TextUtils.isDigitsOnly(a)) {
            Log.e(TAG, "timestamp A is not digits only");
            return 91;
        }
        long timestampA = Long.parseLong(a);
        long timestampB = Long.parseLong(b);
        if (timestampA == timestampB) return 0;
        if (timestampA < timestampB) return -1;
        if (timestampA > timestampB) return 1;
        return 92;
    }
}