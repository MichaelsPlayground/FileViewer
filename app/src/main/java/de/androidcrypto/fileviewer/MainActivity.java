package de.androidcrypto.fileviewer;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    TextView readResult;

    private Context contextSave;
    private byte[] contentLoaded;

    private SharedPreferences sharedPreferences; // for text size storage
    private final String PREFERENCES_FILENAME = "shared_prefs";
    private final String TEXT_SIZE = "textsize";
    private int defaultTextSizeInDp = 6;
    private int MINIMUM_TEXT_SIZE_IN_DP = 3;

    private String dumpExportString;
    private String dumpFileName;
    private int MAXIMUM_FILE_SIZE = 20000; // be careful when increasing the maximum as it may block your UI and you get an "Application not responding" notice

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);
        contextSave = getApplicationContext();

        // for storage of the text size
        sharedPreferences = getSharedPreferences(PREFERENCES_FILENAME, Context.MODE_PRIVATE);

        readResult = findViewById(R.id.tvMainReadResult);
        readResult.setTextSize(coverPixelToDP(sharedPreferences.getInt(TEXT_SIZE, defaultTextSizeInDp)));
    }

    private void writeToUiAppend(TextView textView, String message) {
        runOnUiThread(() -> {
            String newString = textView.getText().toString() + "\n" + message;
            textView.setText(newString);
        });
    }

    private void writeToUiReverseAppend(TextView textView, String message) {
        runOnUiThread(() -> {
            String newString = message + "\n" + textView.getText().toString();
            textView.setText(newString);
        });
    }

    private void writeToUiToast(String message) {
        runOnUiThread(() -> {
            Toast.makeText(getApplicationContext(),
                    message,
                    Toast.LENGTH_SHORT).show();
        });
    }

    private int coverPixelToDP(int dps) {
        final float scale = this.getResources().getDisplayMetrics().density;
        return (int) (dps * scale);
    }

    private void showFileContent() {
        if (contentLoaded != null) {

            Thread DoDeleteGoogleDriveFile = new Thread() {
                public void run() {
                    StringBuilder sb = new StringBuilder();
                    int contentLoadedLength = contentLoaded.length;
                    //writeToUiAppend(readResult, "contentLoaded length: " + contentLoadedLength);
                    // processing in 8 byte chunks
                    int CHUNK_SIZE = 8;
                    int completeRounds = contentLoadedLength / CHUNK_SIZE;
                    //writeToUiAppend(readResult, "completeRounds (each 8 byte): " + completeRounds);
                    int lastBytes = contentLoadedLength - (completeRounds * CHUNK_SIZE);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            writeToUiAppend(readResult, "contentLoaded length: " + contentLoadedLength);
                            writeToUiAppend(readResult, "completeRounds (each 8 byte): " + completeRounds);
                            writeToUiAppend(readResult, "lastBytes: " + lastBytes);

                            //Toast.makeText(DeleteGoogleDriveFile.this, "selected file deleted", Toast.LENGTH_SHORT).show();
                        }
                    });


                    //writeToUiAppend(readResult, "lastBytes: " + lastBytes);
                    //StringBuilder sb = new StringBuilder();
                    for (int part = 0; part < completeRounds; part++) {
                        byte[] chunkPart = get8ByteChunk(part);
                        //System.out.println("part " + part + " : " + bytesToHex(chunkPart));
                        //sb.append(hexPrint((part * CHUNK_SIZE), chunkPart)).append("\n");
                        String chunkString = hexPrint((part * CHUNK_SIZE), chunkPart);
                        sb.append(chunkString).append("\n");

                        //writeToUiAppend(readResult, chunkString);

                    }
                    if (lastBytes > 0) {
                        byte[] lastChunkPart = getLastByteChunk(completeRounds, lastBytes);
                        //System.out.println("part " + completeRounds + " : " + bytesToHex(lastChunkPart));
                        String chunkString = hexPrint((completeRounds * CHUNK_SIZE), lastChunkPart);
                        sb.append(chunkString).append("\n");
                    }
                    final String completeString = sb.toString();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            writeToUiAppend(readResult, completeString);
                        }
                    });

                    //String completeOutput = sb.toString();
                    //writeToUiAppend(readResult, completeOutput);

                    // this is for processing the complete file
/*
                    if (contentLoaded.length < MAXIMUM_FILE_SIZE) {
                        //readResult.setTextSize(coverPixelToDP(textSizeInDp));
                        dumpExportString = HexDumpOwn.prettyPrint(contentLoaded, 0);
                        writeToUiAppend(readResult, dumpExportString);
                    } else {
                        String message = "The file is larger than the allowed content of " + MAXIMUM_FILE_SIZE + " bytes.";
                        writeToUiAppend(readResult, message);
                        //readResult.setText(message);
                        writeToUiToast(message);
                    }
*/

                }
            };
            DoDeleteGoogleDriveFile.start();

            // this is for processing the complete file
