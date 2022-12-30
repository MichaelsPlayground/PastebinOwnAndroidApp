package de.androidcrypto.pastebinownandroidapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;

public class SendPasteActivity extends AppCompatActivity {

    com.google.android.material.textfield.TextInputEditText pasteTitle;
    com.google.android.material.textfield.TextInputEditText pasteText;
    Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_paste);
    }
}