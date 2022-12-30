package de.androidcrypto.pastebinownandroidapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.jpaste.exceptions.PasteException;
import org.jpaste.pastebin.PastebinLink;
import org.jpaste.pastebin.PastebinPaste;
import org.jpaste.pastebin.account.PastebinAccount;
import org.jpaste.pastebin.exceptions.LoginException;

import java.util.Date;

public class SendPasteActivity extends AppCompatActivity {
    private static final String TAG = "SendPaste";

    SwitchMaterial pastePrivate, pasteEncrypted;
    com.google.android.material.textfield.TextInputEditText pasteTitle;
    com.google.android.material.textfield.TextInputEditText pasteText;
    Button submit;

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
                if (pasteEncrypted.isChecked()) {
                    contentHeader = InternalStorageUtils.ENCRYPTED_CONTENT + "\n";
                    Log.i(TAG, "the paste is encrypted");
                } else {
                    contentHeader = InternalStorageUtils.UNENCRYPTED_CONTENT + "\n";
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
                Date pasteDate = link.getPasteDate();
                System.out.println("*** pasteDate: " + pasteDate);

                String pasteLink = link.getLink().toString();
                String pasteKey = link.getKey();
                System.out.println(link.getLink());
                //getUrlData.setText("paste is posted with this key " + pasteKey + " and URL: " + pasteLink);
                Snackbar snackbar = Snackbar.make(view, "The paste was sent successfully to Pastebin.com", Snackbar.LENGTH_SHORT);
                snackbar.setBackgroundTint(ContextCompat.getColor(SendPasteActivity.this, R.color.green));
                snackbar.show();

                // todo work on encrypted pastes

                // now save the paste in internal storage
                InternalStorageUtils internalStorageUtils = new InternalStorageUtils(view.getContext());
                // write an unencrypted string
                boolean writeSuccess = internalStorageUtils.writePasteInternal(
                        pasteTitleString,
                        pasteTextString,
                        String.valueOf(timestamp),
                        pasteEncrypted.isChecked());
                Log.i(TAG, "writeSuccess: " + writeSuccess);

                // clean data
                pasteTitle.setText("");
                pasteText.setText("");
            }
        });

    }


}