package de.androidcrypto.pastebinownandroidapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import com.google.android.material.snackbar.Snackbar;

public class EnterDeveloperKeyActivity extends AppCompatActivity {

    com.google.android.material.textfield.TextInputEditText developerKey;
    Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_developer_key);

        developerKey = findViewById(R.id.etEnterDeveloperKeyKey);
        submit = findViewById(R.id.btnEnterDeveloperKeySubmit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(EnterDeveloperKeyActivity.this);
                if (TextUtils.isEmpty(developerKey.getText().toString())) {
                    Snackbar snackbar = Snackbar.make(view, "Please enter a developer key", Snackbar.LENGTH_LONG);
                    snackbar.setBackgroundTint(ContextCompat.getColor(EnterDeveloperKeyActivity.this, R.color.red));
                    snackbar.show();
                    return;
                }
                // store in encrypted shared preference
                StorageUtils storageUtils = new StorageUtils(view.getContext());
                if (!storageUtils.isStorageLibraryReady()) {
                    Snackbar snackbar = Snackbar.make(view, "secure storage is not available, aborted", Snackbar.LENGTH_LONG);
                    snackbar.setBackgroundTint(ContextCompat.getColor(EnterDeveloperKeyActivity.this, R.color.red));
                    snackbar.show();
                    return;
                }
                boolean storageSuccess = storageUtils.setDeveloperKey(developerKey.getText().toString());
                if (storageSuccess) {
                    Snackbar snackbar = Snackbar.make(view, "secure storage was successful", Snackbar.LENGTH_LONG);
                    snackbar.setBackgroundTint(ContextCompat.getColor(EnterDeveloperKeyActivity.this, R.color.green));
                    snackbar.show();
                } else {
                    Snackbar snackbar = Snackbar.make(view, "secure storage was not successful", Snackbar.LENGTH_LONG);
                    snackbar.setBackgroundTint(ContextCompat.getColor(EnterDeveloperKeyActivity.this, R.color.red));
                    snackbar.show();
                }
                if (storageSuccess) {
                    // wait for 2 seconds
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            Intent intent = new Intent(EnterDeveloperKeyActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }, 2000);
                }
            }
        });
    }

    public static void hideKeyboard(Activity activity) {
        // code from https://stackoverflow.com/a/17789187/8166854
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}