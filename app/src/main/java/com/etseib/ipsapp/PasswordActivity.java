package com.etseib.ipsapp;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.etseib.ipsapp.QuestContract.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PasswordActivity extends AppCompatActivity {
    private EditText newPassword;
    private EditText confirmPassword;
    private Button changePassword;
    private SessionManager session;
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
        setContentView(R.layout.activity_password);
        newPassword = findViewById(R.id.newPassword);
        confirmPassword = findViewById(R.id.confirmPassword);
        changePassword = findViewById(R.id.changePassword);
        session = new SessionManager(getApplicationContext());

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPassword1 = newPassword.getText().toString();
                String newPassword2 = confirmPassword.getText().toString();
                if (newPassword1.equals(newPassword2)){
                    submit(newPassword1);
                } else{
                    alert.showAlertDialog(PasswordActivity.this, getString(R.string.failedPasswordChange), getString(R.string.notMatchingPasswords), false);
                }
            }
        });
    }

    private void submit(final String newPassword){
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("password", newPassword);
        JSONObject jsonBody = new JSONObject(params);

        String URL = BuildConfig.API_URL + "/mobile/changepassword";

        final String token = session.getToken();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL, jsonBody, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    String message = response.getString("message");
                    if(message.equals("Password changed successfully")){
                        DatabaseHelper admin = new DatabaseHelper(getApplicationContext());
                        SQLiteDatabase db = admin.getWritableDatabase();
                        ContentValues cv = new ContentValues();
                        cv.put(UsersTable.COL_CHANGE_PASSWORD, 0);
                        db.update(UsersTable.TABLE_NAME, cv,  UsersTable.COL_EMAIL + "= ?", new String[] {session.getEmail()});
                        db.close();
                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(i);
                        finish();
                    } else{
                        alert.showAlertDialog(PasswordActivity.this, getString(R.string.failedPasswordChange), getString(R.string.tryAgainLater), false);
                    }
                } catch (JSONException e) {
                    alert.showAlertDialog(PasswordActivity.this, getString(R.string.failedPasswordChange), getString(R.string.tryAgainLater), false);
                }
                //Log.i("VOLLEY", response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                alert.showAlertDialog(PasswordActivity.this, getString(R.string.failedPasswordChange), getString(R.string.tryAgainLater), false);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("access-token", token);
                return headers;
            }
        };
        int MY_SOCKET_TIMEOUT_MS = 7000;
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }
}
