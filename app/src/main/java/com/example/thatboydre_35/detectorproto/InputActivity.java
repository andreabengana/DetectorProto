package com.example.thatboydre_35.detectorproto;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class InputActivity extends AppCompatActivity {
    private ImageButton button;
    public static final int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        button = (ImageButton) findViewById(R.id.imageButton);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                //onActivityResult();
            }

        });

        /*@Override
        public void onActivityResult(int requestCode, int resultCode, Intent data)
        {
            if (requestCode == PICK_IMAGE) {
                Bitmap bitmap = data.getExtras().getParcelable("data");
            }
        }*/

    }
}
