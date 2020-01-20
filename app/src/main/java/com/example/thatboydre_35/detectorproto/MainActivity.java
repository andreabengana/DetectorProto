package com.example.thatboydre_35.detectorproto;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    //private Button startButton;
    private Button newStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);

//        startButton = (Button) findViewById(R.id.startButton);
//        startButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startInputView();
//            }
//        });

        newStart = (Button) findViewById(R.id.newStart);
        newStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startInputView();
            }
        });
    }

    public void startInputView(){
        Intent intent = new Intent(this, ChooseImage.class);
        startActivity(intent);
    }
}
