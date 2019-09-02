package com.etseib.ipsapp;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {
    private EditText emailField;
    private EditText passwordField;
    private Button logInButton;
    private Button forgotPassword;
    private SessionManager session;
    private DatabaseHelper admin;
    private AlertDialogManager alert = new AlertDialogManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        logInButton = findViewById(R.id.logInButton);
        forgotPassword = findViewById(R.id.forgotPassword);
        session = new SessionManager(getApplicationContext());
        admin = new DatabaseHelper(this);

        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailField.getText().toString();
                String password = passwordField.getText().toString();
                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
                    alert.showAlertDialog(LoginActivity.this, getString(R.string.failedLogIn), getString(R.string.emptyCredential), false);
                } else{
                    submit(email, password);
                }
            }
        });
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void submit(final String email, final String password){
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("email", email);
        params.put("password", password);
        JSONObject jsonBody = new JSONObject(params);

        String URL = BuildConfig.API_URL + "/mobile/login";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL, jsonBody, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    String token = response.getString("token");
                    if (admin.checkIfUserExists(email)) {
                        SQLiteDatabase db = admin.getReadableDatabase();
                        String query = "SELECT * FROM Users WHERE Email = ?";
                        String[] selectionArgs = {email};
                        Cursor fila = db.rawQuery(query, selectionArgs);
                        if( fila != null && fila.moveToFirst() ) {
                            boolean passwordChange = (fila.getInt(fila.getColumnIndex("ChangePassword")) > 0);
                            if (passwordChange) {
                                Intent i = new Intent(getApplicationContext(), PasswordActivity.class);
                                startActivity(i);
                            } else {
                                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(i);
                            }
                        } fila.close();
                        db.close();
                    } else {
                        Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
                        startActivity(i);
                    }
                    session.createLoginSession(email, token);
                    finish();

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_LONG).show();

                }
                //Log.i("VOLLEY", response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    if (error.networkResponse != null){
                        String errorMessage = new String(error.networkResponse.data, "utf-8");
                        JSONObject data= new JSONObject(errorMessage);
                        String message = data.optString("message");
                        if (message.equals("Invalid email")){
                            alert.showAlertDialog(LoginActivity.this, getString(R.string.failedLogIn), getString(R.string.wrongEmail), false);
                        } else {
                            alert.showAlertDialog(LoginActivity.this, getString(R.string.failedLogIn), getString(R.string.wrongCredential), false);
                        }
                    } else{
                        Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_LONG).show();
                    }
                } catch (UnsupportedEncodingException | JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Internal error (1)", Toast.LENGTH_LONG).show();
                }

                //Log.v("VOLLEY", error.toString());
            }
        });
        int MY_SOCKET_TIMEOUT_MS = 7000;
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    /*@Override
    protected void onStop() {
        super.onStop();
        if (requestQueue != null) {
            requestQueue.cancelAll(REQ_TAG);
        }*/
}
