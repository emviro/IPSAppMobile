package com.etseib.ipsapp;

import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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

public class ForgotPasswordActivity extends AppCompatActivity {
    private Button sendButton2;
    private EditText emailField2;
    private AlertDialogManager alert = new AlertDialogManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        emailField2 = findViewById(R.id.emailField2);
        sendButton2 = findViewById(R.id.sendButton2);
        sendButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailField2.getText().toString();
                submit(email);
            }
        });
    }

    private void submit(final String email){
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("email", email);
        JSONObject jsonBody = new JSONObject(params);

        String URL = BuildConfig.API_URL + "/mobile/forgotpassword";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL, jsonBody, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    String message = response.getString("message");
                    if (message.equals("Mail sent")){
                        DatabaseHelper admin = new DatabaseHelper(getApplicationContext());
                        SQLiteDatabase db = admin.getWritableDatabase();
                        ContentValues cv = new ContentValues();
                        cv.put("ChangePassword", 1);
                        db.update("Users", cv,  "Email = ?", new String[] {email});
                        db.close();
                        Resources res = getResources();
                        String mailSent = ((Resources) res).getString(R.string.mailSent2, email);
                        alert.showAlertDialog(ForgotPasswordActivity.this, getString(R.string.mailSent), mailSent, true);
                    }

                } catch (JSONException e) {
                    alert.showAlertDialog(ForgotPasswordActivity.this, getString(R.string.failedPetition), getString(R.string.genericError) + "internal error 1", false);

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
                        if (message.equals("User not found")){
                            alert.showAlertDialog(ForgotPasswordActivity.this, getString(R.string.failedPetition), getString(R.string.wrongEmail), false);
                        } else {
                            alert.showAlertDialog(ForgotPasswordActivity.this, getString(R.string.failedPetition), getString(R.string.genericError)+"internal error 3", false);
                        }
                    } else{
                        alert.showAlertDialog(ForgotPasswordActivity.this, getString(R.string.failedPetition), getString(R.string.serverError), false);
                    }
                } catch (UnsupportedEncodingException | JSONException e) {
                    alert.showAlertDialog(ForgotPasswordActivity.this, getString(R.string.failedPetition), getString(R.string.genericError)+"internal error 2", false);
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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
