package de.androidcrypto.pastebinownandroidapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.nio.charset.StandardCharsets;
import java.util.Date;

public class InternalStorageActivity extends AppCompatActivity {

    private static final String TAG = "InternalStorageAct";

    com.google.android.material.textfield.TextInputEditText content;
    Button save, loadUnencrypted, loadEncrypted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internal_storage);

        content = findViewById(R.id.etISAContent);
        save = findViewById(R.id.btnISASave);
        loadUnencrypted = findViewById(R.id.btnISALoadUnencrypted);
        loadEncrypted = findViewById(R.id.btnISALoadEncrypted);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "save");

                String filename = "test.txt";
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
                InternalStorageUtils internalStorageUtils = new InternalStorageUtils(view.getContext());
                // write an unencrypted string
                boolean writeSuccess = internalStorageUtils.writePasteInternal(
                        filename,
                        sampleString,
                        String.valueOf(timestamp),
                        false);
                Log.i(TAG, "unencrypted writeSuccess: " + writeSuccess);
                // write an encrypted string
                writeSuccess = internalStorageUtils.writePasteInternal(
                        filename,
                        sampleCiphertextString,
                        String.valueOf(timestamp),
                        true);
                Log.i(TAG, "encrypted writeSuccess: " + writeSuccess);

            }
        });

        loadUnencrypted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "load unencrypted");
                String filename = "test.txt";
                InternalStorageUtils internalStorageUtils = new InternalStorageUtils(view.getContext());
                content.setText(internalStorageUtils.loadPasteInternal(filename, false));
            }
        });

        loadEncrypted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "load encrypted");
                String filename = "test.txt";
                InternalStorageUtils internalStorageUtils = new InternalStorageUtils(view.getContext());
                content.setText(internalStorageUtils.loadPasteInternal(filename, true));
            }
        });

    }
}