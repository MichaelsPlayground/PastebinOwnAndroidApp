package de.androidcrypto.pastebinownandroidapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

public class EnterUserCredentialsActivity extends AppCompatActivity {

    com.google.android.material.textfield.TextInputEditText userName;
    com.google.android.material.textfield.TextInputEditText userPassword;
    Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_user_credentials);

        userName = findViewById(R.id.etEnterUserCredentialsName);
        userPassword = findViewById(R.id.etEnterUserCredentialsPassword);
        submit = findViewById(R.id.btnEnterUserCredentialsSubmit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(EnterUserCredentialsActivity.this);
                if (TextUtils.isEmpty(userName.getText().toString())) {
                    Snackbar snackbar = Snackbar.make(view, "Please enter a user name", Snackbar.LENGTH_LONG);
                    snackbar.setBackgroundTint(ContextCompat.getColor(EnterUserCredentialsActivity.this, R.color.red));
                    snackbar.show();
                    return;
                }
                if (TextUtils.isEmpty(userPassword.getText().toString())) {
                    Snackbar snackbar = Snackbar.make(view, "Please enter a user password", Snackbar.LENGTH_LONG);
                    snackbar.setBackgroundTint(ContextCompat.getColor(EnterUserCredentialsActivity.this, R.color.red));
                    snackbar.show();
                    return;
                }
                // store in encrypted shared preference
                StorageUtils storageUtils = new StorageUtils(view.getContext());
                if (!storageUtils.isStorageLibraryReady()) {
                    Snackbar snackbar = Snackbar.make(view, "secure storage is not available, aborted", Snackbar.LENGTH_LONG);
                    snackbar.setBackgroundTint(ContextCompat.getColor(EnterUserCredentialsActivity.this, R.color.red));
                    snackbar.show();
                    return;
                }
                boolean storageNameSuccess = storageUtils.setUserName(userName.getText().toString());
                if (storageNameSuccess) {
                    Snackbar snackbar = Snackbar.make(view, "secure storage of thje user name was successful", Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(ContextCompat.getColor(EnterUserCredentialsActivity.this, R.color.green));
                    snackbar.show();
                } else {
                    Snackbar snackbar = Snackbar.make(view, "secure storage was not successful", Snackbar.LENGTH_LONG);
                    snackbar.setBackgroundTint(ContextCompat.getColor(EnterUserCredentialsActivity.this, R.color.red));
                    snackbar.show();
                }
                boolean storagePasswordSuccess = storageUtils.setUserPassword(userPassword.getText().toString());
                if (storagePasswordSuccess) {
                    Snackbar snackbar = Snackbar.make(view, "secure storage of the user password was successful", Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(ContextCompat.getColor(EnterUserCredentialsActivity.this, R.color.green));
                    snackbar.show();
                } else {
                    Snackbar snackbar = Snackbar.make(view, "secure storage of the user password was not successful", Snackbar.LENGTH_LONG);
                    snackbar.setBackgroundTint(ContextCompat.getColor(EnterUserCredentialsActivity.this, R.color.red));
                    snackbar.show();
                }
                if (storageNameSuccess && storagePasswordSuccess) {
                    // wait for 2 seconds
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            Intent intent = new Intent(EnterUserCredentialsActivity.this, MainActivity.class);
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