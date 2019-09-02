package com.etseib.ipsapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

public class ImageActivity extends AppCompatActivity {
    private ImageView imageView;
    private Button nextButton;
    private TextView imageAnswer;
    private int numQuest;
    private String imageAnswerString;
    private DatabaseHelper admin;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        imageView = findViewById(R.id.imageView300);
        nextButton = findViewById(R.id.nextButton2);
        imageAnswer = findViewById(R.id.imageAnswer);
        admin = new DatabaseHelper(this);
        session = new SessionManager(this);
        Bundle extras = getIntent().getExtras();
        numQuest = extras.getInt("com.etseib.ipsapp.quest");
        imageAnswerString = extras.getString("com.etseib.ipsapp.imageanswer");
        final String assetName = extras.getString("com.etseib.ipsapp.image");
        imageAnswer.setText(imageAnswerString);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent;
                if (numQuest<32){
                    newIntent = new Intent(getApplicationContext(), QuestionActivity.class);
                    newIntent.putExtra("com.etseib.ipsapp.number", (numQuest + 1));
                    startActivity(newIntent);
                    finish();
                } else{
                    AlertDialog alertDialog = new AlertDialog.Builder(ImageActivity.this).create();
                    alertDialog.setTitle(getString(R.string.end));
                    alertDialog.setMessage(getString(R.string.endText3));
                    alertDialog.setCancelable(false);
                    alertDialog.setButton(getString(R.string._continue), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            admin.sendTest(session);
                        }
                    });
                    alertDialog.show();
                }
            }
        });

        // run image related code after the view was laid out
        // to have all dimensions calculated
        imageView.post(new Runnable() {
            @Override
            public void run() {
                if (assetName != null) {
                    setPicFromAsset(assetName);
                }
            }
        });

    }

    private void setPicFromAsset(String assetName) {
        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        AssetManager am = getAssets();
        try {
            InputStream is = am.open("img/" + assetName);
            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, new Rect(-1, -1, -1, -1), bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

            is.reset();

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            Bitmap bitmap = BitmapFactory.decodeStream(is, new Rect(-1, -1, -1, -1), bmOptions);
            imageView.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }


}
