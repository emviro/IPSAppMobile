package com.etseib.ipsapp;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;

import com.etseib.ipsapp.QuestContract.*;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;

public class EndActivity extends AppCompatActivity {
    private SessionManager session;
    private DatabaseHelper admin;
    private String[] files;
    private String[] answers;
    private Button sendAnswerButton;
    private EditText puzzleAnswer;
    private String correctAnswer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);
        session = new SessionManager(getApplicationContext());
        admin = new DatabaseHelper(this);
        puzzleAnswer = findViewById(R.id.puzzleAnswer);
        sendAnswerButton = findViewById(R.id.sendAnswerButton);

        SQLiteDatabase db = admin.getReadableDatabase();
        String query = "SELECT * FROM " + UsersTable.TABLE_NAME + " WHERE " +
                UsersTable.COL_EMAIL + " = ?";
        String[] selectionArgs = {session.getEmail()};
        Cursor cursor = db.rawQuery(query, selectionArgs);
        if (cursor.moveToFirst() && cursor.getCount()>0) {
            String[] seenImages = cursor.getString(cursor.getColumnIndex(UsersTable.COL_SEEN_IMAGES)).split(";");
            int len = seenImages.length;
            files  = new String[len];
            answers  = new String[len];
            for (int i=0; i<len; i++) {
                String query2 = "SELECT * FROM " + ConceptImagesTable.TABLE_NAME + " WHERE " +
                        ConceptImagesTable.COL_ID_CONCEPT_IMAGE + " = " + seenImages[i];
                Cursor cursor2 = db.rawQuery(query2, null);
                if (cursor2.moveToFirst() && cursor2.getCount()>0){
                    String image = cursor2.getString(cursor2.getColumnIndex(ConceptImagesTable.COL_IMAGE_NAME));
                    String _correctAnswer = getString(getResources().getIdentifier(image.replace(".","_"), "string", getPackageName()));
                    String correctAnswer = _correctAnswer.split("\\|")[0];
                    files[i] = image;
                    answers[i] = correctAnswer;
                } cursor2.close();
            }
        }
        cursor.close();
        db.close();
        sendAnswerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(answerIsCorrect()){
                    AlertDialog alertDialog = new AlertDialog.Builder(EndActivity.this).create();
                    alertDialog.setTitle(getString(R.string.correct));
                    Resources res = getResources();
                    String correctMessage = res.getString(R.string.incorrectConcept, correctAnswer);
                    alertDialog.setMessage(correctMessage);
                    alertDialog.setIcon(R.drawable.success);
                    alertDialog.setCancelable(false);
                    alertDialog.setButton(getString(R.string.seeResults), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(getApplicationContext(), ResultsActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                    alertDialog.show();
                } else{
                    AlertDialog alertDialog = new AlertDialog.Builder(EndActivity.this).create();
                    alertDialog.setTitle(getString(R.string.incorrect));
                    Resources res = getResources();
                    String incorrectMessage = res.getString(R.string.incorrectConcept, correctAnswer);
                    alertDialog.setMessage(incorrectMessage);
                    alertDialog.setIcon(R.drawable.fail);
                    alertDialog.setCancelable(false);
                    alertDialog.setButton(getString(R.string.seeResults), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(getApplicationContext(), ResultsActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                    alertDialog.show();
                }
                SQLiteDatabase db = admin.getWritableDatabase();
                ContentValues values2 = new ContentValues();
                values2.put(UsersTable.COL_IN_GAME, 0);
                db.update(UsersTable.TABLE_NAME, values2,UsersTable.COL_EMAIL + " = ?",
                        new String[] {session.getEmail()});
                db.close();
            }
        });

        GridView grid = findViewById(R.id.grid);
        grid.setAdapter(new ImageAdapter(this, files));
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), Image2Activity.class);
                intent.putExtra("com.etseib.ipsapp.assetName", files[i % files.length]);
                intent.putExtra("com.etseib.ipsapp.imageAnswer", answers[i % answers.length]);
                startActivity(intent);
            }
        });
    }

    private boolean answerIsCorrect(){
        SQLiteDatabase db = admin.getReadableDatabase();
        String query = "SELECT * FROM " + UsersTable.TABLE_NAME + " INNER JOIN " +
                ConceptsTable.TABLE_NAME + " ON "+ ConceptsTable.COL_ID_CONCEPT +
                " = " + UsersTable.COL_CURRENT_CONCEPT + " WHERE "+ UsersTable.COL_EMAIL + " = ?";
        String[] selectionArgs = {session.getEmail()};
        Cursor cursor = db.rawQuery(query, selectionArgs);
        String _correctAnswer="";
        if (cursor.moveToFirst() && cursor.getCount()>0) {
            String concept = cursor.getString(cursor.getColumnIndex(ConceptsTable.COL_CONCEPT));
            _correctAnswer = getString(getResources().getIdentifier(concept, "string", getPackageName()));
            correctAnswer = _correctAnswer.split("\\|")[0];
        }
        cursor.close();
        db.close();
        String nPuzzleAnswer = (Normalizer.normalize(puzzleAnswer.getText().toString(), Normalizer.Form.NFD));
        nPuzzleAnswer = nPuzzleAnswer.replaceAll("[^\\p{ASCII}]", "").toLowerCase();
        String nCorrectAnswer = (Normalizer.normalize(_correctAnswer, Normalizer.Form.NFD));
        nCorrectAnswer = nCorrectAnswer.replaceAll("[^\\p{ASCII}]", "").toLowerCase();
        List<String> correctAnswers = Arrays.asList(nCorrectAnswer.split("\\|"));
        return (correctAnswers.contains(nPuzzleAnswer));
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

}
