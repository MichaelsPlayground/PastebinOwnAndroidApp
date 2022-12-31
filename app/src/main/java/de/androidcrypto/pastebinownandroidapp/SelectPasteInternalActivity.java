package de.androidcrypto.pastebinownandroidapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;

import org.jpaste.pastebin.PastebinLink;
import org.jpaste.pastebin.account.PastebinAccount;
import org.jpaste.pastebin.exceptions.LoginException;
import org.jpaste.pastebin.exceptions.ParseException;

import java.io.File;
import java.util.ArrayList;

public class SelectPasteInternalActivity extends AppCompatActivity {
    final String TAG = "selectPasteInternalActivity";

    SwitchMaterial pasteEncrypted;

    RecyclerView recyclerView;
    //androidx.swiperefreshlayout.widget.SwipeRefreshLayout srl;
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(SelectPasteInternalActivity.this);

    private PastesInternalAdapter pastesInternalAdapter;
    //private ArrayList<PasteModel> pasteArrayList;

    private boolean listEncrypted = false; // default
    private ArrayList<String> pastesArrayList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_paste_internal);

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

        pasteEncrypted = findViewById(R.id.swSPIEncrypted);
        recyclerView = (RecyclerView) findViewById(R.id.rvSPI);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        // creating our new array list
        //pastesArrayList = new ArrayList<>();

        // adding our array list to our recycler view adapter class.
        pastesInternalAdapter = new PastesInternalAdapter(pastesArrayList, this, listEncrypted);

        // setting adapter to our recycler view.
        recyclerView.setAdapter(pastesInternalAdapter);

        getInternalPastes(this, listEncrypted);

        //PastesAdapter pastesAdapter = new PastesAdapter(dataReturned);
        //recyclerView.setAdapter(pastesAdapter);

    }

    private void getInternalPastes(Context context, boolean listEncryptedFiles) {
        Log.i(TAG, "getInternalPastes");
        pastesArrayList = new ArrayList<>();
        InternalStorageUtils internalStorageUtils = new InternalStorageUtils(context);
        pastesArrayList = internalStorageUtils.listPastesInternal(listEncryptedFiles);
        Log.i(TAG, "pastesArrayList contains entries: " + pastesArrayList.size());
        pastesInternalAdapter = new PastesInternalAdapter(pastesArrayList, context, listEncrypted);
    }

}