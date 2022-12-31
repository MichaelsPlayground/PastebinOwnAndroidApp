package de.androidcrypto.pastebinownandroidapp;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;

public class SelectPasteInternalSimpleActivity extends AppCompatActivity {
    final String TAG = "selectPasteInternalSimpleActivity";

    SwitchMaterial pasteEncrypted;

    RecyclerView recyclerView;
    //androidx.swiperefreshlayout.widget.SwipeRefreshLayout srl;
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(SelectPasteInternalSimpleActivity.this);

    private PastesInternalSimpleAdapter pastesInternalSimpleAdapter;
    //private ArrayList<PasteModel> pasteArrayList;

    private boolean listEncrypted = false; // default
    private ArrayList<String> pastesArrayList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_paste_internal_simple);

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

        // adding our array list to our recycler view adapter class.
        pastesInternalSimpleAdapter = new PastesInternalSimpleAdapter(pastesArrayList, this, listEncrypted);
        // setting adapter to our recycler view.
        recyclerView.setAdapter(pastesInternalSimpleAdapter);
        getInternalPastes(this, listEncrypted);

        pasteEncrypted.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                listEncrypted = pasteEncrypted.isChecked();
                getInternalPastes(getApplicationContext(), listEncrypted);
            }
        });
    }

    private void getInternalPastes(Context context, boolean listEncryptedFiles) {
        Log.i(TAG, "getInternalPastes");
        pastesArrayList = new ArrayList<>();
        InternalStorageUtils internalStorageUtils = new InternalStorageUtils(context);
        pastesArrayList = internalStorageUtils.listPastesInternal(listEncryptedFiles);
        Log.i(TAG, "pastesArrayList contains entries: " + pastesArrayList.size());
        System.out.println("getInternalPastes, filenames");
        for (int i = 0; i < pastesArrayList.size(); i++) {
            System.out.println("pos " + i + " fn: " + pastesArrayList.get(i));
        }
        pastesInternalSimpleAdapter = new PastesInternalSimpleAdapter(pastesArrayList, context, listEncrypted);
        pastesInternalSimpleAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(pastesInternalSimpleAdapter);
    }

}