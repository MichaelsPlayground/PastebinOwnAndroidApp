package de.androidcrypto.pastebinownandroidapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class MainBrowseFolderActivity extends AppCompatActivity implements Serializable {

    Button generateSampleFiles, listFolder, listFiles, browseFolder;
    EditText selectedFolder, selectedFile, browsedFile;

    Intent listFolderIntent, listFilesIntent, browseFolderIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_browse_folder);

        generateSampleFiles = findViewById(R.id.btnGenerateSampleFiles);
        listFolder = findViewById(R.id.btnListFolder);
        listFiles = findViewById(R.id.btnListFiles);
        browseFolder = findViewById(R.id.btnBrowseFolder);
        selectedFolder = findViewById(R.id.etSelectedFolder);
        selectedFile = findViewById(R.id.etSelectedFile);
        browsedFile = findViewById(R.id.etBrowsedFile);

        listFolderIntent = new Intent(MainBrowseFolderActivity.this, ListFolder.class);
        listFilesIntent = new Intent(MainBrowseFolderActivity.this, ListFiles.class);
        browseFolderIntent = new Intent(MainBrowseFolderActivity.this, BrowseFolder.class);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String folder = "";
            String file = "";
            folder = (String) getIntent().getSerializableExtra("selectedFolder"); //Obtaining data
            if (folder != null) {
                selectedFolder.setText(folder);
                System.out.println("MainActivity folder: " + folder);
                // todo do what has todo when folder is selected
            }
            file = (String) getIntent().getSerializableExtra("selectedFile"); //Obtaining data
            if (file != null) {
                selectedFile.setText(file);
                System.out.println("MainActivity file: " + file);
                // todo do what has todo when file is selected
            }
        }

        generateSampleFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // generates 50 files in 50 sub folders plus 50 files in root folder
                String basisFilename = "ABC_2021_";
                String basisFolderFilename = "XYZ_2021_";
                String basisExtension = ".csv";
                String completeFilename;
                // start with root folder
                System.out.println("generate 50 sample files in root folder");
                int counter = 0;
                for (int i = 1; i < 51; i++) {
                    completeFilename = basisFilename +
                            String.format(Locale.GERMANY, "%02d", i) +
                            basisExtension;
                    writeFileToInternalStorage("", completeFilename);
                }
                System.out.println("generate 50 sample files in 50 subfolder");
                String subDirectory;
                for (int j = 1; j < 51; j++) {
                    subDirectory = "2021_" + String.format(Locale.GERMANY, "%02d", j);
                    for (int i = 1; i < 51; i++) {
                        completeFilename = basisFolderFilename +
                                String.format(Locale.GERMANY, "%02d", i) +
                                basisExtension;
                        writeFileToInternalStorage(subDirectory, completeFilename);
                    }
                }
                System.out.println("done, all files and folders created");
            }
        });

        listFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(listFolderIntent);
            }
        });

        listFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(listFilesIntent);
            }
        });

        browseFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(browseFolderIntent);
            }
        });

    }

    private boolean writeFileToInternalStorage(String subDir, String filename) {
        byte[] data = "test".getBytes(StandardCharsets.UTF_8);
        try {
            File dir = new File(getFilesDir(), subDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            System.out.println("** dir: " + dir.toString());
            File newFile = new File(dir, filename);
            System.out.println("newFile: " + newFile.toString());
            FileOutputStream output = new FileOutputStream(new File(dir, filename));
            ByteArrayInputStream input = new ByteArrayInputStream(data);
            int DEFAULT_BUFFER_SIZE = 1024;
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int n = 0;
            n = input.read(buffer, 0, DEFAULT_BUFFER_SIZE);
            while (n >= 0) {
                output.write(buffer, 0, n);
                n = input.read(buffer, 0, DEFAULT_BUFFER_SIZE);
            }
            output.close();
            input.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}