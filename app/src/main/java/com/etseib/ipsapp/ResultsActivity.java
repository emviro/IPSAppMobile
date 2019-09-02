package com.etseib.ipsapp;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.etseib.ipsapp.QuestContract.*;

public class ResultsActivity extends AppCompatActivity {
    private DatabaseHelper admin;
    private SessionManager session;
    private TextView resultTitle;
    private TextView resultText;
    private Button exitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        TextView resultTitle = findViewById(R.id.resultTitle);
        TextView resultText = findViewById(R.id.resultText);
        Button exitButton = findViewById(R.id.exitButton);
        admin = new DatabaseHelper(this);
        session = new SessionManager(this);
        String email = session.getEmail();
        SQLiteDatabase db = admin.getWritableDatabase();
        String query2 = "SELECT * FROM " + UsersTable.TABLE_NAME + " WHERE " + UsersTable.COL_EMAIL + " = ?";
        String[] selectionArgs = {email};
        Cursor cursor = db.rawQuery(query2, selectionArgs);
        if (cursor.moveToFirst() && cursor.getCount() > 0) {
            int EB = cursor.getInt(cursor.getColumnIndex(UsersTable.COL_EB));
            double PB = cursor.getDouble(cursor.getColumnIndex(UsersTable.COL_PB));
            if (EB<=0){
                //INTERNAL ATTRIBUTOR
                resultTitle.setText(getString(R.string.internal));
                resultText.setText(getString(R.string.internalText));
            } else{
                if (PB>0.5){
                  //PERSONAL ATTRIBUTOR
                    resultTitle.setText(getString(R.string.personal));
                    resultText.setText(getString(R.string.personalText));
                } else{
                    //SITUATIONAL ATTRIBUTOR
                    resultTitle.setText(getString(R.string.situational));
                    resultText.setText(getString(R.string.situationalText));
                }
            }
        }
        ContentValues values = new ContentValues();
        values.put(UsersTable.COL_STATUS, 4);
        values.put(UsersTable.COL_CURRENT_CONCEPT, (Integer) null);
        values.put(UsersTable.COL_SEEN_IMAGES, (String) null);
        values.put(UsersTable.COL_EB, (Integer) null);
        values.put(UsersTable.COL_PB, (Integer) null);
        db.update(UsersTable.TABLE_NAME, values,UsersTable.COL_EMAIL + " = ?",
                new String[] {email});
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

}
