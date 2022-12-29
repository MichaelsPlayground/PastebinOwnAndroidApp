package de.androidcrypto.pastebinownandroidapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.snackbar.Snackbar;

import org.jpaste.exceptions.PasteException;
import org.jpaste.pastebin.Pastebin;
import org.jpaste.pastebin.PastebinLink;
import org.jpaste.pastebin.PastebinPaste;
import org.jpaste.pastebin.account.PastebinAccount;
import org.jpaste.pastebin.exceptions.LoginException;
import org.jpaste.pastebin.exceptions.ParseException;
import org.jpaste.utils.web.Post;
import org.jpaste.utils.web.Web;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    Button enterDeveloperKey, enterUserCredentials;
    Button login, selectPaste, listPastes, pastePublicNoExpiration;

    Button encryptAString, mainBrowseFolder;

    Button checkInternetConnection;

    TextView getUrlData;

    PastebinAccount account;

    // basis url
    // get Developer key https://pastebin.com/doc_api#1
    private static final String API_GET_RAW_CONTENT = "https://pastebin.com/api/api_raw.php";
    // create a new paste https://pastebin.com/doc_api#2
    // getUserKey from login https://pastebin.com/doc_api#9


    private String userKey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        enterDeveloperKey = findViewById(R.id.btnMainEnterDeveloperKey);
        enterUserCredentials = findViewById(R.id.btnMainEnterUserCredentials);
        login = findViewById(R.id.btnMainLogin);
        selectPaste = findViewById(R.id.btnMainSelectPaste);

        listPastes = findViewById(R.id.btnMainListPastes);
        pastePublicNoExpiration = findViewById(R.id.btnPastePublicNoExpiration);

        encryptAString = findViewById(R.id.btnEncryptString);
        mainBrowseFolder = findViewById(R.id.btnMainBrowseFolder);

        checkInternetConnection = findViewById(R.id.btnCheckInternetConnection);


        getUrlData = findViewById(R.id.tvGetUrl);

        // todo work with threads to avoid the StrictMode.ThreadPolicy workaround
        // the error happens while doing network operations on MainThread like urlGetSynchronic
        // solution: https://stackoverflow.com/questions/25093546/android-os-networkonmainthreadexception-at-android-os-strictmodeandroidblockgua
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        enterDeveloperKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EnterDeveloperKeyActivity.class);
                startActivity(intent);
                // finish
            }
        });

        enterUserCredentials.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EnterUserCredentialsActivity.class);
                startActivity(intent);
                // finish
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "login to Pastebin");
                // check for stored credentials (developer key, user name and password)
                boolean credentialsAreSet = checkForCredentials();
                if (!credentialsAreSet) {
                    Log.d(TAG, "cannot login as not all credentials are set");
                    Snackbar snackbar = Snackbar.make(view, "Did you set the developer key, user name and password ? aborted", Snackbar.LENGTH_LONG);
                    snackbar.setBackgroundTint(ContextCompat.getColor(MainActivity.this, R.color.red));
                    snackbar.show();
                    return;
                }
                StorageUtils su = new StorageUtils(view.getContext());
                account = new PastebinAccount(su.getDeveloperKey(), su.getUserName(), su.getUserPassword());
                // fetches an user session id
                try {
                    account.login();
                } catch (LoginException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                    return;
                }
                userKey = account.getUserSessionId();
                Log.i(TAG, "userKey: " + userKey);
                getUrlData.setText("userKey : " + userKey);
            }
        });

        selectPaste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "select a paste");
                if (TextUtils.isEmpty(userKey)) {
                    Snackbar snackbar = Snackbar.make(view, "Please login to proceed, aborted", Snackbar.LENGTH_LONG);
                    snackbar.setBackgroundTint(ContextCompat.getColor(MainActivity.this, R.color.red));
                    snackbar.show();
                    //Toast.makeText(MainActivity.this, "Please login to proceed", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(MainActivity.this, SelectPasteActivity.class);
                //intent.putExtra("USER_KEY", userKey);
                startActivity(intent);
                // finish();
            }
        });


        listPastes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "list pastes");
                if (TextUtils.isEmpty(userKey)) {
                    Snackbar snackbar = Snackbar.make(view, "Please login to proceed, aborted", Snackbar.LENGTH_LONG);
                    snackbar.setBackgroundTint(ContextCompat.getColor(MainActivity.this, R.color.red));
                    snackbar.show();
                    //Toast.makeText(MainActivity.this, "Please login to proceed", Toast.LENGTH_SHORT).show();
                    return;
                }
                PastebinLink[] pastes = new PastebinLink[0];
                StringBuilder sb = new StringBuilder();
                try {
                    pastes = account.getPastes();
                } catch (ParseException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                    return;
                }
                if (pastes == null) {
                    Toast.makeText(MainActivity.this, "You don't have any pastes!", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "You don't have any pastes!");
                    return;
                }
                sb.append("found pastes: ").append(pastes.length).append("\n");
                for (PastebinLink paste : pastes) {
                    sb.append("Link: ").append(paste.getLink()).append("\n");
                    System.out.println("Link: " + paste.getLink());
                    sb.append("Hits: ").append(paste.getHits()).append("\n");
                    System.out.println("Hits: " + paste.getHits());
                    System.out.println();
                    sb.append("Title: ").append(paste.getPaste().getPasteTitle()).append("\n");
                    System.out.println("Title: " + paste.getPaste().getPasteTitle());
                    //System.out.println();
                    //System.out.println("[ Contents ]");
                    //System.out.println();
                    //paste.fetchContent();
                    //System.out.println(paste.getPaste().getContents());
                    System.out.println();
                    System.out.println("===============================");
                    sb.append("===============================").append("\n");
                    //System.out.println(resp);
                    sb.append("paste.getKey: " + paste.getKey()).append("\n");
                    sb.append(getPasteContent(paste.getKey())).append("\n");
                    System.out.println("===============================");
                    sb.append("===============================").append("\n");
                    System.out.println("[ END ]");
                }
                getUrlData.setText(sb.toString());
            }
        });

        pastePublicNoExpiration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "paste private no expiration");
                String title = "Test title";
                String contents = " This is a test content.";
                int visibility = PastebinPaste.VISIBILITY_PUBLIC;
                // create paste
                PastebinPaste paste = new PastebinPaste(account);
                paste.setContents(contents);
                paste.setPasteTitle(title);
                paste.setVisibility(visibility);
                // push paste
                PastebinLink link = null;
                try {
                    link = paste.paste();
                } catch (PasteException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                    return;
                }
                String pasteLink = link.getLink().toString();
                String pasteKey = link.getKey();
                System.out.println(link.getLink());
                getUrlData.setText("paste is posted with this key " + pasteKey + " and URL: " + pasteLink);
            }
        });

        // just a service activity for testing
        encryptAString.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EncryptionUtil.class);
                startActivity(intent);
                // finish();
            }
        });

        mainBrowseFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MainBrowseFolderActivity.class);
                startActivity(intent);
            }
        });



        checkInternetConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "check that an internet connection is available");
                getUrlData.setText("Internet connection is active: " + isOnline());


            }
        });

    }

    /**
     * This method checks that a developer key, a user name and user password were stored
     * returns TRUE if all are set or FALSE when one or more are not set
     */
    private boolean checkForCredentials() {
        StorageUtils storageUtils = new StorageUtils(this);
        if (!storageUtils.isDeveloperKeyAvailable()) {
            Log.d(TAG, "the developer key is not available");
            return false;
        }
        if (!storageUtils.isUserNameAvailable()) {
            Log.d(TAG, "the user name is not available");
            return false;
        }
        if (!storageUtils.isUserPasswordAvailable()) {
            Log.d(TAG, "the user password is not available");
            return false;
        }
        return true;
    }

    private String getPasteContent(String apiPasteKey) {
        // do it myself to get the content
        StorageUtils storageUtils = new StorageUtils(getApplicationContext());
        Post post = new Post();
        post.put("api_dev_key", storageUtils.getDeveloperKey());
        post.put("api_user_key", userKey);
        post.put("api_paste_key", apiPasteKey);
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


    /**
     * This method checks if we can ping to google.com - if yes we do have an active internet connection
     * returns true if there is an active internet connection
     * returns false if there is no active internet connection
     * https://stackoverflow.com/a/45777087/8166854 by sami rahimi
     */
    public Boolean isOnline() {
        try {
            Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.com");
            int returnVal = p1.waitFor();
            boolean reachable = (returnVal==0);
            return reachable;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

}