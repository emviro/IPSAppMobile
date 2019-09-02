package com.etseib.ipsapp;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DURATION = 2000;
    SessionManager session;
    DatabaseHelper admin;
    private AlertDialogManager alert = new AlertDialogManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        session = new SessionManager(getApplicationContext());
        admin = new DatabaseHelper(this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (session.isLoggedIn()) {
                    validateToken();
                } else {
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
                //finish();
            }
        }, SPLASH_DURATION);
    }

    private void validateToken() {
        String URL = BuildConfig.API_URL + "/mobile/validatetoken";

        final String token = session.getToken();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    String message = response.getString("message");
                    if (message.equals("Valid token")) {
                        String email = session.getEmail();
                        if (admin.checkIfUserExists(email)) {
                            //if user has already registered
                            SQLiteDatabase db = admin.getReadableDatabase();
                            String query = "SELECT * FROM Users WHERE Email = ?";
                            String[] selectionArgs = {email};
                            Cursor cursor = db.rawQuery(query, selectionArgs);
                            if( cursor != null && cursor.moveToFirst() ) {
                                boolean passwordChange = (cursor.getInt(cursor.getColumnIndex("ChangePassword")) > 0);
                                if (passwordChange) {
                                    Intent i = new Intent(getApplicationContext(), PasswordActivity.class);
                                    startActivity(i);
                                } else {
                                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(i);
                                }
                                cursor.close();
                            }
                            db.close();
                        } else {
                            Intent intent = new Intent(SplashActivity.this, RegisterActivity.class);
                            startActivity(intent);
                        } finish();
                    }
                } catch (JSONException e) {
                    session.logoutUser();
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
                //Log.i("VOLLEY", response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                session.logoutUser();
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
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