package com.example.thatboydre_35.detectorproto;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import android.Manifest;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

public class InputActivity extends AppCompatActivity {

    private static final Pattern IP_ADDRESS
            = Pattern.compile(
            "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
                    + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
                    + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
                    + "|[1-9][0-9]|[0-9]))");
    final int SELECT_MULTIPLE_IMAGES = 1;
    ArrayList<String> selectedImagesPaths; // Paths of the image(s) selected by the user.
    boolean imagesSelected = false; // Whether the user selected at least an image or not.

    private Button button;
    private Button predict;
    public static final int PICK_IMAGE = 1;
    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    protected Bitmap grayImg;

    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    public boolean checkPermissionREAD_EXTERNAL_STORAGE(
            final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context,
                            Manifest.permission.READ_EXTERNAL_STORAGE);

                } else {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }

    public void showDialog(final String msg, final Context context,
                           final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[] { permission },
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 2);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        setContentView(R.layout.activity_input);




        ImageView image = (ImageView) findViewById(R.id.imageView);
        button = (Button) findViewById(R.id.btnEvaluate);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                selectImage(v);

            }

        });

        predict = (Button) findViewById(R.id.btnPredict);
        predict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectServer(v);
            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(getApplicationContext(), "Access to Storage Permission Granted. Thanks.", Toast.LENGTH_SHORT).show();
                } else {
//                    Toast.makeText(getApplicationContext(), "Access to Storage Permission Denied.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case 2: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(getApplicationContext(), "Access to Internet Permission Granted. Thanks.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Access to Internet Permission Denied.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    public void connectServer(View v) {
        //TextView responseText = findViewById(R.id.responseText);

        if (imagesSelected == false) { // This means no image is selected and thus nothing to upload.
            //responseText.setText("No Image Selected to Upload. Select Image(s) and Try Again.");
            Toast.makeText(this, "No Image Selected to Upload. Select Image(s) and Try Again.", Toast.LENGTH_LONG).show();
            return;
        }
        //responseText.setText("Sending the Files. Please Wait ...");
        Toast.makeText(this, "Sending the Files. Please Wait ...", Toast.LENGTH_LONG).show();

//        EditText ipv4AddressView = findViewById(R.id.IPAddress);
//        String ipv4Address = ipv4AddressView.getText().toString();
//        EditText portNumberView = findViewById(R.id.portNumber);
//        String portNumber = portNumberView.getText().toString();

        String ipv4AddressView = "192.168.43.171";
        String ipv4Address = ipv4AddressView;
        String portNumberView = "5000";
        String portNumber = portNumberView;

        Matcher matcher = IP_ADDRESS.matcher(ipv4Address);
        if (!matcher.matches()) {
            //responseText.setText("Invalid IPv4 Address. Please Check Your Inputs.");
            Toast.makeText(this, "Invalid IPv4 Address. Please Check Your Inputs.", Toast.LENGTH_LONG).show();
            return;
        }

        String postUrl = "http://" + ipv4Address + ":" + portNumber + "/";

        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        for (int i = 0; i < selectedImagesPaths.size(); i++) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            try {
                // Read BitMap by file path.
                Bitmap bitmap = BitmapFactory.decodeFile(selectedImagesPaths.get(i), options);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            }catch(Exception e){
                //responseText.setText("Please Make Sure the Selected File is an Image.");
                Toast.makeText(this, "Please Make Sure the Selected File is an Image.", Toast.LENGTH_LONG).show();
                return;
            }
            byte[] byteArray = stream.toByteArray();

            multipartBodyBuilder.addFormDataPart("image", "Android_Flask_" + i + ".jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray));
        }

        RequestBody postBodyImage = multipartBodyBuilder.build();

//        RequestBody postBodyImage = new MultipartBody.Builder()
//                .setType(MultipartBody.FORM)
//                .addFormDataPart("image", "androidFlask.jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray))
//                .build();

        postRequest(postUrl, postBodyImage);
    }

    void postRequest(String postUrl, RequestBody postBody) {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Cancel the post on failure.
                call.cancel();
                Log.d("FAIL", e.getMessage());

                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        TextView responseText = findViewById(R.id.responseText);
//                        responseText.setText("Failed to Connect to Server. Please Try Again.");
//                    }
//                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView responseText = findViewById(R.id.responseText);
                        try {

                            responseText.setText(response.body().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    public void selectImage(View v) {
        Intent intent = new Intent();
        intent.setType("*/*");
        //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

//        try {
//            if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && null != data) {
//                // When a single image is selected.
//                String currentImagePath;
//                selectedImagesPaths = new ArrayList<>();
//                //TextView numSelectedImages = findViewById(R.id.numSelectedImages);
//                if (data.getData() != null) {
//                    Uri uri = data.getData();
//                    currentImagePath = getPath(getApplicationContext(), uri);
//                    Log.d("ImageDetails", "Single Image URI : " + uri);
//                    Log.d("ImageDetails", "Single Image Path : " + currentImagePath);
//                    selectedImagesPaths.add(currentImagePath);
//                    imagesSelected = true;
//                    //numSelectedImages.setText("Number of Selected Images : " + selectedImagesPaths.size());
//                } else {
//                    // When multiple images are selected.
//                    // Thanks tp Laith Mihyar for this Stackoverflow answer : https://stackoverflow.com/a/34047251/5426539
//                    if (data.getClipData() != null) {
//                        ClipData clipData = data.getClipData();
//                        for (int i = 0; i < clipData.getItemCount(); i++) {
//
//                            ClipData.Item item = clipData.getItemAt(i);
//                            Uri uri = item.getUri();
//
//                            currentImagePath = getPath(getApplicationContext(), uri);
//                            selectedImagesPaths.add(currentImagePath);
//                            Log.d("ImageDetails", "Image URI " + i + " = " + uri);
//                            Log.d("ImageDetails", "Image Path " + i + " = " + currentImagePath);
//                            imagesSelected = true;
//                            //numSelectedImages.setText("Number of Selected Images : " + selectedImagesPaths.size());
//                            Toast.makeText(this, "Number of Selected Images : " + selectedImagesPaths.size(), Toast.LENGTH_LONG).show();
//                        }
//                    }
//                }
//            } else {
//                Toast.makeText(this, "You haven't Picked any Image.", Toast.LENGTH_LONG).show();
//            }
//            Toast.makeText(getApplicationContext(), selectedImagesPaths.size() + " Image(s) Selected.", Toast.LENGTH_LONG).show();
//        } catch (Exception e) {
//            Toast.makeText(this, "Something Went Wrong." + e.toString(), Toast.LENGTH_LONG).show();
//            e.printStackTrace();
//        }

        if (requestCode == PICK_IMAGE) {
            Uri uriData = data.getData();

            String currentImagePath;
            selectedImagesPaths = new ArrayList<>();
            currentImagePath = getPath(getApplicationContext(), uriData);
            selectedImagesPaths.add(currentImagePath);
            imagesSelected = true;
            Toast.makeText(this, "Please Make Sure the Selected File is an Image.", Toast.LENGTH_LONG).show();
            ImageView image = (ImageView) findViewById(R.id.imageView);
            ImageView image2 = (ImageView) findViewById(R.id.imageView2);
            image.setImageURI(uriData);

            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);
            image.setColorFilter(new ColorMatrixColorFilter(matrix));

            BitmapDrawable drawable = (BitmapDrawable) image.getDrawable();
            Bitmap bitmap = drawable.getBitmap();
            image2.setImageBitmap(bitmap);
            /*if(image.getMaxWidth() < image.getMaxHeight()){
                image2.setMaxWidth(20);
                image2.setMaxHeight(20);
            }*/

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}

