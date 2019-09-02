package com.etseib.ipsapp;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import com.etseib.ipsapp.QuestContract.*;

public class QuestionActivity extends AppCompatActivity {
    private RadioGroup answerGroup;
    private EditText causeField;
    private TextView number;
    private TextView question;
    private SeekBar percentBar;
    private TextView percentBox;
    private Button nextButton;
    private int value;
    private SessionManager session;
    private DatabaseHelper admin;
    private int answer = 0;
    private String cause = "";
    private String email;
    private int numQuest;

    private AlertDialogManager alert = new AlertDialogManager();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.logoutItem:
                session.logoutUser();
                return true;
            case R.id.instructionsItem:
                Intent newIntent = new Intent(getApplicationContext(), InstructionsActivity.class);
                newIntent.putExtra("com.etseib.ipsapp.questionNumber", numQuest);
                newIntent.putExtra("com.etseib.ipsapp.inGame", false);
                startActivity(newIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.dropdown_menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        question = findViewById(R.id.question);
        number = findViewById(R.id.number);
        nextButton = findViewById(R.id.nextButton);
        causeField = findViewById(R.id.causeField);
        percentBar = findViewById(R.id.percentBar);
        percentBox = findViewById(R.id.percentBox);
        answerGroup = findViewById(R.id.answerGroup);
        session = new SessionManager(getApplicationContext());
        admin = new DatabaseHelper(getApplicationContext());

        Bundle extras = getIntent().getExtras();
        numQuest = extras.getInt("com.etseib.ipsapp.number");
        email = session.getEmail();

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cause = causeField.getText().toString();
                insertAnswer(email, numQuest, cause, answer, value);
            }
        });

        showQuestion(numQuest);

        answerGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.answerA){
                    answer = 1;
                } else if (checkedId == R.id.answerB){
                    answer = 2;
                } else if (checkedId == R.id.answerC){
                    answer = 3;
                }
            }
        });
        percentBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onStopTrackingTouch(SeekBar bar) {
                value = bar.getProgress(); // the value of the seekBar progress
            }

            public void onStartTrackingTouch(SeekBar bar) {

            }

            public void onProgressChanged(SeekBar bar,
                                          int paramInt, boolean paramBoolean) {
                percentBox.setText("" + paramInt + "%"); // here in textView the percent will be shown
            }
        });
    }

    private void showQuestion(final int numQuest) {
        if (numQuest < 33 && numQuest>0) {
            number.setText(numQuest + "");
            int resId = getResources().getIdentifier("q" + (numQuest + ""), "string", getPackageName());
            question.setText(getString(resId));
            answerGroup.clearCheck();
            causeField.clearComposingText();
            percentBox.setText("0%");
            percentBar.setProgress(0);
            answer = 0;
            cause = "";
        }
        if(numQuest==32){
            nextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SQLiteDatabase db = admin.getWritableDatabase();
                    ContentValues values2 = new ContentValues();
                    values2.put(UsersTable.COL_STATUS, 2);
                    db.update(UsersTable.TABLE_NAME, values2,UsersTable.COL_EMAIL + " = ?",
                            new String[] {session.getEmail()});
                    db.close();
                    cause = causeField.getText().toString();
                    insertAnswer(email, numQuest, cause, answer, value);
                }
            });
        }
    }

    private void insertAnswer(String email, int question, String cause, int answer, int percentage) {
        if ((!TextUtils.isEmpty(cause)) && (answer != 0) && (percentage !=0)) {
            SQLiteDatabase db = admin.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(AnswersTable.COL_EMAIL, email);
            values.put(AnswersTable.COL_QUESTION, question);
            values.put(AnswersTable.COL_CAUSE, cause);
            values.put(AnswersTable.COL_ANSWER, answer);
            values.put(AnswersTable.COL_PERCENTAGE, percentage);
            db.insert(AnswersTable.TABLE_NAME, null, values);
            ContentValues values2 = new ContentValues();
            values2.put(UsersTable.COL_IN_GAME, 1);
            values2.put(UsersTable.COL_LAST_COMPLETED_QUESTION, question);
            db.update(UsersTable.TABLE_NAME, values2,UsersTable.COL_EMAIL + " = ?",
                    new String[] {session.getEmail()});
            db.close();
            Intent i = new Intent(getApplicationContext(), GameActivity.class);
            i.putExtra("com.etseib.ipsapp.num", numQuest);
            i.putExtra("com.etseib.ipsapp.addpiece", true);
            startActivity(i);
            finish();
        } else{
            alert.showAlertDialog(QuestionActivity.this, getString(R.string.failedNext), getString(R.string.writeAll), false);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }
}