/*
            if (contentLoaded.length < MAXIMUM_FILE_SIZE) {
                //readResult.setTextSize(coverPixelToDP(textSizeInDp));
                dumpExportString = HexDumpOwn.prettyPrint(contentLoaded, 0);
                writeToUiAppend(readResult, dumpExportString);
            } else {
                String message = "The file is larger than the allowed content of " + MAXIMUM_FILE_SIZE + " bytes.";
                writeToUiAppend(readResult, message);
                //readResult.setText(message);
                writeToUiToast(message);
            }

 */
        }
    }

    private String hexPrint(int address, byte[] data) {
        // get hex address of part
        String hexAddress = formatWithNullsLeft(Integer.toHexString(address), 8) + ":";
        String hexContent = bytesToHexBlank(data);
        // add blanks depending on data length (7 = add 3 blanks, 6 = add 6 blanks
        for (int i = 0; i < (8 - data.length); i++) {
            hexContent += "   ";
        }
        String asciiRowString = "";
        for (int j = 0; j < data.length; j++) {
            // check for maximal characters
            asciiRowString = asciiRowString + returnPrintableChar(data[j], true);
        }
        String hexAscii = (char) 124 + formatWithBlanksRight(asciiRowString, 8);
        return hexAddress + hexContent + hexAscii;
    }

    public static String formatWithNullsLeft(String value, int len) {
        while (value.length() < len) {
            value = "0" + value;
        }
        return value;
    }

    public static String formatWithBlanksRight(String value, int len) {
        while (value.length() < len) {
            value += " ";
        }
        return value;
    }

    public static String bytesToHexBlank(byte[] bytes) {
        StringBuffer result = new StringBuffer();
        for (byte b : bytes)
            result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1)).append(" ");
        return result.toString();
    }

    public static char returnPrintableChar(byte inputByte, Boolean printDotBool) {
        // ascii-chars from these ranges are printed
        // 48 -  57 = 0-9
        // 65 -  90 = A-Z
        // 97 - 122 = a-z
        // if printDotBool = true then print a dot "."
        char returnChar = 0;
        if (printDotBool == true) {
            returnChar = 46;
        }
        if ((inputByte >= 48) && (inputByte <= 57)) {
            returnChar = (char) inputByte;
        }
        if ((inputByte >= 65) && (inputByte <= 90)) {
            returnChar = (char) inputByte;
        }
        if ((inputByte >= 97) && (inputByte <= 122)) {
            returnChar = (char) inputByte;
        }
        return returnChar;
    }


    private byte[] get8ByteChunk(int chunkPart) {
        // needs a global defined contentLoaded byte array
        return Arrays.copyOfRange(contentLoaded, (chunkPart * 8), ((chunkPart * 8) + 8));
    }

    private byte[] getLastByteChunk(int chunkPart, int numberLastBytes) {
        // needs a global defined contentLoaded byte array
        return Arrays.copyOfRange(contentLoaded, (chunkPart * 8), ((chunkPart * 8) + numberLastBytes));
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuffer result = new StringBuffer();
        for (byte b : bytes) result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }

    /**
     * section open a file
     */

    private void openFileFromExternalSharedStorage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        // Optionally, specify a URI for the file that should appear in the
        // system file picker when it loads.
        boolean pickerInitialUri = false;
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);
        fileOpenActivityResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> fileOpenActivityResultLauncher = registerForActivityResult(
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
                                contentLoaded = readBytesFromUri(uri);
                                //showFileContent();
                            } catch (IOException e) {
                                contentLoaded = null;
                                e.printStackTrace();
                                writeToUiToast("ERROR: " + e.toString());
                                return;
                            }
                        }
                    }
                }
            });

    private byte[] readBytesFromUri(Uri uri) throws IOException {
        if (contextSave != null) {
            ContentResolver contentResolver = contextSave.getContentResolver();
            String filename = queryName(contentResolver, uri);
            writeToUiAppend(readResult, "content of file " + filename);
            dumpFileName = filename;
            // warning: contextSave needs to get filled

            Thread DoBasicCreateFolder = new Thread() {
                public void run() {
                    try (InputStream inputStream = contentResolver.openInputStream(uri);
                         // this dynamically extends to take the bytes you read
                         ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();) {
                        // this is storage overwritten on each iteration with bytes
                        int bufferSize = 1024;
                        byte[] buffer = new byte[bufferSize];
                        // we need to know how may bytes were read to write them to the byteBuffer
                        int len = 0;
                        while ((len = inputStream.read(buffer)) != -1) {
                            byteBuffer.write(buffer, 0, len);
                        }
                        // and then we can return your byte array.
                        //return byteBuffer.toByteArray();
                        contentLoaded = byteBuffer.toByteArray();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showFileContent();
                                //Toast.makeText(DeleteGoogleDriveFile.this, "selected file deleted", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
            };
            DoBasicCreateFolder.start();

            /*
            try (InputStream inputStream = contentResolver.openInputStream(uri);
                 // this dynamically extends to take the bytes you read
                 ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();) {
                // this is storage overwritten on each iteration with bytes
                int bufferSize = 1024;
                byte[] buffer = new byte[bufferSize];
                // we need to know how may bytes were read to write them to the byteBuffer
                int len = 0;
                while ((len = inputStream.read(buffer)) != -1) {
                    byteBuffer.write(buffer, 0, len);
                }
                // and then we can return your byte array.
                return byteBuffer.toByteArray();
            }
            */
        }
        return null;
    }

    private String queryName(ContentResolver resolver, Uri uri) {
        Cursor returnCursor =
                resolver.query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }

    /**
     * section export and mail dump file
     */

    private void mailDumpFile() {
        if (dumpExportString.isEmpty()) {
            writeToUiToast("open a file first before sending emails :-)");
            return;
        }
        String subject = "Dump of file " + dumpFileName;
        String body = dumpExportString;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void exportDumpFile() {
        if (dumpExportString.isEmpty()) {
            writeToUiToast("open a file first before writing files :-)");
            return;
        }
        //verifyPermissionsWriteString();
        writeStringToExternalSharedStorage();
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
        String filename = dumpFileName + ".txt";
        // sanity check
        if (filename.equals("")) {
            writeToUiToast("scan a tag before writing the content to a file :-)");
            return;
        }
        intent.putExtra(Intent.EXTRA_TITLE, filename);
        fileSaveActivityResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> fileSaveActivityResultLauncher = registerForActivityResult(
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
                                String fileContent = dumpExportString;
                                writeTextToUri(uri, fileContent);
                                String message = "file written to external shared storage: " + uri.toString();
                                writeToUiToast("file written to external shared storage: " + uri.toString());
                            } catch (IOException e) {
                                e.printStackTrace();
                                writeToUiToast("ERROR: " + e.toString());
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
     * section on OptionsMenu
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);

        MenuItem mOpenFile = menu.findItem(R.id.action_open_file);
        mOpenFile.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //Intent i = new Intent(MainActivity.this, AddEntryActivity.class);
                //startActivity(i);
                readResult.setText("");
                dumpFileName = "";
                dumpExportString = "";
                openFileFromExternalSharedStorage();
                return false;
            }
        });

        MenuItem mPlusTextSize = menu.findItem(R.id.action_plus_text_size);
        mPlusTextSize.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int textSizeInDp = sharedPreferences.getInt(TEXT_SIZE, defaultTextSizeInDp) + 1;
                readResult.setTextSize(coverPixelToDP(textSizeInDp));
                System.out.println("textSizeInDp: " + textSizeInDp);
                try {
                    sharedPreferences.edit().putInt(TEXT_SIZE, textSizeInDp).apply();
                } catch (Exception e) {
                    writeToUiToast("Error on size storage: " + e.getMessage());
                    return false;
                }
                return false;
            }
        });

        MenuItem mMinusTextSize = menu.findItem(R.id.action_minus_text_size);
        mMinusTextSize.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int textSizeInDp = sharedPreferences.getInt(TEXT_SIZE, defaultTextSizeInDp) - 1;
                if (textSizeInDp < MINIMUM_TEXT_SIZE_IN_DP) {
                    writeToUiToast("You cannot decrease text size any further");
                    return false;
                }
                readResult.setTextSize(coverPixelToDP(textSizeInDp));
                try {
                    sharedPreferences.edit().putInt(TEXT_SIZE, textSizeInDp).apply();
                } catch (Exception e) {
                    writeToUiToast("Error on size storage: " + e.getMessage());
                    return false;
                }
                return false;
            }
        });

        MenuItem mExportDumpFile = menu.findItem(R.id.action_export_dump_file);
        mExportDumpFile.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                exportDumpFile();
                return false;
            }
        });

        MenuItem mMailDumpFile = menu.findItem(R.id.action_mail_dump_file);
        mMailDumpFile.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                mailDumpFile();
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

}