package de.androidcrypto.pastebinownandroidapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

//import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class IntentGroup04 extends AppCompatActivity {

    Button btn01, btn02, btn03, btn04,
            btn05, btn06, btn07, btn08;
    Button btn05Crop;

    EditText etE01, etE02, etE03, etG04;
    ImageView ivE05;

    private static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 100;
    private static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 101;
    private static final int REQUEST_PERMISSION_WRITE_IMAGE_EXTERNAL_STORAGE = 102;
    private static final int REQUEST_PERMISSION_READ_IMAGE_EXTERNAL_STORAGE = 104;
    private static final int REQUEST_PERMISSION_WRITE_BYTE_EXTERNAL_STORAGE = 104;
    private static final int REQUEST_PERMISSION_READ_BYTE_EXTERNAL_STORAGE = 105;

    Context contextSave; // wird für write & read a file from uri benötigt

    Uri mImageCaptureUri; // for cropping intent
    private static final int CROPPING = 5;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intent_group04);

        btn01 = findViewById(R.id.btnG04B01);
        btn02 = findViewById(R.id.btnG04B02);
        btn03 = findViewById(R.id.btnG04B03);
        btn04 = findViewById(R.id.btnG04B04);
        btn05 = findViewById(R.id.btnG04B05);
        btn06 = findViewById(R.id.btnG04B06);
        btn07 = findViewById(R.id.btnG04B07);
        btn08 = findViewById(R.id.btnG04B08);
        btn05Crop = findViewById(R.id.btnG04B05Crop);

        etE01 = findViewById(R.id.etG04E01);
        etE02 = findViewById(R.id.etG04E02);
        etE03 = findViewById(R.id.etG04E03);
        ivE05 = findViewById(R.id.ivG04B05);
        etG04 = findViewById(R.id.etG04E04);

        // create some random content
        RandomString gen = new RandomString(5);
        String randomString = "A " + gen.nextString() + " B " + gen.nextString();
        etE01.setText(randomString);

        // write string to internal storage
        btn01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // store a file in internal storage
                String dataToWrite = etE01.getText().toString();
                String filename = etE03.getText().toString();
                etG04.setText(""); // clear the info
                // sanity check
                if (dataToWrite.equals("")) {
                    etG04.setText("no data to save");
                    return;
                }
                if (filename.equals("")) {
                    etG04.setText("no filename to save");
                    return;
                }
                FileWriter writer = null;
                try {
                    File file = new File(v.getContext().getFilesDir(), filename);
                    writer = new FileWriter(file);
                    writer.append(dataToWrite);
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    etG04.setText("ERROR: " + e.toString());
                    return;
                }
                String message = "file written to internal storage: " + filename;
                etG04.setText(message);
            }
        });

        // read string from internal storage
        btn02.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // read a file from internal storage
                String dataToLoad = "";
                etE02.setText(dataToLoad); // clear the edittext
                etG04.setText(""); // clear the info
                String filename = etE03.getText().toString();
                // sanity check
                if (filename.equals("")) {
                    etG04.setText("no filename to load");
                    return;
                }
                File file = new File(v.getContext().getFilesDir(), filename);
                // check that file is existing
                if (!file.exists()) {
                    String errorMessage = "file not existing: " + filename;
                    etG04.setText(errorMessage);
                    return;
                }
                int length = (int) file.length();
                byte[] bytes = new byte[length];
                FileInputStream in = null;
                try {
                    in = new FileInputStream(file);
                    in.read(bytes);
                    in.close();
                    dataToLoad = new String(bytes);
                    etE02.setText(dataToLoad);
                } catch (IOException e) {
                    e.printStackTrace();
                    etG04.setText("ERROR: " + e.toString());
                    return;
                }
                String message = "file loaded from internal storage: " + filename;
                etG04.setText(message);

            }
        });

        // write string to external storage
        btn03.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // write a file to external shared storage
                contextSave = v.getContext();
                etG04.setText(""); // clear the info
                verifyPermissionsWriteString();
            }
        });

        // read string from external storage
        btn04.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // read a file from external shared storage
                contextSave = v.getContext();
                etG04.setText(""); // clear the info
                // clear edittext
                etE02.setText("");
                verifyPermissionsReadString();
            }
        });

        // read image from external storage
        btn05.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // load an image
                contextSave = v.getContext();
                etG04.setText(""); // clear the info
                // clear edittext
                etE02.setText("");
                verifyPermissionsReadImage();
            }
        });

        btn05Crop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // crop an image by intent
            }
        });

        // write image to external storage
        btn06.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // we are using scoped storage
                // in build.gradle:
                /*
                buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
                buildFeatures {
        viewBinding true
    }
                 */
                // save an image
                contextSave = v.getContext();
                etG04.setText(""); // clear the info
                // clear edittext
                etE02.setText("");
                verifyPermissionsWriteImage();
            }
        });

        // read byte array from external storage
        btn07.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //contextSave = v.getContext();
                etG04.setText(""); // clear the info
                // clear edittext
                etE02.setText("");
                verifyPermissionsReadByte();
            }
        });

        // write byte array to external storage
        btn08.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contextSave = v.getContext();
                etG04.setText(""); // clear the info
                // clear edittext
                //etE02.setText("");
                verifyPermissionsWriteByte();
            }
        });

        ivE05.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // https://stackoverflow.com/a/39200533/8166854
                BitmapDrawable draw = (BitmapDrawable) ivE05.getDrawable();
                Bitmap original = draw.getBitmap();
                Bitmap bitmap = Bitmap.createBitmap(original.getWidth(),
                        original.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);

                Paint paint = new Paint();
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(0);
                paint.setColorFilter(new ColorMatrixColorFilter(
                        matrix));
                canvas.drawBitmap(original, 0, 0, paint);
                ivE05.setImageBitmap(bitmap);
                etG04.setText("image converted to black and white");
                return true;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                writeStringToExternalSharedStorage();
            } else {
                Toast.makeText(this, "Grant Storage Permission is Required to use this function.", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == REQUEST_PERMISSION_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readStringFromExternalSharedStorage();
            } else {
                Toast.makeText(this, "Grant Storage Permission is Required to use this function.", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == REQUEST_PERMISSION_READ_IMAGE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readImageFromExternalSharedStorage();
            } else {
                Toast.makeText(this, "Grant Storage Permission is Required to use this function.", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == REQUEST_PERMISSION_WRITE_IMAGE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                writeImageToExternalSharedStorage();
            } else {
                Toast.makeText(this, "Grant Storage Permission is Required to use this function.", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == REQUEST_PERMISSION_READ_BYTE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readByteFromExternalSharedStorage();
            } else {
                Toast.makeText(this, "Grant Storage Permission is Required to use this function.", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == REQUEST_PERMISSION_WRITE_BYTE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                writeByteToExternalSharedStorage();
            } else {
                Toast.makeText(this, "Grant Storage Permission is Required to use this function.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * section external storage permission check
     */

    private void verifyPermissionsWriteString() {
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[1]) == PackageManager.PERMISSION_GRANTED) {
            writeStringToExternalSharedStorage();
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
        }
    }

    private void writeStringToExternalSharedStorage() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        // Optionally, specify a URI for the file that should appear in the
        // system file picker when it loads.
        //boolean pickerInitialUri = false;
        //intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);
        // get filename from edittext
        String filename = etE03.getText().toString();
        // sanity check
        if (filename.equals("")) {
            etG04.setText("no filename to load");
            return;
        }
        intent.putExtra(Intent.EXTRA_TITLE, filename);
        fileSaverActivityResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> fileSaverActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent resultData = result.getData();
                        // The result data contains a URI for the document or directory that
                        // the user selected.
                        Uri uri = null;
                        if (resultData != null) {
                            uri = resultData.getData();
                            // Perform operations on the document using its URI.
                            try {
                                // get file content from edittext
                                String fileContent = etE01.getText().toString();
                                writeTextToUri(uri, fileContent);
                                String message = "file written to external shared storage: " + uri.toString();
                                etG04.setText(message);
                            } catch (IOException e) {
                                e.printStackTrace();
                                etG04.setText("ERROR: " + e.toString());
                                return;
                            }
                        }
                    }
                }
            });

    private void writeTextToUri(Uri uri, String data) throws IOException {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(contextSave.getContentResolver().openOutputStream(uri));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            System.out.println("Exception File write failed: " + e.toString());
        }
    }

    /**
     * section read string from external shared storage
     */

    private void verifyPermissionsReadString() {
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[1]) == PackageManager.PERMISSION_GRANTED) {
            readStringFromExternalSharedStorage();
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);
        }
    }

    private void readStringFromExternalSharedStorage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        // Optionally, specify a URI for the file that should appear in the
        // system file picker when it loads.
        boolean pickerInitialUri = false;
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);
        fileLoaderActivityResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> fileLoaderActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent resultData = result.getData();
                        // The result data contains a URI for the document or directory that
                        // the user selected.
                        Uri uri = null;
                        if (resultData != null) {
                            uri = resultData.getData();
                            // Perform operations on the document using its URI.
                            try {
                                String fileContent = readTextFromUri(uri);
                                etE02.setText(fileContent);
                                String message = "file loaded from external storage" + uri.toString();
                                etG04.setText(message);
                                // get metadata from uri
                                dumpImageMetaData(uri);
                            } catch (IOException e) {
                                e.printStackTrace();
                                etG04.setText("ERROR: " + e.toString());
                                return;
                            }
                        }
                    }
                }
            });

    private String readTextFromUri(Uri uri) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        //try (InputStream inputStream = getContentResolver().openInputStream(uri);
        // warning: contextSave needs to get filled
        try (InputStream inputStream = contextSave.getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(Objects.requireNonNull(inputStream)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }
        }
        return stringBuilder.toString();
    }

    public void dumpImageMetaData(Uri uri) {
        String TAG = "### LOG ";
        // The query, because it only applies to a single document, returns only
        // one row. There's no need to filter, sort, or select fields,
        // because we want all fields for one document.
        String message = etG04.getText().toString();
        Cursor cursor = contextSave.getContentResolver()
                .query(uri, null, null, null, null, null);

        try {
            // moveToFirst() returns false if the cursor has 0 rows. Very handy for
            // "if there's anything to look at, look at it" conditionals.
            if (cursor != null && cursor.moveToFirst()) {
                // Note it's called "Display Name". This is
                // provider-specific, and might not necessarily be the file name.
                @SuppressLint("Range") String displayName = cursor.getString(
                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                Log.i(TAG, "Display Name: " + displayName);
                message = message + "\nDisplay Name: " + displayName;
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                // If the size is unknown, the value stored is null. But because an
                // int can't be null, the behavior is implementation-specific,
                // and unpredictable. So as
                // a rule, check if it's null before assigning to an int. This will
                // happen often: The storage API allows for remote files, whose
                // size might not be locally known.
                String size = null;
                if (!cursor.isNull(sizeIndex)) {
                    // Technically the column stores an int, but cursor.getString()
                    // will do the conversion automatically.
                    size = cursor.getString(sizeIndex);
                } else {
                    size = "Unknown";
                }
                Log.i(TAG, "Size: " + size + " bytes");
                message = message + "\nSize: " + size + " bytes";
                etG04.setText(message);
            }
        } finally {
            cursor.close();
        }
    }

    /**
     * section write an image to external storage
     */

    private void verifyPermissionsReadImage() {
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[1]) == PackageManager.PERMISSION_GRANTED) {
            readImageFromExternalSharedStorage();
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);
        }
    }

    private void readImageFromExternalSharedStorage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        // todo delete routines following
        // does not work on Samsung A5
        //intent.putExtra("crop","true");
        //intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        // delete until here
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // Only the system receives the ACTION_OPEN_DOCUMENT, so no need to test.
        //startActivityForResult(intent, REQUEST_IMAGE_OPEN);
        boolean pickerInitialUri = false;
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);
        imageFileLoaderActivityResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> imageFileLoaderActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent resultData = result.getData();
                        // The result data contains the full sized image data
                        // the user selected.
                        Uri uri = null;
                        if (resultData != null) {
                            uri = resultData.getData();
                            ivE05.setImageURI(uri);
                            etG04.setText("photo loaded");
                        }
                    }
                }
            });

    /**
     * section write an image to external storage
     */

    private void verifyPermissionsWriteImage() {
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[1]) == PackageManager.PERMISSION_GRANTED) {
            writeImageToExternalSharedStorage();
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);
        }
    }

    private void writeImageToExternalSharedStorage() {
        System.out.println("### writeImageToExternalSharedStorage");
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp;
        BitmapDrawable draw = (BitmapDrawable) ivE05.getDrawable();
        Bitmap bitmap = draw.getBitmap();
        System.out.println("### saveImageToExternalSharedStorage started");
        boolean saved = false;
        saved = saveImageToExternalStorage(imageFileName, bitmap);
        System.out.println("saved: " + saved);
        etG04.setText("image saved to gallery: " + imageFileName);
    }

    ActivityResultLauncher<Intent> imageFileWriterActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent resultData = result.getData();
                        // the user selected.
                        Uri uri = null;
                        File storageFile;
                        if (resultData != null) {
                            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                            String imageFileName = "JPEG_" + timeStamp;
                            BitmapDrawable draw = (BitmapDrawable) ivE05.getDrawable();
                            Bitmap bitmap = draw.getBitmap();
                            saveImageToExternalStorage(imageFileName, bitmap);
                        }
                    }
                }
            });

    // read image from external storage
    // Java:  https://www.youtube.com/watch?v=4FgzqhhdLAs
    // https://www.youtube.com/watch?v=E4hc5Kb6KNc

    private boolean saveImageToExternalStorage(String imgName, Bitmap bmp) {
        // https://www.youtube.com/watch?v=nA4XWsG9IPM
        Uri imageCollection = null;
        ContentResolver resolver = getContentResolver();
        // > SDK 28
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            imageCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            imageCollection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, imgName + ".jpg");
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        Uri imageUri = resolver.insert(imageCollection, contentValues);
        try {
            OutputStream outputStream = resolver.openOutputStream(Objects.requireNonNull(imageUri));
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            Objects.requireNonNull(outputStream);
            return true;
        } catch (Exception e)  {
            Toast.makeText(this, "Image not saved: \n" + e, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return false;
    }

    /**
     * section read byte array from external storage
     */

    private void verifyPermissionsReadByte() {
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[1]) == PackageManager.PERMISSION_GRANTED) {
            readByteFromExternalSharedStorage();
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);
        }
    }

    private void readByteFromExternalSharedStorage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // Only the system receives the ACTION_OPEN_DOCUMENT, so no need to test.
        //startActivityForResult(intent, REQUEST_IMAGE_OPEN);
        boolean pickerInitialUri = false;
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);
        byteFileLoaderActivityResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> byteFileLoaderActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent resultData = result.getData();
                        // The result data contains the full sized image data
                        // the user selected.
                        Uri uri = null;
                        byte[] dataBytes = new byte[0];
                        if (resultData != null) {
                            uri = resultData.getData();
                            /*
                            try {
                                dataBytes = readByteFromUri(uri);
                                //ivE05.setImageURI(uri);
                                etE02.setText(Utils.bytesToHex(dataBytes));
                            } catch (IOException e) {
                                etE02.setText("error on reading the file: " + e);
                            }

                             */
                        }
                    }
                }
            });
