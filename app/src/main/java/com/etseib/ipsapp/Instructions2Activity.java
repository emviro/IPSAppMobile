package com.etseib.ipsapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Instructions2Activity extends AppCompatActivity {
    private Button nextButton;
    private Button previousButton;
    private int numQuest;
    private boolean inGame;
    private Button skipButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions2);
        nextButton = findViewById(R.id.nextButton4);
        previousButton = findViewById(R.id.previousButton2);
        skipButton = findViewById(R.id.skipButton);

        Bundle extras = getIntent().getExtras();
        numQuest = extras.getInt("com.etseib.ipsapp.questionNumber");
        inGame = extras.getBoolean("com.etseib.ipsapp.inGame");
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), Instructions3Activity.class);
                i.putExtra("com.etseib.ipsapp.questionNumber", numQuest);
                i.putExtra("com.etseib.ipsapp.inGame", inGame);
                startActivity(i);
                finish();
            }
        });
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
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

    private void goBack(){
        Intent i = new Intent(getApplicationContext(), InstructionsActivity.class);
        i.putExtra("com.etseib.ipsapp.questionNumber", numQuest);
        i.putExtra("com.etseib.ipsapp.inGame", inGame);
        startActivity(i);
        finish();
    }

    @Override
    public void onBackPressed() {
        goBack();
    }

}
