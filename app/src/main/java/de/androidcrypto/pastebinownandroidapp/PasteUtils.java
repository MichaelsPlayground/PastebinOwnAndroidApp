package de.androidcrypto.pastebinownandroidapp;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.snackbar.Snackbar;

import org.jpaste.exceptions.PasteException;
import org.jpaste.pastebin.PastebinLink;
import org.jpaste.pastebin.PastebinPaste;
import org.jpaste.pastebin.account.PastebinAccount;

import java.nio.charset.StandardCharsets;

public class PasteUtils {

    /**
     * This class is responsible to write and load data from Pastebin.com
     */

    private final String TAG = "PasteUtils";
    private static final int MINIMAL_PASSPHRASE_LENGTH = 4;

    Context mContext;

    public PasteUtils(Context mContext) {
        this.mContext = mContext;
    }


    public String saveUnprotectedContent(View view, PastebinAccount account, int visibility, String contentHeader, String pasteTitleString, String pasteTextString, String timestampString) {
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
            snackbar.setBackgroundTint(ContextCompat.getColor(mContext, R.color.red));
            snackbar.show();
            return "";
        }
        Snackbar snackbar = Snackbar.make(view, "The paste was sent successfully to Pastebin.com", Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(ContextCompat.getColor(mContext, R.color.green));
        snackbar.show();
        return link.getLink().toString();
    }


    /**
     * The methods returns the url of the paste on success and "" if not
     * @param view
     * @param account
     * @param visibility
     * @param contentHeader
     * @param pasteTitleString
     * @param pasteTextString
     * @param timestampString
     * @return
     */

    public String savePasswordProtectedContent(View view, PastebinAccount account, int visibility, String contentHeader, String pasteTitleString, String pasteTextString, String timestampString) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        String titleString = "Provide the encryption passphrase";
        String messageString = "\nPlease enter a (minimum) 4 character long passphrase and press on\nSAVE DOCUMENT.";
        String hintString = "  passphrase";
        final String[] pasteLink = {""};

        alertDialog.setTitle(titleString);
        alertDialog.setMessage(messageString);
        final EditText passphrase = new EditText(mContext);
        passphrase.setBackground(ResourcesCompat.getDrawable(mContext.getResources(),R.drawable.round_rect_shape, null));
        passphrase.setHint(hintString);
        passphrase.setPadding(50, 20, 50, 20);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp1.setMargins(36, 36, 36, 36);
        passphrase.setLayoutParams(lp1);
        RelativeLayout container = new RelativeLayout(mContext);
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
                    Snackbar snackbar = Snackbar.make(view.findViewById(android.R.id.content), "The passphrase is too short", Snackbar.LENGTH_LONG);
                    snackbar.setBackgroundTint(ContextCompat.getColor(mContext, R.color.red));
                    snackbar.show();
                    return;
                }
                EncryptionUtils encryptionUtils = new EncryptionUtils();
                String ciphertext = encryptionUtils.doEncryptionAesGcmPbkdf2(password, pasteTextString.getBytes(StandardCharsets.UTF_8));
                if (TextUtils.isEmpty(ciphertext)) {
                    Snackbar snackbar = Snackbar.make(view.findViewById(android.R.id.content), "Could not encrypt the paste", Snackbar.LENGTH_LONG);
                    snackbar.setBackgroundTint(ContextCompat.getColor(mContext, R.color.red));
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
                        snackbar.setBackgroundTint(ContextCompat.getColor(mContext, R.color.red));
                        snackbar.show();
                        return;
                    }
                    Snackbar snackbar = Snackbar.make(view, "The paste was sent successfully to Pastebin.com", Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(ContextCompat.getColor(mContext, R.color.green));
                    snackbar.show();
                    pasteLink[0] = link.getLink().toString();
                    return;
                }
            }
        });
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Snackbar snackbar = Snackbar.make(view.findViewById(android.R.id.content), "Save an encrypted paste was cancelled", Snackbar.LENGTH_LONG);
                snackbar.setBackgroundTint(ContextCompat.getColor(mContext, R.color.red));
                snackbar.show();
                return;
            }
        });
        alertDialog.show();
        return pasteLink[0];
    }
}