/*
    private byte[] readByteFromUri(Uri uri) throws IOException {
        // this method needs Apache Commons IO dependency
        // implementation group: 'commons-io', name: 'commons-io', version: '2.11.0'
        byte[] data;
        // https://stackoverflow.com/questions/1264709/convert-inputstream-to-byte-array-in-java
        try (InputStream inputStream = contextSave.getContentResolver().openInputStream(uri);) {
            data = IOUtils.toByteArray(inputStream);
            IOUtils.closeQuietly(inputStream);
        }
        return data;
    }
*/
    /**
     * section write byte array from external storage
     */

    private void verifyPermissionsWriteByte() {
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[1]) == PackageManager.PERMISSION_GRANTED) {
            writeByteToExternalSharedStorage();
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
        }
    }

    private void writeByteToExternalSharedStorage() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        // Optionally, specify a URI for the file that should appear in the
        // system file picker when it loads.
        //boolean pickerInitialUri = false;
        //intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);
        // get filename from edittext
        String filename = etE03.getText().toString();
        // sanity check
        if (filename.equals("")) {
            etG04.setText("no filename to save");
            return;
        }
        intent.putExtra(Intent.EXTRA_TITLE, filename);
        byteFileWriterActivityResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> byteFileWriterActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent resultData = result.getData();
                        // The result data contains a URI for the document or directory that
                        // the user selected.
                        Uri uri = null;
                        if (resultData != null) {
                            uri = resultData.getData();
                            // Perform operations on the document using its URI.
                            //try {
                                // get file content from edittext

                                byte[] fileContent = (etE01.getText().toString()).getBytes(StandardCharsets.UTF_8);
                                //String fileContent = etE01.getText().toString();
                                /*
                                boolean writeSuccess = writeByteToUri(uri, fileContent);
                                if (writeSuccess) {
                                    String message = "file written to external shared storage: " + uri.toString();
                                    etG04.setText(message);
                                } else {
                                    String message = "error on file writing to external shared storage: " + uri.toString();
                                    etG04.setText(message);
                                }

                                 */
/*
                            } catch (IOException e) {
                                e.printStackTrace();
                                etG04.setText("ERROR: " + e.toString());
                                return;*/
                           // }
                        }
                    }
                }
            });


    /*
    private boolean writeByteToUri(Uri uri, byte[] data) throws IOException {
        // this method needs Apache Commons IO dependency
        // implementation group: 'commons-io', name: 'commons-io', version: '2.11.0'
        try (OutputStream outputStream = contextSave.getContentResolver().openOutputStream(uri);) {
            IOUtils.writeChunked(data, outputStream);
            IOUtils.closeQuietly(outputStream);
            return true;
        } catch (Exception e) {
            System.out.println("*** EXCEPTION: " + e);
            return false;
        }
    }

     */

    /*
    codes for file encryption and decryption using Bouncy Castle and CipherInput/OutputStream

    implementation 'org.bouncycastle:bcprov-jdk15to18:1.72'

    char[] passphrase = "passphrase".toCharArray();
    int iterations = 5000; // iterations for pbkdf2

    // this way for adding bouncycastle to android
    // so wird BouncyCastle in Android eingebunden
    Security.removeProvider("BC"); // remove provider
    Security.addProvider(new BouncyCastleProvider()); // add new provider

    public static boolean encryptGcmFileBufferedCipherOutputStream(String inputFilename, String outputFilename, char[] password, int iterations)  {
        SecureRandom secureRandom = new SecureRandom();
        byte[] salt = new byte[32];
        secureRandom.nextBytes(salt);
        byte[] nonce = new byte[12];
        secureRandom.nextBytes(nonce);
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES/GCM/NOPadding", "BC");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException e) {
            e.printStackTrace();
        }
        try (FileInputStream in = new FileInputStream(inputFilename);
             FileOutputStream out = new FileOutputStream(outputFilename);
             CipherOutputStream encryptedOutputStream = new CipherOutputStream(out, cipher);) {
            out.write(nonce);
            out.write(salt);
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256", "BC");
            KeySpec keySpec = new PBEKeySpec(password, salt, iterations, 32 * 8); // 128 - 192 - 256
            byte[] key = secretKeyFactory.generateSecret(keySpec).getEncoded();
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(16 * 8, nonce);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gcmParameterSpec);
            byte[] buffer = new byte[8096];
            int nread;
            while ((nread = in.read(buffer)) > 0) {
                encryptedOutputStream.write(buffer, 0, nread);
            }
            encryptedOutputStream.flush();
        } catch (IOException | NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException | InvalidAlgorithmParameterException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        if (new File(outputFilename).exists()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean decryptGcmFileBufferedCipherInputStream(String inputFilename, String outputFilename, char[] password, int iterations) {
        byte[] salt = new byte[32];
        byte[] nonce = new byte[12];
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES/GCM/NOPadding", "BC");
        } catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException e) {
            e.printStackTrace();
        }

        try (FileInputStream in = new FileInputStream(inputFilename); // i don't care about the path as all is lokal
             CipherInputStream cipherInputStream = new CipherInputStream(in, cipher);
             FileOutputStream out = new FileOutputStream(outputFilename)) // i don't care about the path as all is lokal
        {
            byte[] buffer = new byte[8192];
            in.read(nonce);
            in.read(salt);
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256", "BC");
            KeySpec keySpec = new PBEKeySpec(password, salt, iterations, 32 * 8); // 128 - 192 - 256
            byte[] key = secretKeyFactory.generateSecret(keySpec).getEncoded();
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(16 * 8, nonce);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmParameterSpec);
            int nread;
            while ((nread = cipherInputStream.read(buffer)) > 0) {
                out.write(buffer, 0, nread);
            }
            out.flush();
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | InvalidKeySpecException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        if (new File(outputFilename).exists()) {
            return true;
        } else {
            return false;
        }
    }

     */


}