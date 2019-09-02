package com.etseib.ipsapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Instructions3Activity extends AppCompatActivity {
    private Button continueButton;
    private Button previousButton;
    private int numQuest;
    private boolean inGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions3);
        continueButton = findViewById(R.id.continueButton2);
        previousButton = findViewById(R.id.previousButton3);
        Bundle extras = getIntent().getExtras();
        numQuest = extras.getInt("com.etseib.ipsapp.questionNumber");
        inGame = extras.getBoolean("com.etseib.ipsapp.inGame");
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });
    }

    private void goBack(){
        Intent i = new Intent(getApplicationContext(), Instructions2Activity.class);
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
