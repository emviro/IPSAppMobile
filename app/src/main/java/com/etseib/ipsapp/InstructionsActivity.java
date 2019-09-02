package com.etseib.ipsapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class InstructionsActivity extends AppCompatActivity {
    private Button nextButton;
    private Button skipButton;
    private int numQuest;
    private boolean inGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);
        nextButton = findViewById(R.id.nextButton3);
        skipButton = findViewById(R.id.skipButton);
        Bundle extras = getIntent().getExtras();
        numQuest = extras.getInt("com.etseib.ipsapp.questionNumber");
        inGame = extras.getBoolean("com.etseib.ipsapp.ingame");
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), Instructions2Activity.class);
                i.putExtra("com.etseib.ipsapp.questionNumber", numQuest);
                i.putExtra("com.etseib.ipsapp.ingame", inGame);
                startActivity(i);
                finish();
            }
        });
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (numQuest==200){
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(i);
                    finish();
                } else{
                    if (inGame){
                        Intent i = new Intent(getApplicationContext(), GameActivity.class);
                        i.putExtra("com.etseib.ipsapp.num", numQuest);
                        startActivity(i);
                        finish();
                    } else{
                        Intent i = new Intent(getApplicationContext(), QuestionActivity.class);
                        i.putExtra("com.etseib.ipsapp.number", numQuest);
                        startActivity(i);
                        finish();
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent i;
        if (numQuest==200){
            i = new Intent(getApplicationContext(),MainActivity.class);
        } else{
            if (inGame){
                i = new Intent(getApplicationContext(), GameActivity.class);
                i.putExtra("com.etseib.ipsapp.num", numQuest);
            } else{
                i = new Intent(getApplicationContext(), QuestionActivity.class);
                i.putExtra("com.etseib.ipsapp.number", numQuest);
            }
        }
        startActivity(i);
        finish();
    }

}
