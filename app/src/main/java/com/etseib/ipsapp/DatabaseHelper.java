package com.etseib.ipsapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.etseib.ipsapp.BuildConfig;

import com.etseib.ipsapp.QuestContract.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {
    private final static String DATABASE_NAME = "ipsapp";
    private final static int DATABASE_VERSION = 2;
    private Context context;
    //private SQLiteDatabase db;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //this.db = db;
        final String SQL_CREATE_USERS_TABLE = "CREATE TABLE " + UsersTable.TABLE_NAME + "(" +
                UsersTable.COL_EMAIL + " TEXT PRIMARY KEY, " +
                UsersTable.COL_NAME + " TEXT, " +
                UsersTable.COL_SEX + " TEXT, " +
                UsersTable.COL_CHANGE_PASSWORD + " BOOLEAN DEFAULT 1, " +
                UsersTable.COL_STATUS + " INT DEFAULT 0, " +
                UsersTable.COL_LAST_COMPLETED_QUESTION + " INT DEFAULT 0, " +
                UsersTable.COL_IN_GAME + " BOOLEAN DEFAULT 0, " +
                UsersTable.COL_CURRENT_CONCEPT + " INT, " +
                UsersTable.COL_EB + " INT, " +
                UsersTable.COL_PB + " REAL, " +
                UsersTable.COL_SEEN_IMAGES + " TEXT, " +
                UsersTable.COL_CURRENT_IMAGE + " INT, " +
                UsersTable.COL_SEEN_PIECES + " TEXT)";
        final String SQL_CREATE_ANSWERS_TABLE = "CREATE TABLE " + AnswersTable.TABLE_NAME + "(" +
                AnswersTable.COL_EMAIL + " TEXT, " +
                AnswersTable.COL_QUESTION + " INTEGER, " +
                AnswersTable.COL_ANSWER + " INTEGER, " +
                AnswersTable.COL_CAUSE + " TEXT, " +
                AnswersTable.COL_PERCENTAGE + " INTEGER, " +
                "FOREIGN KEY (" + AnswersTable.COL_EMAIL + ") REFERENCES " +
                UsersTable.TABLE_NAME + "(" + UsersTable.COL_EMAIL + ")," +
                "PRIMARY KEY(" + AnswersTable.COL_EMAIL + "," + AnswersTable.COL_QUESTION + "))";
        final String SQL_CREATE_CONCEPTS_TABLE = "CREATE TABLE " + ConceptsTable.TABLE_NAME + "(" +
                ConceptsTable.COL_ID_CONCEPT + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ConceptsTable.COL_CONCEPT + " TEXT)";
        final String SQL_CREATE_CONCEPT_IMAGES_TABLE = "CREATE TABLE " + ConceptImagesTable.TABLE_NAME + "(" +
                ConceptImagesTable.COL_ID_CONCEPT_IMAGE + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ConceptImagesTable.COL_ID_CONCEPT + " INTEGER, " +
                ConceptImagesTable.COL_IMAGE_NAME + " TEXT, " +
                "FOREIGN KEY (" + ConceptImagesTable.COL_ID_CONCEPT + ") REFERENCES " +
                ConceptsTable.TABLE_NAME + "(" + ConceptsTable.COL_ID_CONCEPT + "))";
        db.execSQL(SQL_CREATE_USERS_TABLE);
        db.execSQL(SQL_CREATE_ANSWERS_TABLE);
        db.execSQL(SQL_CREATE_CONCEPTS_TABLE);
        db.execSQL(SQL_CREATE_CONCEPT_IMAGES_TABLE);

        String[] concepts = {"summer", "technology", "animals"};
        String[][] images = {{"sunflower.jpg", "watermelon.jpg", "beach.jpg", "floater.jpg", "palmtree.jpg",
                "icecream.jpg", "sunglasses.jpg", "umbrella.jpg", "flamingo.jpg", "pineapple.jpg",
                "swimmingpool.jpg", "bee.jpg", "berries.jpg", "bikini.jpg", "coral.jpg", "daisy.jpg",
                "deckchair.jpg", "diver.jpg", "fan.jpg", "flipflops.jpg", "hammock.jpg", "hat.jpg",
                "ice.jpg", "lavander.jpg", "lemonade.jpg", "sailboat.jpg", "shells.jpg",
                "skirt.jpg", "sun.jpg", "surfer.jpg", "trafficjam.jpg", "wheatmower.jpg"
        },
                {"camera.jpg", "car.jpg", "drone.jpg", "elevator.jpg", "headphones.jpg", "television.jpg",
                        "mouse.jpg", "microphone.jpg","robot.jpg", "smartphone.jpg", "washingmachine.jpg", "speaker.jpg",
                "coffeemachine.jpg", "computer.jpg", "lightbulb.jpg", "fan.jpg", "airplane.jpg", "alarmclock.jpg",
                        "fridge.jpg", "keyboard.jpg", "laser.jpg", "microscope.jpg", "printer.jpg", "projector.jpg", "radiography.jpg",
                        "rocket.jpg", "telephone.jpg", "telescope.jpg", "train.jpg", "turbine.jpg", "turntable.jpg", "watch.jpg"
                },
                {"dog.jpg", "elephant.jpg", "lion.jpg", "tiger.jpg", "turtle.jpg", "ant.jpg", "duck.jpg",
                        "hamster.jpg", "eagle.jpg", "parrot.jpg", "squirrel.jpg", "flamingo.jpg", "butterfly.jpg",
                        "reindeer.jpg", "fox.jpg", "jellyfish.jpg", "frog.jpg", "penguin.jpg", "monkey.jpg", "rooster.jpg",
                "bee.jpg", "bear.jpg", "camel.jpg", "cow.jpg", "crab.jpg", "dolphin.jpg", "hippopotamus.jpg", "horse.jpg",
                        "koala.jpg", "owl.jpg", "pig.jpg", "rabbit.jpg", "seal.jpg", "snake.jpg"
                }};
        for (int i = 0; i< concepts.length; i++) {
            ContentValues values = new ContentValues();
            values.put(ConceptsTable.COL_CONCEPT, concepts[i]);
            db.insert(ConceptsTable.TABLE_NAME, null, values);
            String query = "SELECT * FROM " + ConceptsTable.TABLE_NAME + " WHERE " + ConceptsTable.COL_CONCEPT + "= ?";
            String[] selectionArgs = {concepts[i]};
            Cursor cursor = db.rawQuery(query, selectionArgs);
            if (cursor.moveToFirst() && cursor.getCount() > 0) {
                int conceptId = cursor.getInt(cursor.getColumnIndex(ConceptsTable.COL_ID_CONCEPT));
                for (int j = 0; j < images[i].length; j++) {
                    ContentValues values2 = new ContentValues();
                    values2.put(ConceptImagesTable.COL_IMAGE_NAME, images[i][j]);
                    values2.put(ConceptImagesTable.COL_ID_CONCEPT, conceptId);
                    db.insert(ConceptImagesTable.TABLE_NAME, null, values2);
                }
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + UsersTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + AnswersTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ConceptImagesTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ConceptsTable.TABLE_NAME);
        onCreate(db);
    }

    public boolean checkIfUserExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + UsersTable.TABLE_NAME + " WHERE " +
                UsersTable.COL_EMAIL + " = ?";
        String[] selectionArgs = {email};
        Cursor cursor = db.rawQuery(query, selectionArgs);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count > 0;
    }

    public boolean getTestState(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] selectionArgs = {email};
        for (int i=1; i<33; ++i){
            String query = "SELECT * FROM " + AnswersTable.TABLE_NAME + " WHERE " + AnswersTable.COL_EMAIL +
                    " = ? AND " + AnswersTable.COL_QUESTION + " = " + i;
            Cursor cursor = db.rawQuery(query, selectionArgs);
            if (cursor.moveToFirst()) {
                int answer = cursor.getInt(cursor.getColumnIndex(AnswersTable.COL_ANSWER));
                int percentage = cursor.getInt(cursor.getColumnIndex(AnswersTable.COL_PERCENTAGE));
                String cause = cursor.getString(cursor.getColumnIndex(AnswersTable.COL_CAUSE));
                if (TextUtils.isEmpty(cause) || (answer == 0) || (percentage == 0)) {
                    cursor.close();
                    db.close();
                    return false;
                }
            } else{
                cursor.close();
                db.close();
                return false;
            } cursor.close();
        }
        db.close();
        return true;
    }

    public void sendTest(SessionManager session) {
        final String email = session.getEmail();
        SQLiteDatabase db = this.getReadableDatabase();

        List<Integer> answers = new ArrayList<>(32);
        List<String> causes = new ArrayList<>(32);
        List<Integer> percentages = new ArrayList<>(32);

        String[] selectionArgs = {email};
        for (int i=1; i<33; ++i){
            String query = "SELECT * FROM " + AnswersTable.TABLE_NAME + " WHERE " + AnswersTable.COL_EMAIL +
                    " = ? AND " + AnswersTable.COL_QUESTION + " = " + i;
            Cursor cursor = db.rawQuery(query, selectionArgs);
            if (cursor.moveToFirst() && cursor.getCount()>0){
                int percentage = cursor.getInt(cursor.getColumnIndex(AnswersTable.COL_PERCENTAGE));
                int answer = cursor.getInt(cursor.getColumnIndex(AnswersTable.COL_ANSWER));
                String cause = cursor.getString(cursor.getColumnIndex(AnswersTable.COL_CAUSE));
                //int percentage = cursor.getInt(cursor.getColumnIndex(AnswersTable.COL_PERCENTAGE));
                answers.add(answer);
                causes.add(cause);
                percentages.add(percentage);
                cursor.close();
            }
        }
        db.close();

        HashMap<String, List> params = new HashMap<String, List>();
        params.put("answers", answers);
        params.put("causes", causes);
        params.put("percentages", percentages);
        JSONObject jsonBody = new JSONObject(params);

        String URL = BuildConfig.API_URL + "/mobile/saveanswers";

        final String token = session.getToken();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL, jsonBody, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    String message = response.getString("message");
                    String[] whereArgs = new String[]{email};
                    if(message.equals("Saved test")){
                        int EB = response.getInt("EB");
                        double PB = response.getDouble("PB");
                        SQLiteDatabase db2 = DatabaseHelper.this.getWritableDatabase();
                        ContentValues values = new ContentValues();
                        values.put(UsersTable.COL_EB, EB);
                        values.put(UsersTable.COL_PB, PB);
                        values.put(UsersTable.COL_STATUS, 3);
                        values.put(UsersTable.COL_CURRENT_IMAGE, (Integer) null);
                        values.put(UsersTable.COL_LAST_COMPLETED_QUESTION, 0);
                        values.put(UsersTable.COL_SEEN_PIECES, (String) null);
                        db2.update(UsersTable.TABLE_NAME, values,UsersTable.COL_EMAIL + " = ?",
                                new String[] {email});
                        //delete all answers from mobile database
                        String whereClause = AnswersTable.COL_EMAIL + " =? ";
                        db2.delete(AnswersTable.TABLE_NAME, whereClause, whereArgs);
                        db2.close();
                        Intent intent = new Intent(context, EndActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    } else{
                        Toast.makeText(context, "Internal error (3)", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(context, "Server error", Toast.LENGTH_LONG).show();
                }
                //Log.i("VOLLEY", response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    if (error.networkResponse != null) {
                        String errorMessage = new String(error.networkResponse.data, "utf-8");
                        JSONObject data = new JSONObject(errorMessage);
                        String message = data.optString("message");
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                    } else{
                        Toast.makeText(context, "Server error", Toast.LENGTH_LONG).show();
                    }
                } catch (UnsupportedEncodingException | JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Internal error (2)", Toast.LENGTH_LONG).show();
                }
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
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(jsonObjectRequest);
        db.close();
    }
}
