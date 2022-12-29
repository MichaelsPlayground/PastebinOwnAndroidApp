package de.androidcrypto.pastebinownandroidapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import org.jpaste.utils.web.Post;
import org.jpaste.utils.web.Web;

public class ViewPasteActivity extends AppCompatActivity {

    private static final String TAG = "ViewPaste";
    TextView header;
    EditText viewPaste;

    private static final String API_GET_RAW_CONTENT = "https://pastebin.com/api/api_raw.php";
    private static final String API_PASTE_CONTENT_KEY = "https://pastebin.com/";

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
        // the activity was called directly so it will return to MainActivity

        if (!TextUtils.isEmpty(pasteUrl)) {
            String headerString = "Paste from URL " + pasteUrl;
            header.setText(headerString);
            String content = getPasteContent(userKey, pasteUrl);
            Log.i(TAG, "Content:\n" + content);
            viewPaste.setText(content);
        }
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
}