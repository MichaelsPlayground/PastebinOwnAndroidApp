package de.androidcrypto.pastebinownandroidapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jpaste.pastebin.PasteExpireDate;
import org.jpaste.pastebin.PastebinLink;
import org.jpaste.pastebin.account.PastebinAccount;
import org.jpaste.pastebin.exceptions.LoginException;
import org.jpaste.pastebin.exceptions.ParseException;

import java.util.ArrayList;

public class SelectPasteActivity extends AppCompatActivity {
    final String TAG = "selectPasteActivity";
    RecyclerView recyclerView;
    androidx.swiperefreshlayout.widget.SwipeRefreshLayout srl;
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(SelectPasteActivity.this);

    private PastesAdapter pastesAdapter;
    private ArrayList<PasteModel> pasteArrayList;

    PastebinAccount account;
    String userKey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_paste);

        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // todo change activity to AddPaste.class
                startActivity(new Intent(SelectPasteActivity.this, MainActivity.class));
            }
        });

         */

        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        // creating our new array list
        pasteArrayList = new ArrayList<>();

        // adding our array list to our recycler view adapter class.
        pastesAdapter = new PastesAdapter(pasteArrayList, this, userKey);

        // setting adapter to our recycler view.
        recyclerView.setAdapter(pastesAdapter);

        //PastesAdapter pastesAdapter = new PastesAdapter(dataReturned);
        //recyclerView.setAdapter(pastesAdapter);


        StorageUtils su = new StorageUtils(this);
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
        getPastes();
        System.out.println("*** getPastes size: " + pasteArrayList.size());
        //pastesAdapter.notifyDataSetChanged();

        // receive the link from intent
        Intent intent = getIntent();
        userKey = intent.getStringExtra("USER_KEY");

    }

    private void getPastes() {
        pasteArrayList = new ArrayList<>();
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
            Toast.makeText(SelectPasteActivity.this, "You don't have any pastes!", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "You don't have any pastes!");
            return;
        }
        sb.append("found pastes: ").append(pastes.length).append("\n");
        for (PastebinLink paste : pastes) {
            PasteModel pasteModel = new PasteModel(
                    // String pasteKey,
                    // long pasteDate,
                    // String pasteTitle,
                    // long pasteSize,
                    // int pasteExpireDate,
                    // int pastePrivate,
                    // String formatLong,
                    // String formatShort,
                    // String pasteUrl,
                    // int pasteHits
                paste.getKey(),
                paste.getPasteDate(),
                    paste.getPaste().getPasteTitle(),
                    123, // size
                    String.valueOf(paste.getPaste().getPasteExpireDate()), // todo expire data
                    paste.getPaste().getVisibility(), // pastePrivate
                    paste.getPaste().getPasteFormat(),
                    paste.getPaste().getPasteFormat(),
                    paste.getLink().toString(),
                    paste.getHits()
            );
            pasteArrayList.add(pasteModel);

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
            //System.out.println("===============================");
            //sb.append("===============================").append("\n");
            //System.out.println(resp);
            //sb.append(getPasteContent(paste.getKey())).append("\n");
            //System.out.println("===============================");
            sb.append("===============================").append("\n");
            System.out.println("[ END ]");
        }

        pastesAdapter = new PastesAdapter(pasteArrayList, this, userKey);
        pastesAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(pastesAdapter);
    }


    /*
I/System.out: ����t�<paste>
I/System.out: 	<paste_key>NkiZjcBq</paste_key>
I/System.out: 	<paste_date>1670605818</paste_date>
I/System.out: 	<paste_title>FirebasePlayground google-services.json</paste_title>
I/System.out: 	<paste_size>1898</paste_size>
I/System.out: 	<paste_expire_date>0</paste_expire_date>
I/System.out: 	<paste_private>2</paste_private>
I/System.out: 	<paste_format_long>None</paste_format_long>
I/System.out: 	<paste_format_short>text</paste_format_short>
I/System.out: 	<paste_url>https://pastebin.com/NkiZjcBq</paste_url>
I/System.out: 	<paste_hits>3</paste_hits>
I/System.out: </paste>
     */

}