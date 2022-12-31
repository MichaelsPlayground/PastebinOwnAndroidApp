package de.androidcrypto.pastebinownandroidapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.jpaste.exceptions.PasteException;
import org.jpaste.pastebin.PastebinLink;
import org.jpaste.pastebin.PastebinPaste;
import org.jpaste.pastebin.account.PastebinAccount;
import org.jpaste.pastebin.exceptions.LoginException;

import java.nio.charset.StandardCharsets;
import java.util.Date;

public class SendPasteActivity extends AppCompatActivity {
    private static final String TAG = "SendPaste";

    SwitchMaterial pastePrivate, pasteEncrypted;
    com.google.android.material.textfield.TextInputEditText pasteTitle;
    com.google.android.material.textfield.TextInputEditText pasteText;
    Button submit;

    private static final int MINIMAL_PASSPHRASE_LENGTH = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_paste);

        pastePrivate = findViewById(R.id.swSendPastePrivate);
        pasteEncrypted = findViewById(R.id.swSendPasteEncrypted);
        pasteTitle = findViewById(R.id.etSendPasteTitle);
        pasteText = findViewById(R.id.etSendPasteText);
        submit = findViewById(R.id.btnSendPasteSubmit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "submit clicked");
                String pasteTitleString = pasteTitle.getText().toString();
                String pasteTextString = pasteText.getText().toString();
                if (TextUtils.isEmpty(pasteTitleString)) {
                    Log.e(TAG, "the title is empty");
                    Snackbar snackbar = Snackbar.make(view, "Please enter a title", Snackbar.LENGTH_LONG);
                    snackbar.setBackgroundTint(ContextCompat.getColor(SendPasteActivity.this, R.color.red));
                    snackbar.show();
                    return;
                }
                if (TextUtils.isEmpty(pasteTextString)) {
                    Log.e(TAG, "the text is empty");
                    Snackbar snackbar = Snackbar.make(view, "Please enter a text", Snackbar.LENGTH_LONG);
                    snackbar.setBackgroundTint(ContextCompat.getColor(SendPasteActivity.this, R.color.red));
                    snackbar.show();
                    return;
                }
                // try to send the paste, then try to save the data
                int visibility;
                if (pastePrivate.isChecked()) {
                    visibility = PastebinPaste.VISIBILITY_PRIVATE;

                    Log.i(TAG, "save a private paste");
                } else {
                    visibility = PastebinPaste.VISIBILITY_PUBLIC;

                    Log.i(TAG, "save a public paste");
                }
                String contentHeader = "";
                String contentHeaderType = "";
                if (pastePrivate.isChecked()) {
                    contentHeaderType = InternalStorageUtils.CONTENT_TYPE_PRIVATE;
                    Log.i(TAG, "the paste is private");
                } else {
                    contentHeaderType = InternalStorageUtils.CONTENT_TYPE_PUBLIC;
                    Log.i(TAG, "the paste is public");
                }
                if (pasteEncrypted.isChecked()) {
                    contentHeader = InternalStorageUtils.ENCRYPTED_CONTENT + contentHeaderType + "\n";
                    Log.i(TAG, "the paste is encrypted");
                } else {
                    contentHeader = InternalStorageUtils.UNENCRYPTED_CONTENT + contentHeaderType + "\n";
                    Log.i(TAG, "the paste is unencrypted");
                }
                // create paste, for this we need to login and get an account
                StorageUtils su = new StorageUtils(view.getContext());
                boolean credentialsAreSet = su.checkForCredentials();
                if (!credentialsAreSet) {
                    Log.d(TAG, "cannot login as not all credentials are set");
                    Snackbar snackbar = Snackbar.make(view, "Did you set the developer key, user name and password ? aborted", Snackbar.LENGTH_LONG);
                    snackbar.setBackgroundTint(ContextCompat.getColor(SendPasteActivity.this, R.color.red));
                    snackbar.show();
                    return;
                }
                PastebinAccount account = new PastebinAccount(su.getDeveloperKey(), su.getUserName(), su.getUserPassword());
                // fetches an user session id
                try {
                    account.login();
                } catch (LoginException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                    Snackbar snackbar = Snackbar.make(view, "Error during login to Pastebin.com, aborted", Snackbar.LENGTH_LONG);
                    snackbar.setBackgroundTint(ContextCompat.getColor(SendPasteActivity.this, R.color.red));
                    snackbar.show();
                    return;
                }
                // get the same timestamp for pastebin.com and internal storage
                long timestamp = new Date().getTime();
                String timestampString = InternalStorageUtils.TIMESTAMP_CONTENT + timestamp + "\n";


                // todo work on encrypted pastes
                if (pasteEncrypted.isChecked()) {
                    savePasswordProtectedContent(
                            view,
                            account,
                            visibility,
                            contentHeader,
                            pasteTitleString,
                            pasteTextString,
                            timestampString);
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
                        snackbar.setBackgroundTint(ContextCompat.getColor(SendPasteActivity.this, R.color.red));
                        snackbar.show();
                        return;
                    }
                    Snackbar snackbar = Snackbar.make(view, "The paste was sent successfully to Pastebin.com", Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(ContextCompat.getColor(SendPasteActivity.this, R.color.green));
                    snackbar.show();

                    // now save the paste in internal storage
                    InternalStorageUtils internalStorageUtils = new InternalStorageUtils(view.getContext());
                    // write an unencrypted string
                    boolean writeSuccess = internalStorageUtils.writePasteInternal(
                            pasteTitleString,
                            pasteTextString,
                            String.valueOf(timestamp),
                            false,
                            pastePrivate.isChecked());
                    Log.i(TAG, "unencrypted writeSuccess: " + writeSuccess);

                    // clean data
                    pasteTitle.setText("");
                    pasteText.setText("");
                }
            }
        });

    }


    private void savePasswordProtectedContent(View view, PastebinAccount account, int visibility, String contentHeader, String pasteTitleString, String pasteTextString, String timestampString) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(SendPasteActivity.this);
        String titleString = "Provide the encryption passphrase";
        String messageString = "\nPlease enter a (minimum) 4 character long passphrase and press on\nSAVE DOCUMENT.";
        String hintString = "  passphrase";

        alertDialog.setTitle(titleString);
        alertDialog.setMessage(messageString);
        final EditText passphrase = new EditText(SendPasteActivity.this);
        passphrase.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.round_rect_shape, null));
        passphrase.setHint(hintString);
        passphrase.setPadding(50, 20, 50, 20);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp1.setMargins(36, 36, 36, 36);
        passphrase.setLayoutParams(lp1);
        RelativeLayout container = new RelativeLayout(SendPasteActivity.this);
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
                    snackbar.setBackgroundTint(ContextCompat.getColor(SendPasteActivity.this, R.color.red));
                    snackbar.show();
                    return;
                }
                EncryptionUtils encryptionUtils = new EncryptionUtils();
                String ciphertext = encryptionUtils.doEncryptionAesGcmPbkdf2(password, pasteTextString.getBytes(StandardCharsets.UTF_8));
                if (TextUtils.isEmpty(ciphertext)) {
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Could not encrypt the paste", Snackbar.LENGTH_LONG);
                    snackbar.setBackgroundTint(ContextCompat.getColor(SendPasteActivity.this, R.color.red));
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
                        snackbar.setBackgroundTint(ContextCompat.getColor(SendPasteActivity.this, R.color.red));
                        snackbar.show();
                        return;
                    }
                    Snackbar snackbar = Snackbar.make(view, "The paste was sent successfully to Pastebin.com", Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(ContextCompat.getColor(SendPasteActivity.this, R.color.green));
                    snackbar.show();
                    // now save the paste in internal storage
                    InternalStorageUtils internalStorageUtils = new InternalStorageUtils(view.getContext());
                    // check if paste is private
                    boolean pasteIsPrivate = false;
                    if (visibility == PastebinPaste.VISIBILITY_PRIVATE) pasteIsPrivate = true;
                    // write an unencrypted string
                    boolean writeSuccess = internalStorageUtils.writePasteInternal(
                            pasteTitleString,
                            pasteTextString,
                            timestampString,
                            true,
                            pasteIsPrivate);
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
                snackbar.setBackgroundTint(ContextCompat.getColor(SendPasteActivity.this, R.color.red));
                snackbar.show();
                return;
            }
        });
        alertDialog.show();
    }

}