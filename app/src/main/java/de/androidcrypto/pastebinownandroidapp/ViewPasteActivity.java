package de.androidcrypto.pastebinownandroidapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import org.jpaste.utils.web.Post;
import org.jpaste.utils.web.Web;

import java.nio.charset.StandardCharsets;

public class ViewPasteActivity extends AppCompatActivity {

    private static final String TAG = "ViewPaste";
    TextView header;
    EditText viewPaste;

    private static final String API_GET_RAW_CONTENT = "https://pastebin.com/api/api_raw.php";
    private static final String API_PASTE_CONTENT_KEY = "https://pastebin.com/";
    private static final String ENCRYPTED_CONTENT = "ENCRYPTED CONTENT";
    private static final int MINIMAL_PASSPHRASE_LENGTH = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_paste);

        header = findViewById(R.id.tvViewPasteHeader);
        viewPaste = findViewById(R.id.etViewPaste);

        Log.i(TAG, "ViewPasteActivity started");
        // receive the link from intent
        Intent intent = getIntent();
        String pasteUrl = intent.getStringExtra("PASTE_URL");
        String userKey = intent.getStringExtra("USER_KEY");
        if (TextUtils.isEmpty(pasteUrl)) {
            Log.e(TAG, "the calling intent does not contain a pasteUrl so the activity returns to MainActivity");
            Intent returnIntent = new Intent(ViewPasteActivity.this, MainActivity.class);
            startActivity(returnIntent);
            finish();
        } else {
            // we received a pasteUrl, load the data

        }

        // the activity was called directly so it will return to MainActivity

        /*

        String sampleEncryptionString = "This is a sample encrypted paste.\n" +
                "This is using an AES-256 encryption in GCM mode.\n" +
                "The encryption key is derived by running a PBKDF2 key derivation using " +
                "(at minimum) 10000 iterations and a HMAC256SHA256 algorithm.";
        String samplePassphraseString = "1234";
        EncryptionUtils eu = new EncryptionUtils();

        String sampleCiphertextString = eu.doEncryptionAesGcmPbkdf2(
                samplePassphraseString.toCharArray(),
                sampleEncryptionString.getBytes(StandardCharsets.UTF_8));
        System.out.println("sampleCiphertextString:\n" + sampleCiphertextString);

        if (!TextUtils.isEmpty(pasteUrl)) {
            String headerString = "Paste from URL " + pasteUrl;
            header.setText(headerString);
            String content = getPasteContent(userKey, pasteUrl);
            boolean contentIsEncrypted = isContentEncrypted(content);
            if (contentIsEncrypted) {
                // todo decrypt the data, ask for a passphrase
                // todo this is simplified code, it strips off the first line only, decryption should follow
                loadPasswordPressed(content);
                //content = getEncryptedContent(content);
            }
            Log.i(TAG, "Content:\n" + content);
            viewPaste.setText(content);
        }

         */
    }

    private void loadPasswordPressed(String encryptedContent) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ViewPasteActivity.this);
        String titleString = "Provide the encryption passphrase";
        String messageString = "\nPlease enter a (minimum) 4 character long passphrase and press on\nOPEN DOCUMENT.";
        String hintString = "  passphrase";

        alertDialog.setTitle(titleString);
        alertDialog.setMessage(messageString);
        final EditText passphrase = new EditText(ViewPasteActivity.this);
        passphrase.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.round_rect_shape, null));
        passphrase.setHint(hintString);
        passphrase.setPadding(50, 20, 50, 20);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp1.setMargins(36, 36, 36, 36);
        passphrase.setLayoutParams(lp1);
        RelativeLayout container = new RelativeLayout(ViewPasteActivity.this);
        RelativeLayout.LayoutParams rlParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        container.setLayoutParams(rlParams);
        container.addView(passphrase);
        alertDialog.setView(container);
        alertDialog.setPositiveButton("OPEN DOCUMENT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int passphraseLength = passphrase.length();
                char[] password = new char[passphraseLength];
                passphrase.getText().getChars(0, passphraseLength, password, 0);
                // test on password length
                if (passphraseLength < MINIMAL_PASSPHRASE_LENGTH) {
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "The passphrase is too short", Snackbar.LENGTH_LONG);
                    snackbar.setBackgroundTint(ContextCompat.getColor(ViewPasteActivity.this, R.color.red));
                    snackbar.show();
                    return;
                }
                EncryptionUtils encryptionUtils = new EncryptionUtils();
                String ciphertext = getEncryptedContent(encryptedContent);

                // todo see InternalStorageActivity - loadEncryptedPaste

                String decryptedString = encryptionUtils.doDecryptionAesGcmPbkdf2(password, ciphertext);
                if (TextUtils.isEmpty(decryptedString)) {
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Could not decrypt (wrong passphrase ?)", Snackbar.LENGTH_LONG);
                    snackbar.setBackgroundTint(ContextCompat.getColor(ViewPasteActivity.this, R.color.red));
                    snackbar.show();
                    return;
                } else {
                    viewPaste.setText(decryptedString);
                    return;
                }
            }
        });
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Can not open the encrypted document", Snackbar.LENGTH_LONG);
                snackbar.setBackgroundTint(ContextCompat.getColor(ViewPasteActivity.this, R.color.red));
                snackbar.show();
                return;
            }
        });
        alertDialog.show();
    }

    private String getPasteContent(String userKey, String apiPasteKey) {
        String paste_key = apiPasteKey.replace(API_PASTE_CONTENT_KEY,"");
        // do it myself to get the content
        StorageUtils storageUtils = new StorageUtils(getApplicationContext());
        Post post = new Post();
        post.put("api_dev_key", storageUtils.getDeveloperKey());
        post.put("api_user_key", userKey);
        post.put("api_paste_key", paste_key);
        post.put("api_option", "show_paste");

        String response = Web.getContents(API_GET_RAW_CONTENT, post);
        if (TextUtils.isEmpty(response)) {
            Log.e(TAG, "Empty response from login API server");
            return "";
        }
        if (response.toLowerCase().startsWith("bad")) {
            Log.e(TAG, "Failed to login: " + response);
            return "";
        }
        return response;
    }


    private boolean isContentEncrypted(String content) {
        System.out.println("************");
        System.out.println(content.substring(0, ENCRYPTED_CONTENT.length()));
        System.out.println("************");
        if (content.substring(0, ENCRYPTED_CONTENT.length()).equals(ENCRYPTED_CONTENT)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method strips off the first line of content
     * @param content
     * @return the content excluding the first line
     */
    private String getEncryptedContent (String content) {
        return content.substring(content.indexOf('\n')+1);
    }
}