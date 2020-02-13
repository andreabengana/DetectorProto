package com.example.thatboydre_35.detectorproto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Bundle;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
//import android.support.annotation.NonNull;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.soundcloud.android.crop.Crop;

import java.io.File;

import static com.example.thatboydre_35.detectorproto.InputActivity.PICK_IMAGE;
import static com.example.thatboydre_35.detectorproto.InputActivity.getPath;

public class ChooseImage extends AppCompatActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;

    // button for each available classifier
    private Button inceptionFloat;
    private Button haarCascade;
    private Button cameraCNN;

    // for permission requests
    public static final int REQUEST_PERMISSION = 300;

    // request code for permission requests to the os for image
    public static final int REQUEST_IMAGE = 100;

    // will hold uri of image obtained from camera
    private Uri imageUri;

    // string to send to next activity that describes the chosen classifier
    private String chosen;

    //boolean value dictating if chosen model is quantized version or not.
    private boolean quant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_image);



        inceptionFloat = (Button)findViewById(R.id.inception_float);
        inceptionFloat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // filename in assets
                chosen = "test_classifier_3.tflite";
                // model in not quantized
                quant = false;
                // open camera
                openCameraIntent();
            }
        });

        haarCascade = (Button) findViewById(R.id.haar_cascade);
        haarCascade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quant = true;
                // open camera
                openCameraIntent();
            }
        });

        cameraCNN = (Button) findViewById(R.id.opencamera);
        cameraCNN.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                quant = false;
                // open camera
                openCamera();
            }
        });
    }


    private void openCameraIntent(){

        Intent intent = new Intent();
        intent.setType("*/*");
        //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);

    }

    private void openCamera(){
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media._ID, "asdf");
        //values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        // tell camera where to store the resulting picture
        imageUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //start camera, and wait for it to finish
        startActivityForResult(intent, REQUEST_IMAGE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE) {
            Uri uriData = data.getData();
            String currentImagePath;
            currentImagePath = getPath(getApplicationContext(), uriData);
            Uri dest_uri = Uri.fromFile(new File(getCacheDir(), "cropped"));
            Crop.of(uriData, dest_uri).asSquare().start(this);
        }

        else if(requestCode == REQUEST_IMAGE && resultCode == RESULT_OK) {
            try {
                Uri source_uri = data.getData();
                Uri dest_uri = Uri.fromFile(new File(getCacheDir(), "cropped"));
                // need to crop it to square image as CNN's always required square input
                Crop.of(source_uri, dest_uri).asSquare().start(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // if cropping acitivty is finished, get the resulting cropped image uri and send it to 'Classify' activity
        else if(requestCode == Crop.REQUEST_CROP && resultCode == RESULT_OK){
            imageUri = Crop.getOutput(data);
            Intent i = new Intent(this, Classify.class);
            // put image data in extras to send
            i.putExtra("resID_uri", imageUri);
            // put filename in extras
            i.putExtra("chosen", chosen);
            // put model type in extras
            i.putExtra("quant", quant);
            // send other required data
            startActivity(i);
        }
    }
}
