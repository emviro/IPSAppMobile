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

import com.etseib.ipsapp.QuestContract.*;

public class RegisterActivity extends AppCompatActivity {
    private RadioGroup sexGroup;
    private Button nextButton;
    private EditText nameField;
    private SessionManager session;
    private DatabaseHelper admin;
    private String sex;
    private String name;
    private AlertDialogManager alert = new AlertDialogManager();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.logoutItem:
                session.logoutUser();
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
    public boolean onPrepareOptionsMenu (Menu menu) {
        menu.findItem(R.id.instructionsItem).setEnabled(false);
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        sexGroup = findViewById(R.id.sexGroup);
        nextButton = findViewById(R.id.nextButton);
        nameField = findViewById(R.id.nameField);
        session = new SessionManager(getApplicationContext());
        admin = new DatabaseHelper(getApplicationContext());

        sexGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.maleButton){
                    sex = "male";
                } else if (checkedId == R.id.femaleButton){
                    sex = "female";
                }
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = nameField.getText().toString();
                Intent startIntent = new Intent(getApplicationContext(), PasswordActivity.class);
                if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(sex)) {
                    String email = session.getEmail();
                    insertUser(name, sex, email); //inserts user and images
                    startActivity(startIntent);
                    finish();
                } else{
                    alert.showAlertDialog(RegisterActivity.this, getString(R.string.failedRegister), getString(R.string.emptyCredential), false);
                }
            }
        });
    }

    private void insertUser(String name, String sex, String email) {
        SQLiteDatabase db = admin.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(UsersTable.COL_NAME, name);
        values.put(UsersTable.COL_SEX, sex);
        values.put(UsersTable.COL_EMAIL, email);
        db.insert(UsersTable.TABLE_NAME, null, values);
        db.close();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
