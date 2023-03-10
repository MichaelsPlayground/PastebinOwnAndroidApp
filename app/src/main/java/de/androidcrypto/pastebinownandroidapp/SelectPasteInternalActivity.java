package de.androidcrypto.pastebinownandroidapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
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

    SwitchMaterial pasteSynced;

    RecyclerView recyclerView;
    //androidx.swiperefreshlayout.widget.SwipeRefreshLayout srl;
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(SelectPasteInternalActivity.this);

    private PastesInternalAdapter pastesInternalAdapter;
    //private ArrayList<PasteModel> pasteArrayList;

    private boolean listSynced = false; // default
    private ArrayList<FileModel> pastesArrayList = new ArrayList<>();


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

        pasteSynced = findViewById(R.id.swSPISnyc);
        recyclerView = (RecyclerView) findViewById(R.id.rvSPI);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        // adding our array list to our recycler view adapter class.
        pastesInternalAdapter = new PastesInternalAdapter(pastesArrayList, this, listSynced);
        // setting adapter to our recycler view.
        recyclerView.setAdapter(pastesInternalAdapter);
        getInternalPastes(this, listSynced);

        pasteSynced.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                listSynced = pasteSynced.isChecked();
                getInternalPastes(getApplicationContext(), listSynced);
            }
        });
    }

    private void getInternalPastes(Context context, boolean listSyncedFiles) {
        Log.i(TAG, "getInternalPastes");
        pastesArrayList = new ArrayList<>();
        InternalStorageUtils internalStorageUtils = new InternalStorageUtils(context);
        pastesArrayList = internalStorageUtils.listPastesInternalModel(listSyncedFiles);
        Log.i(TAG, "pastesArrayList contains entries: " + pastesArrayList.size());
        System.out.println("getInternalPastes, filenames");
        for (int i = 0; i < pastesArrayList.size(); i++) {
            System.out.println("pos " + i + " fn: " + pastesArrayList.get(i));
        }
        pastesInternalAdapter = new PastesInternalAdapter(pastesArrayList, context, listSynced);
        pastesInternalAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(pastesInternalAdapter);
    }

}