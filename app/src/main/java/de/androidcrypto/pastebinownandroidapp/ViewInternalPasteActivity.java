package de.androidcrypto.pastebinownandroidapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.snackbar.Snackbar;

import org.jpaste.pastebin.PastebinPaste;

import java.nio.charset.StandardCharsets;

public class ViewInternalPasteActivity extends AppCompatActivity {

    private static final String TAG = "ViewInternalPasteAct";

    com.google.android.material.textfield.TextInputEditText pasteTitle;
    com.google.android.material.textfield.TextInputEditText pasteText;
    com.google.android.material.button.MaterialButton sync;
    Button save, loadUnencrypted, loadEncrypted;

    private String rawContent; // may be encrypted
    String visibilityType;
    String contentType;

    private static final int MINIMAL_PASSPHRASE_LENGTH = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_internal_paste);

        pasteTitle = findViewById(R.id.etVIPPasteTitle);
        pasteText = findViewById(R.id.etVIPContent);
        sync = findViewById(R.id.btnVIPSync);
        save = findViewById(R.id.btnVIPSave);
        loadUnencrypted = findViewById(R.id.btnVIPLoadUnencrypted);
        loadEncrypted = findViewById(R.id.btnVIPLoadEncrypted);
        
        Intent intent = getIntent();
        String filename = intent.getStringExtra("FILENAME");
        String filenameStorage = intent.getStringExtra("FILENAME_STORAGE");
        String timestamp = intent.getStringExtra("TIMESTAMP");
        visibilityType = intent.getStringExtra("VISIBILITY_TYPE"); // PUBLIC, PRIVATE
        contentType = intent.getStringExtra("CONTENT_TYPE"); // UNENCRYPTED, ENCRYPTED
        //intent.putExtra("SYNC_STATUS", "UNSYNCED");
        String syncStatus = intent.getStringExtra("SYNC_STATUS");
        if (syncStatus.equals("UNSYNCED")) {
            sync.setVisibility(View.VISIBLE);
        }

        // todo check that all data are provided
        Log.d(TAG, "filenameStorage received: " + filenameStorage);
        if (!TextUtils.isEmpty(filename)) {
            System.out.println("VIPA filename: " + filename);
            //String filename = "test";
            InternalStorageUtils internalStorageUtils = new InternalStorageUtils(getApplicationContext());
            String contentFullString = internalStorageUtils.loadPasteInternal(filenameStorage);
            pasteTitle.setText(filename);
            pasteText.setText(getRawContent2Lines(contentFullString));
            // get the actual timestamp
            //long timestamp = new Date().getTime();
            //String timestampPaste = "1672403428006";
            //String timestampPaste = "1772403428006";
            // the method to retrieve the timestamp from a stored paste is in InternalStorageUtils
            // to use the method we need to strip off the first line of the content
            String tempContent = getRawContent(contentFullString);
            rawContent = getRawContent2Lines(contentFullString);
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
            Toast.makeText(ViewInternalPasteActivity.this, "Paste Timestamp is " + resultTimestampCompareString, Toast.LENGTH_LONG).show();

        }

        sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "sync with Pastebin.com");
                // check that an internet connect is available
                if (!PastebinLoginUtils.deviceIsOnline()) {
                    Log.e(TAG, "the user is not logged in or an internet connect is not available, sync aborted");
                    Snackbar snackbar = Snackbar.make(view, "Please login to proceed, aborted", Snackbar.LENGTH_LONG);
                    snackbar.setBackgroundTint(ContextCompat.getColor(ViewInternalPasteActivity.this, R.color.red));
                    snackbar.show();
                    return;
                }
                // rawContent holds the data
                // saveUnprotectedContent(View view, PastebinAccount account, int visibility, String contentHeader, String pasteTitleString, String pasteTextString, String timestampString) {
                // savePasswordProtectedContent(View view, PastebinAccount account, int visibility, String contentHeader, String pasteTitleString, String pasteTextString, String timestampString) {
                // check for visibilityType (PUBLIC or PRIVATE)
                int pasteVisibility = PastebinPaste.VISIBILITY_PUBLIC;
                if (visibilityType.equals(InternalStorageUtils.VISIBILITY_TYPE_PRIVATE)) {
                    pasteVisibility = PastebinPaste.VISIBILITY_PRIVATE;
                }
                // check for contentType (ENCRYPTED or UNENCRYPTED)
                boolean pasteIsEncrypted = false;
                if (contentType.equals(InternalStorageUtils.ENCRYPTED_CONTENT)) {
                    pasteIsEncrypted = true;
                }
                PasteUtils pasteUtils = new PasteUtils(getApplicationContext());
                //String pasteUrl = pasteUtils.saveUnprotectedContent(this, pasteVisibility,pa)
            }
        });

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
                //long timestamp = new Date().getTime();
                // use a dummy timestamp
                long timestamp = Long.parseLong("1672577506489");
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
                        "https://pastebin.com/xxx");
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
                pasteText.setText(getRawContent2Lines(contentFullString));
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
                Toast.makeText(ViewInternalPasteActivity.this, "Paste Timestamp is " + resultTimestampCompareString, Toast.LENGTH_LONG).show();
            }
        });

        loadEncrypted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "load encrypted");

                // simple anonymus alert dialog
                // source: https://stackoverflow.com/a/47978118/8166854
                new AlertDialog.Builder(view.getContext()).setTitle("Paste is encrypted")
                        .setMessage("Do you want to decrypt it ?")
                        // show in device language instead of fixed string
                        // android.R.string.yes
                        //.setPositiveButton("YES",
                        .setPositiveButton(android.R.string.yes,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Toast.makeText(ViewInternalPasteActivity.this, "type in the password to decrypt the content", Toast.LENGTH_SHORT).show();
                                        loadPasswordProtectedContent(view);
                                        // Perform Action & Dismiss dialog
                                        dialog.dismiss();
                                    }
                                })
                        // android.R.string.no
                        //.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing
                                Toast.makeText(ViewInternalPasteActivity.this, "no decryption", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        })
                        .create()
                        .show();

                String demo = "cQ4Gg6K9nFCCKAW6MtQT88OukB/8cfbr3ZaiyLkR34/nZ3ECq0C778NP1WSKmG44ULa3FI3n8CVaXJxbRR4vmWRS3oO+sIOEDOm4WZktlEm4LCDBpZzNPg==";
                System.out.println("demo: " + demo);
                EncryptionUtils encryptionUtils = new EncryptionUtils();
                String decryptedtxt = encryptionUtils.doDecryptionAesGcmPbkdf2("1234".toCharArray(), demo);
                System.out.println("decry: " + decryptedtxt);


            }
        });

    }

    /**
     * This method strips off the first line of content
     * @param content
     * @return the content excluding the first line
     */
    private String getRawContent (@NonNull String content) {
        return content.substring(content.indexOf('\n')+1);
    }

    /**
     * This method strips off the first two lines of content
     * @param content
     * @return the content excluding the first two lines
     */
    private String getRawContent2Lines (@NonNull String content) {
        String tempContent = content.substring(content.indexOf('\n')+1);
        return tempContent.substring(tempContent.indexOf('\n')+1);
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

    private void loadPasswordProtectedContent(View view) {
        Context context = ViewInternalPasteActivity.this;
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ViewInternalPasteActivity.this);
        String titleString = "Provide the encryption passphrase";
        String messageString = "\nPlease enter a (minimum) 4 character long passphrase and press on\nSAVE DOCUMENT.";
        String hintString = "  passphrase";

        alertDialog.setTitle(titleString);
        alertDialog.setMessage(messageString);
        final EditText passphrase = new EditText(context);
        passphrase.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.round_rect_shape, null));
        passphrase.setHint(hintString);
        passphrase.setPadding(50, 20, 50, 20);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp1.setMargins(36, 36, 36, 36);
        passphrase.setLayoutParams(lp1);
        RelativeLayout container = new RelativeLayout(context);
        RelativeLayout.LayoutParams rlParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        container.setLayoutParams(rlParams);
        container.addView(passphrase);
        alertDialog.setView(container);
        alertDialog.setPositiveButton("DECRYPT DOCUMENT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int passphraseLength = passphrase.length();
                char[] password = new char[passphraseLength];
                passphrase.getText().getChars(0, passphraseLength, password, 0);
                // test on password length
                if (passphraseLength < MINIMAL_PASSPHRASE_LENGTH) {
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "The passphrase is too short", Snackbar.LENGTH_LONG);
                    snackbar.setBackgroundTint(ContextCompat.getColor(context, R.color.red));
                    snackbar.show();
                    return;
                }
                EncryptionUtils encryptionUtils = new EncryptionUtils();
                System.out.println("### rawContent: " + rawContent);
                String decryptedtext = encryptionUtils.doDecryptionAesGcmPbkdf2(password, rawContent);
                if (TextUtils.isEmpty(decryptedtext)) {
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Could not decrypt the paste", Snackbar.LENGTH_LONG);
                    snackbar.setBackgroundTint(ContextCompat.getColor(context, R.color.red));
                    snackbar.show();
                    return;
                } else {
                    // now show the paste
                    pasteText.setText(decryptedtext);
                    Log.i(TAG, "decrypted loadSuccess");
                    return;
                }
            }
        });
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "load an encrypted paste was cancelled", Snackbar.LENGTH_LONG);
                snackbar.setBackgroundTint(ContextCompat.getColor(context, R.color.red));
                snackbar.show();
                return;
            }
        });
        alertDialog.show();
    }
}