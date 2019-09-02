package com.etseib.ipsapp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.etseib.ipsapp.QuestContract.*;

public class MainActivity extends AppCompatActivity {
    private Button startButton;
    private TextView introduction;
    private TextView presentationText;
    private SessionManager session;
    private DatabaseHelper admin;
    private SQLiteDatabase db;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.logoutItem:
                session.logoutUser();
                return true;
            case R.id.instructionsItem:
                Intent newIntent = new Intent(getApplicationContext(), InstructionsActivity.class);
                newIntent.putExtra("com.etseib.ipsapp.questionNumber", 200); //so that intructions activity goes back to main when finishing
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

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        presentationText = findViewById(R.id.presentationText);
        introduction = findViewById(R.id.introduction);
        session = new SessionManager(getApplicationContext());
        admin = new DatabaseHelper(this);
        db = admin.getWritableDatabase();
        String email = session.getEmail();
        startButton = findViewById(R.id.startButton);
        prepareScreen(email);
    }
    public void prepareScreen(final String email) {
        String query = "SELECT * FROM " + UsersTable.TABLE_NAME + " WHERE " + UsersTable.COL_EMAIL + "= ?";
        String[] selectionArgs = {email};
        Cursor cursor = db.rawQuery(query, selectionArgs);
        if (cursor.moveToFirst() && cursor.getCount()>0) {
            String name = cursor.getString(cursor.getColumnIndex(UsersTable.COL_NAME));
            int status = cursor.getInt(cursor.getColumnIndex(UsersTable.COL_STATUS));
            final boolean inGame = cursor.getInt(cursor.getColumnIndex(UsersTable.COL_IN_GAME)) > 0;
            final int lastQuestion = cursor.getInt(cursor.getColumnIndex(UsersTable.COL_LAST_COMPLETED_QUESTION));
            cursor.close();
            Resources res = getResources();
            String formatted = res.getString(R.string.presentationText, name);
            presentationText.setText(formatted);
            if (status == 0 || status == 4) { //0: questionnaire not started, 4: completed, sent and game finished
                startButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String query2 = "SELECT * FROM " + ConceptsTable.TABLE_NAME + " ORDER BY RANDOM() LIMIT 1";
                        Cursor cursor2 = db.rawQuery(query2, null);
                        if (cursor2.moveToFirst() && cursor2.getCount()>0) {
                            int conceptId = cursor2.getInt(cursor2.getColumnIndex(ConceptsTable.COL_ID_CONCEPT));
                            ContentValues cv = new ContentValues();
                            cv.put(UsersTable.COL_CURRENT_CONCEPT, conceptId);
                            cv.put(UsersTable.COL_STATUS, 1);
                            cursor2.close();
                            String query3 = "SELECT * FROM " + ConceptsTable.TABLE_NAME + " INNER JOIN " +
                                    ConceptImagesTable.TABLE_NAME + " ON "+ ConceptsTable.COL_ID_CONCEPT +
                                    " = " + ConceptImagesTable.COL_ID_CONCEPT + " WHERE "+
                                    ConceptsTable.COL_ID_CONCEPT + " = " + conceptId + " ORDER BY RANDOM() LIMIT 1";
                            Cursor cursor3 = db.rawQuery(query3, null);
                            if (cursor3.moveToFirst() && cursor3.getCount()>0) {
                                int imageId = cursor3.getInt(cursor3.getColumnIndex(ConceptImagesTable.COL_ID_CONCEPT_IMAGE));
                                cv.put(UsersTable.COL_CURRENT_IMAGE, imageId);
                                cursor3.close();
                            } db.update(UsersTable.TABLE_NAME, cv,UsersTable.COL_EMAIL + " = ?",
                                    new String[] {session.getEmail()});
                        }
                        Intent newIntent = new Intent(getApplicationContext(), InstructionsActivity.class);
                        newIntent.putExtra("com.etseib.ipsapp.questionNumber", lastQuestion+1);
                        newIntent.putExtra("com.etseib.ipsapp.inGame", false);
                        startActivity(newIntent);
                        db.close();
                        finish();
                    }
                });
                if (status == 0){
                    introduction.setText(R.string.introduction);
                } else{
                    introduction.setText(R.string.introduction5);
                }
            } else if (status == 1) { //questionnaire started
                introduction.setText(R.string.introduction2);
                startButton.setText(R.string.resumeButton);
                if (inGame) {
                    startButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent newIntent = new Intent(getApplicationContext(), GameActivity.class);
                            newIntent.putExtra("com.etseib.ipsapp.addpiece", false);
                            newIntent.putExtra("com.etseib.ipsapp.num", lastQuestion);
                            startActivity(newIntent);
                            db.close();
                            finish();
                        }
                    });
                } else {
                    startButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent newIntent = new Intent(getApplicationContext(), QuestionActivity.class);
                            newIntent.putExtra("com.etseib.ipsapp.number", lastQuestion+1);
                            startActivity(newIntent);
                            db.close();
                            finish();
                        }
                    });
                }
            } else if (status == 2) { //questionnaire completed but not sent
                introduction.setText(R.string.introduction3);
                startButton.setText(R.string.sendButton);
                startButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        admin.sendTest(session);
                        db.close();
                    }
                });
            } else { //status = 3, questionnaire completed and sent but game not finished
                introduction.setText(R.string.introduction4);
                startButton.setText(R.string.access);
                startButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent newIntent = new Intent(getApplicationContext(), EndActivity.class);
                        startActivity(newIntent);
                        db.close();
                        finish();
                    }
                });
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
