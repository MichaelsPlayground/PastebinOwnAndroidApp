package de.androidcrypto.pastebinownandroidapp;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.jpaste.exceptions.PasteException;
import org.jpaste.pastebin.PastebinLink;
import org.jpaste.pastebin.PastebinPaste;
import org.jpaste.pastebin.account.PastebinAccount;
import org.jpaste.pastebin.exceptions.LoginException;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * This class is writing the paste to internal storage only, NO publishing
 */

public class WritePasteActivity extends AppCompatActivity {
    private static final String TAG = "WritePaste";

    SwitchMaterial pastePrivate, pasteEncrypted;
    com.google.android.material.textfield.TextInputEditText pasteTitle;
    com.google.android.material.textfield.TextInputEditText pasteText;
    Button submit;

    private static final String URL_DEFAULT = "https://pastebin.com/xxx";
    private static final int MINIMAL_PASSPHRASE_LENGTH = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_paste);

        pastePrivate = findViewById(R.id.swWritePastePrivate);
        pasteEncrypted = findViewById(R.id.swWritePasteEncrypted);
        pasteTitle = findViewById(R.id.etWritePasteTitle);
        pasteText = findViewById(R.id.etWritePasteText);
        submit = findViewById(R.id.btnWritePasteSubmit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "submit clicked");
                String pasteTitleString = pasteTitle.getText().toString();
                String pasteTextString = pasteText.getText().toString();
                boolean pasteIsPrivate = pastePrivate.isChecked();
                boolean pasteIsEncrypted = pasteEncrypted.isChecked();

                if (TextUtils.isEmpty(pasteTitleString)) {
                    Log.e(TAG, "the title is empty");
                    Snackbar snackbar = Snackbar.make(view, "Please enter a title", Snackbar.LENGTH_LONG);
                    snackbar.setBackgroundTint(ContextCompat.getColor(WritePasteActivity.this, R.color.red));
                    snackbar.show();
                    return;
                }
                if (TextUtils.isEmpty(pasteTextString)) {
                    Log.e(TAG, "the text is empty");
                    Snackbar snackbar = Snackbar.make(view, "Please enter a text", Snackbar.LENGTH_LONG);
                    snackbar.setBackgroundTint(ContextCompat.getColor(WritePasteActivity.this, R.color.red));
                    snackbar.show();
                    return;
                }
                // try to send the paste, then try to save the data

                if (pasteIsPrivate) {
                    Log.i(TAG, "save a private paste");
                } else {
                    Log.i(TAG, "save a public paste");
                }
                if (pasteIsEncrypted) {
                    Log.i(TAG, "the paste is encrypted");
                } else {
                    Log.i(TAG, "the paste is unencrypted");
                }

                // get the same timestamp for pastebin.com and internal storage
                long timestamp = new Date().getTime();

                if (pasteIsEncrypted) {
                    savePasswordProtectedContent(
                            view,
                            pasteTitleString,
                            pasteTextString,
                            String.valueOf(timestamp),
                            pasteIsPrivate,
                            URL_DEFAULT);
                } else {

                    // now save the paste in internal storage
                    InternalStorageUtils internalStorageUtils = new InternalStorageUtils(view.getContext());
                    // write an unencrypted string
                    boolean writeSuccess = internalStorageUtils.writePasteInternal(
                            pasteTitleString,
                            pasteTextString,
                            String.valueOf(timestamp),
                            false,
                            pasteIsPrivate,
                            URL_DEFAULT);
                    Log.i(TAG, "unencrypted writeSuccess: " + writeSuccess);
                    if (writeSuccess) {
                        Snackbar snackbar = Snackbar.make(view, "The paste was written to internal storage", Snackbar.LENGTH_SHORT);
                        snackbar.setBackgroundTint(ContextCompat.getColor(WritePasteActivity.this, R.color.green));
                        snackbar.show();
                    } else {
                        Snackbar snackbar = Snackbar.make(view, "Error during writing the paste to internal storage", Snackbar.LENGTH_LONG);
                        snackbar.setBackgroundTint(ContextCompat.getColor(WritePasteActivity.this, R.color.red));
                        snackbar.show();
                    }
                    // clean data
                    pasteTitle.setText("");
                    pasteText.setText("");
                }
            }
        });
    }

    /*
    savePasswordProtectedContent(
                            view,
                            pasteTitleString,
                            pasteTextString,
                            String.valueOf(timestamp),
                            false,
                            pastePrivate.isChecked(),
                            "https://pastebin.com/xxx");
     */

    private void savePasswordProtectedContent(View view, String pasteTitleString, String pasteTextString, String timestampString, boolean pasteIsPrivate, String url) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(WritePasteActivity.this);
        String titleString = "Provide the encryption passphrase";
        String messageString = "\nPlease enter a (minimum) 4 character long passphrase and press on\nSAVE DOCUMENT.";
        String hintString = "  passphrase";

        alertDialog.setTitle(titleString);
        alertDialog.setMessage(messageString);
        final EditText passphrase = new EditText(WritePasteActivity.this);
        passphrase.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.round_rect_shape, null));
        passphrase.setHint(hintString);
        passphrase.setPadding(50, 20, 50, 20);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp1.setMargins(36, 36, 36, 36);
        passphrase.setLayoutParams(lp1);
        RelativeLayout container = new RelativeLayout(WritePasteActivity.this);
        RelativeLayout.LayoutParams rlParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        container.setLayoutParams(rlParams);
        container.addView(passphrase);
        alertDialog.setView(container);
        alertDialog.setPositiveButton("SAVE DOCUMENT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int passphraseLength = passphrase.length();
                char[] password = new char[passphraseLength];
                passphrase.getText().getChars(0, passphraseLength, password, 0);
                // test on password length
                if (passphraseLength < MINIMAL_PASSPHRASE_LENGTH) {
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "The passphrase is too short", Snackbar.LENGTH_LONG);
                    snackbar.setBackgroundTint(ContextCompat.getColor(WritePasteActivity.this, R.color.red));
                    snackbar.show();
                    return;
                }
                EncryptionUtils encryptionUtils = new EncryptionUtils();
                String ciphertext = encryptionUtils.doEncryptionAesGcmPbkdf2(password, pasteTextString.getBytes(StandardCharsets.UTF_8));
                if (TextUtils.isEmpty(ciphertext)) {
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Could not encrypt the paste", Snackbar.LENGTH_LONG);
                    snackbar.setBackgroundTint(ContextCompat.getColor(WritePasteActivity.this, R.color.red));
                    snackbar.show();
                    return;
                } else {
                    // now save the paste in internal storage
                    InternalStorageUtils internalStorageUtils = new InternalStorageUtils(view.getContext());
                    boolean writeSuccess = internalStorageUtils.writePasteInternal(
                            pasteTitleString,
                            ciphertext,
                            timestampString,
                            true,
                            pasteIsPrivate,
                            url);
                    Log.i(TAG, "encrypted writeSuccess: " + writeSuccess);
                    if (writeSuccess) {
                        Snackbar snackbar = Snackbar.make(view, "The paste was written to internal storage", Snackbar.LENGTH_SHORT);
                        snackbar.setBackgroundTint(ContextCompat.getColor(WritePasteActivity.this, R.color.green));
                        snackbar.show();
                    } else {
                        Snackbar snackbar = Snackbar.make(view, "Error during writing the paste to internal storage", Snackbar.LENGTH_LONG);
                        snackbar.setBackgroundTint(ContextCompat.getColor(WritePasteActivity.this, R.color.red));
                        snackbar.show();
                    }

                    // clean data
                    pasteTitle.setText("");
                    pasteText.setText("");

                    return;
                }
            }
        });
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Save an encrypted paste was cancelled", Snackbar.LENGTH_LONG);
                snackbar.setBackgroundTint(ContextCompat.getColor(WritePasteActivity.this, R.color.red));
                snackbar.show();
                return;
            }
        });
        alertDialog.show();
    }

    private void savePasswordProtectedContentOld(View view, PastebinAccount account, int visibility, String contentHeader, String pasteTitleString, String pasteTextString, String timestampString) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(WritePasteActivity.this);
        String titleString = "Provide the encryption passphrase";
        String messageString = "\nPlease enter a (minimum) 4 character long passphrase and press on\nSAVE DOCUMENT.";
        String hintString = "  passphrase";

        alertDialog.setTitle(titleString);
        alertDialog.setMessage(messageString);
        final EditText passphrase = new EditText(WritePasteActivity.this);
        passphrase.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.round_rect_shape, null));
        passphrase.setHint(hintString);
        passphrase.setPadding(50, 20, 50, 20);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp1.setMargins(36, 36, 36, 36);
        passphrase.setLayoutParams(lp1);
        RelativeLayout container = new RelativeLayout(WritePasteActivity.this);
        RelativeLayout.LayoutParams rlParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        container.setLayoutParams(rlParams);
        container.addView(passphrase);
        alertDialog.setView(container);
        alertDialog.setPositiveButton("SAVE DOCUMENT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int passphraseLength = passphrase.length();
                char[] password = new char[passphraseLength];
                passphrase.getText().getChars(0, passphraseLength, password, 0);
                // test on password length
                if (passphraseLength < MINIMAL_PASSPHRASE_LENGTH) {
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "The passphrase is too short", Snackbar.LENGTH_LONG);
                    snackbar.setBackgroundTint(ContextCompat.getColor(WritePasteActivity.this, R.color.red));
                    snackbar.show();
                    return;
                }
                EncryptionUtils encryptionUtils = new EncryptionUtils();
                String ciphertext = encryptionUtils.doEncryptionAesGcmPbkdf2(password, pasteTextString.getBytes(StandardCharsets.UTF_8));
                if (TextUtils.isEmpty(ciphertext)) {
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Could not encrypt the paste", Snackbar.LENGTH_LONG);
                    snackbar.setBackgroundTint(ContextCompat.getColor(WritePasteActivity.this, R.color.red));
                    snackbar.show();
                    return;
                } else {
                    PastebinPaste paste = new PastebinPaste(account);
                    paste.setContents(
                            contentHeader +
                                    timestampString +
                                    pasteTextString);
                    paste.setPasteTitle(pasteTitleString);
                    paste.setVisibility(visibility);
                    // push paste
                    PastebinLink link = null;
                    try {
                        link = paste.paste();
                    } catch (PasteException e) {
                        e.printStackTrace();
                        Log.e(TAG, e.getMessage());
                        Snackbar snackbar = Snackbar.make(view, "Error during send a paste to Pastebin.com, aborted", Snackbar.LENGTH_LONG);
                        snackbar.setBackgroundTint(ContextCompat.getColor(WritePasteActivity.this, R.color.red));
                        snackbar.show();
                        return;
                    }
                    Snackbar snackbar = Snackbar.make(view, "The paste was sent successfully to Pastebin.com", Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(ContextCompat.getColor(WritePasteActivity.this, R.color.green));
                    snackbar.show();
                    // now save the paste in internal storage
                    InternalStorageUtils internalStorageUtils = new InternalStorageUtils(view.getContext());
                    // check if paste is private
                    boolean pasteIsPrivate = false;
                    if (visibility == PastebinPaste.VISIBILITY_PRIVATE) pasteIsPrivate = true;
                    // write an unencrypted string

                    // todo get pastebin.com url

                    boolean writeSuccess = internalStorageUtils.writePasteInternal(
                            pasteTitleString,
                            pasteTextString,
                            timestampString,
                            true,
                            pasteIsPrivate,
                            "https://pastebin.com/xxx");
                    Log.i(TAG, "encrypted writeSuccess: " + writeSuccess);

                    // clean data
                    pasteTitle.setText("");
                    pasteText.setText("");

                    return;
                }
            }
        });
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Save an encrypted paste was cancelled", Snackbar.LENGTH_LONG);
                snackbar.setBackgroundTint(ContextCompat.getColor(WritePasteActivity.this, R.color.red));
                snackbar.show();
                return;
            }
        });
        alertDialog.show();
    }

}