package com.etseib.ipsapp;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.etseib.ipsapp.QuestContract.*;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static java.lang.StrictMath.abs;

public class GameActivity extends AppCompatActivity implements AlertDialogPuzzle.DialogListener {
    private Button yesButton;
    private Button noButton;
    private ImageView imageView;
    ArrayList<PuzzlePiece> pieces;
    private int numQuest;
    private boolean addPiece;
    private DatabaseHelper admin;
    private SessionManager session;
    private String email;
    private List<PuzzlePiece> currentPieces;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.logoutItem:
                session.logoutUser();
                return true;
            case R.id.instructionsItem:
                Intent newIntent = new Intent(getApplicationContext(), InstructionsActivity.class);
                newIntent.putExtra("com.etseib.ipsapp.questionNumber", numQuest);
                newIntent.putExtra("com.etseib.ipsapp.inGame", true);
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
        setContentView(R.layout.activity_game);
        yesButton = findViewById(R.id.yesButton);
        noButton = findViewById(R.id.noButton);
        imageView = findViewById(R.id.imageView400);
        admin = new DatabaseHelper(this);
        final RelativeLayout layout = findViewById(R.id.layout);
        session = new SessionManager(getApplicationContext());
        email = session.getEmail();

        Bundle extras = getIntent().getExtras();
        numQuest = extras.getInt("com.etseib.ipsapp.num");
        addPiece = extras.getBoolean("com.etseib.ipsapp.addpiece");

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePuzzleState();
                if (currentPieces.size() == 9) {
                    String title = getString(R.string.unguessed);
                    int message;
                    if (numQuest < 32) {
                        message = R.string.incorrectText2;
                    } else {
                        message = R.string.endText4;
                    }
                    updateAndSeeImage(title, message, true);
                } else{
                    if (numQuest < 32) {
                        Intent newIntent = new Intent(getApplicationContext(), QuestionActivity.class);
                        newIntent.putExtra("com.etseib.ipsapp.number", (numQuest + 1));
                        startActivity(newIntent);
                        finish();
                    } else {
                        AlertDialog alertDialog = new AlertDialog.Builder(GameActivity.this).create();
                        alertDialog.setTitle(getString(R.string.end));
                        alertDialog.setMessage(getString(R.string.endText3));
                        alertDialog.setCancelable(false);
                        alertDialog.setButton(getString(R.string._continue), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                admin.sendTest(session);
                            }
                        });
                        alertDialog.show();
                    }
                }
            }
        });

        // run image related code after the view was laid out
        // to have all dimensions calculated
        imageView.post(new Runnable() {
            @Override
            public void run() {
                setImage(email);
                pieces = splitImage();
                SQLiteDatabase db = admin.getWritableDatabase();
                String query = "SELECT * FROM " + UsersTable.TABLE_NAME + " WHERE " +
                        UsersTable.COL_EMAIL + " = ? AND " + UsersTable.COL_SEEN_PIECES + " IS NOT NULL";
                String[] selectionArgs = {email};
                Cursor cursor = db.rawQuery(query, selectionArgs);
                int r1;
                currentPieces = new ArrayList<>();
                if (cursor.moveToFirst() && cursor.getCount() > 0) {
                    String indexesString = cursor.getString(cursor.getColumnIndex(UsersTable.COL_SEEN_PIECES));
                    String[] indexes = indexesString.split(";");
                    currentPieces = new ArrayList<>();
                    for (String index : indexes) {
                        currentPieces.add(pieces.get(Integer.parseInt(index)));
                    }
                    cursor.close();
                    // only add piece if coming from a question, not if coming from main
                    if (addPiece) {
                        do {
                            Random r = new Random();
                            r1 = r.nextInt(9);
                        } while (indexesString.contains(r1 + ""));
                        String newIndexes = indexesString + r1 + ";";
                        addPiece(db, newIndexes, r1);
                    }
                } else {
                    Random r = new Random();
                    r1 = r.nextInt(9);
                    String newIndexes = r1 + ";";
                    addPiece(db, newIndexes, r1);
                }
                db.close();
                for (PuzzlePiece piece : currentPieces) {
                    RelativeLayout.LayoutParams lParams = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    lParams.leftMargin = piece.xCoord;
                    lParams.topMargin = piece.yCoord;
                    layout.addView(piece, lParams);
                }
            }
        });
    }

    private void addPiece(SQLiteDatabase db, String newIndexes, int r1){
        currentPieces.add(pieces.get(r1));
        ContentValues values = new ContentValues();
        values.put(UsersTable.COL_SEEN_PIECES, newIndexes);
        db.update(UsersTable.TABLE_NAME, values,UsersTable.COL_EMAIL + " = ?",
                new String[] {session.getEmail()});
    }

    private void setImage(String email){
        SQLiteDatabase db = admin.getWritableDatabase();
        String query = "SELECT * FROM " + UsersTable.TABLE_NAME + " INNER JOIN " +
                ConceptImagesTable.TABLE_NAME + " ON "+ ConceptImagesTable.COL_ID_CONCEPT_IMAGE +
                " = " + UsersTable.COL_CURRENT_IMAGE + " WHERE "+ UsersTable.COL_EMAIL + " = ?";
        String[] selectionArgs = {email};
        Cursor cursor = db.rawQuery(query, selectionArgs);
        if (cursor.moveToFirst() && cursor.getCount()>0){
            String image = cursor.getString(cursor.getColumnIndex(ConceptImagesTable.COL_IMAGE_NAME));
            setPicFromAsset(image);
        } else{
            //select random image from images table and set it in the ImageView
            String query2 = "SELECT * FROM " + UsersTable.TABLE_NAME + " INNER JOIN " +
                    ConceptImagesTable.TABLE_NAME + " ON "+ ConceptImagesTable.COL_ID_CONCEPT +
                    " = " + UsersTable.COL_CURRENT_CONCEPT + " WHERE "+
                    UsersTable.COL_EMAIL + " = ? ORDER BY RANDOM()";
            String[] selectionArgs2 = {email};
            Cursor cursor2 = db.rawQuery(query2, selectionArgs2);
            if (cursor2.moveToFirst() && cursor2.getCount()>0){
                String seenImages = cursor2.getString(cursor2.getColumnIndex(UsersTable.COL_SEEN_IMAGES));
                String[] seenImagesArray = seenImages.split(";");
                int imageId;
                String image;
                do{
                    image = cursor2.getString(cursor2.getColumnIndex(ConceptImagesTable.COL_IMAGE_NAME));
                    imageId = cursor2.getInt(cursor2.getColumnIndex(ConceptImagesTable.COL_ID_CONCEPT_IMAGE));
                    cursor2.moveToNext();
                } while(Arrays.asList(seenImagesArray).contains(imageId+""));
                ContentValues values = new ContentValues();
                values.put(UsersTable.COL_CURRENT_IMAGE, imageId);
                db.update(UsersTable.TABLE_NAME, values,UsersTable.COL_EMAIL + " = ?",
                        new String[] {email});
                setPicFromAsset(image);
            } cursor2.close();
            //to do - CASE WHEN CURSOR IS NULL (ALL IMAGES USED)
        } cursor.close();
        db.close();
    }

    private void setPicFromAsset(String assetName) {
        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        AssetManager am = getAssets();
        try {
            InputStream is = am.open("img/" + assetName);
            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, new Rect(-1, -1, -1, -1), bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

            is.reset();

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            Bitmap bitmap = BitmapFactory.decodeStream(is, new Rect(-1, -1, -1, -1), bmOptions);
            imageView.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void openDialog() {
        AlertDialogPuzzle sendDialog = new AlertDialogPuzzle();
        sendDialog.show(getSupportFragmentManager(), "send dialog");
    }

    @Override
    public void confirm(String answer) {
        updatePuzzleState();
        if (isAnswerCorrect(answer)) {
            //answer is correct
            //Update pieces, current image and seen images
            String title = getString(R.string.correct);
            int message = R.string.correctText;
            updateAndSeeImage(title, message, false);
        } else {
            String title = getString(R.string.incorrect);
            int message;
            if (currentPieces.size() == 9) {
                if (numQuest < 32) {
                    message = R.string.incorrectText2;
                } else {
                    message = R.string.endText4;
                }
                updateAndSeeImage(title, message, true);
            } else{
                if (numQuest < 32) {
                    AlertDialog alertDialog = new AlertDialog.Builder(GameActivity.this).create();
                    alertDialog.setTitle(title);
                    alertDialog.setMessage(getString(R.string.incorrectText));
                    alertDialog.setCancelable(false);
                    alertDialog.setButton(getString(R.string._continue), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent newIntent = new Intent(getApplicationContext(), QuestionActivity.class);
                            newIntent.putExtra("com.etseib.ipsapp.number", (numQuest + 1));
                            startActivity(newIntent);
                            finish();
                        }
                    });
                    alertDialog.show();
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(GameActivity.this).create();
                    alertDialog.setTitle(getString(R.string.incorrect));
                    alertDialog.setMessage(getString(R.string.endText));
                    alertDialog.setCancelable(false);
                    alertDialog.setButton(getString(R.string._continue), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            admin.sendTest(session);
                        }
                    });
                    alertDialog.show();
                }
            }
        }
    }

    private boolean isAnswerCorrect(String answer){
        SQLiteDatabase db = admin.getWritableDatabase();
        String query2 = "SELECT * FROM " + UsersTable.TABLE_NAME + " INNER JOIN " +
                ConceptImagesTable.TABLE_NAME + " ON " + ConceptImagesTable.COL_ID_CONCEPT_IMAGE +
                " = " + UsersTable.COL_CURRENT_IMAGE + " WHERE " + UsersTable.COL_EMAIL + " = ?";
        String[] selectionArgs = {email};
        Cursor cursor = db.rawQuery(query2, selectionArgs);
        if (cursor.moveToFirst() && cursor.getCount() > 0) {
            final String image = cursor.getString(cursor.getColumnIndex(ConceptImagesTable.COL_IMAGE_NAME));
            final String _correctAnswer = getString(getResources().getIdentifier(image.replace
                    (".", "_"), "string", getPackageName()));
            String nCorrectAnswer = (Normalizer.normalize(_correctAnswer, Normalizer.Form.NFD));
            nCorrectAnswer = nCorrectAnswer.replaceAll("[^\\p{ASCII}]", "").toLowerCase();
            List<String> correctAnswers = Arrays.asList(nCorrectAnswer.split("\\|")); //accepted versions to compare
            String nAnswer = (Normalizer.normalize(answer, Normalizer.Form.NFD));
            nAnswer = nAnswer.replaceAll("[^\\p{ASCII}]", "").toLowerCase();
            cursor.close();
            db.close();
            return (correctAnswers.contains(nAnswer));
        } cursor.close();
        db.close();
        return false;
    }

    private void updateAndSeeImage(String title, int message, boolean formatMessage) {
        SQLiteDatabase db = admin.getWritableDatabase();
        String query = "SELECT * FROM " + UsersTable.TABLE_NAME + " INNER JOIN " +
                ConceptImagesTable.TABLE_NAME + " ON " + ConceptImagesTable.COL_ID_CONCEPT_IMAGE +
                " = " + UsersTable.COL_CURRENT_IMAGE + " WHERE " + UsersTable.COL_EMAIL + " = ?";
        String[] selectionArgs = {email};
        Cursor cursor = db.rawQuery(query, selectionArgs);
        if (cursor.moveToFirst() && cursor.getCount() > 0) {
            final String image = cursor.getString(cursor.getColumnIndex(ConceptImagesTable.COL_IMAGE_NAME));
            final String _correctAnswer = getString(getResources().getIdentifier(image.replace
                    (".", "_"), "string", getPackageName()));
            final String correctAnswer = _correctAnswer.split("\\|")[0];
            String query2 = "SELECT * FROM " + UsersTable.TABLE_NAME + " WHERE " + UsersTable.COL_EMAIL + " = ?";
            String[] selectionArgs2 = {email};
            Cursor cursor2 = db.rawQuery(query2, selectionArgs2);
            if (cursor2.moveToFirst() && cursor.getCount() > 0) {
                int imageId = cursor.getInt(cursor2.getColumnIndex(UsersTable.COL_CURRENT_IMAGE));
                ContentValues values = new ContentValues();
                values.put(UsersTable.COL_CURRENT_IMAGE, (String) null);
                values.put(UsersTable.COL_SEEN_PIECES, (String) null);
                String seenImages = cursor.getString(cursor2.getColumnIndex(UsersTable.COL_SEEN_IMAGES));
                if (!TextUtils.isEmpty(seenImages)) {
                    values.put(UsersTable.COL_SEEN_IMAGES, seenImages + imageId + ";");
                } else {
                    values.put(UsersTable.COL_SEEN_IMAGES, imageId + ";");
                }
                db.update(UsersTable.TABLE_NAME, values, UsersTable.COL_EMAIL + " = ?",
                        new String[]{session.getEmail()});
            }
            cursor.close();
            db.close();
            AlertDialog alertDialog = new AlertDialog.Builder(GameActivity.this).create();
            alertDialog.setTitle(title);
            String message2;
            if (formatMessage){
                message2 = getString(message, correctAnswer);
            } else{
                message2 = getString(message);
            }
            alertDialog.setMessage(message2);
            alertDialog.setCancelable(false);
            alertDialog.setButton(getString(R.string._continue), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent newIntent = new Intent(getApplicationContext(), ImageActivity.class);
                    newIntent.putExtra("com.etseib.ipsapp.quest", (numQuest));
                    newIntent.putExtra("com.etseib.ipsapp.imageanswer", correctAnswer);
                    newIntent.putExtra("com.etseib.ipsapp.image", image);
                    startActivity(newIntent);
                    finish();
                }
            });
            alertDialog.show();
        }
    }

    private void updatePuzzleState(){
        SQLiteDatabase db = admin.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(UsersTable.COL_IN_GAME, 0);
        db.update(UsersTable.TABLE_NAME, values,UsersTable.COL_EMAIL + " = ?",
                new String[] {session.getEmail()});
        db.close();
    }

    private ArrayList<PuzzlePiece> splitImage() {
        int piecesNumber = 9;
        int rows = 3;
        int cols = 3;

        ArrayList<PuzzlePiece> pieces = new ArrayList<>(piecesNumber);

        // Get the scaled bitmap of the source image
        //imageView.setImageURI(null);
        //imageView.setImageURI("");
        BitmapDrawable drawable = ((BitmapDrawable)imageView.getDrawable());
        Bitmap bitmap = drawable.getBitmap();

        int[] dimensions = getBitmapPositionInsideImageView();
        int scaledBitmapLeft = dimensions[0];
        int scaledBitmapTop = dimensions[1];
        int scaledBitmapWidth = dimensions[2];
        int scaledBitmapHeight = dimensions[3];

        int croppedImageWidth = scaledBitmapWidth - 2 * abs(scaledBitmapLeft);
        int croppedImageHeight = scaledBitmapHeight - 2 * abs(scaledBitmapTop);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledBitmapWidth, scaledBitmapHeight, true);
        Bitmap croppedBitmap = Bitmap.createBitmap(scaledBitmap, abs(scaledBitmapLeft), abs(scaledBitmapTop), croppedImageWidth, croppedImageHeight);

        // Calculate the with and height of the pieces
        int pieceWidth = croppedImageWidth/cols;
        int pieceHeight = croppedImageHeight/rows;

        // Create each bitmap piece and add it to the resulting array
        int yCoord = 0;
        for (int row = 0; row < rows; row++) {
            int xCoord = 0;
            for (int col = 0; col < cols; col++) {
                // calculate offset for each piece
                int offsetX = 0;
                int offsetY = 0;
                if (col > 0) {
                    offsetX = pieceWidth / 3;
                }
                if (row > 0) {
                    offsetY = pieceHeight / 3;
                }

                // apply the offset to each piece
                Bitmap pieceBitmap = Bitmap.createBitmap(croppedBitmap, xCoord - offsetX, yCoord - offsetY, pieceWidth + offsetX, pieceHeight + offsetY);
                PuzzlePiece piece = new PuzzlePiece(getApplicationContext());
                piece.setImageBitmap(pieceBitmap);
                piece.xCoord = xCoord - offsetX + imageView.getLeft();
                piece.yCoord = yCoord - offsetY + imageView.getTop();
                piece.pieceWidth = pieceWidth + offsetX;
                piece.pieceHeight = pieceHeight + offsetY;

                // this bitmap will hold our final puzzle piece image
                Bitmap puzzlePiece = Bitmap.createBitmap(pieceWidth + offsetX, pieceHeight + offsetY, Bitmap.Config.ARGB_8888);

                // draw path
                int bumpSize = pieceHeight / 4;
                Canvas canvas = new Canvas(puzzlePiece);
                Path path = new Path();
                path.moveTo(offsetX, offsetY);
                if (row == 0) {
                    // top side piece
                    path.lineTo(pieceBitmap.getWidth(), offsetY);
                } else {
                    // top bump
                    path.lineTo(offsetX + (pieceBitmap.getWidth() - offsetX) / 3, offsetY);
                    path.cubicTo(offsetX + (pieceBitmap.getWidth() - offsetX) / 6, offsetY - bumpSize, offsetX + (pieceBitmap.getWidth() - offsetX) / 6 * 5, offsetY - bumpSize, offsetX + (pieceBitmap.getWidth() - offsetX) / 3 * 2, offsetY);
                    path.lineTo(pieceBitmap.getWidth(), offsetY);
                }

                if (col == cols - 1) {
                    // right side piece
                    path.lineTo(pieceBitmap.getWidth(), pieceBitmap.getHeight());
                } else {
                    // right bump
                    path.lineTo(pieceBitmap.getWidth(), offsetY + (pieceBitmap.getHeight() - offsetY) / 3);
                    path.cubicTo(pieceBitmap.getWidth() - bumpSize,offsetY + (pieceBitmap.getHeight() - offsetY) / 6, pieceBitmap.getWidth() - bumpSize, offsetY + (pieceBitmap.getHeight() - offsetY) / 6 * 5, pieceBitmap.getWidth(), offsetY + (pieceBitmap.getHeight() - offsetY) / 3 * 2);
                    path.lineTo(pieceBitmap.getWidth(), pieceBitmap.getHeight());
                }

                if (row == rows - 1) {
                    // bottom side piece
                    path.lineTo(offsetX, pieceBitmap.getHeight());
                } else {
                    // bottom bump
                    path.lineTo(offsetX + (pieceBitmap.getWidth() - offsetX) / 3 * 2, pieceBitmap.getHeight());
                    path.cubicTo(offsetX + (pieceBitmap.getWidth() - offsetX) / 6 * 5,pieceBitmap.getHeight() - bumpSize, offsetX + (pieceBitmap.getWidth() - offsetX) / 6, pieceBitmap.getHeight() - bumpSize, offsetX + (pieceBitmap.getWidth() - offsetX) / 3, pieceBitmap.getHeight());
                    path.lineTo(offsetX, pieceBitmap.getHeight());
                }

                if (col == 0) {
                    // left side piece
                    path.close();
                } else {
                    // left bump
                    path.lineTo(offsetX, offsetY + (pieceBitmap.getHeight() - offsetY) / 3 * 2);
                    path.cubicTo(offsetX - bumpSize, offsetY + (pieceBitmap.getHeight() - offsetY) / 6 * 5, offsetX - bumpSize, offsetY + (pieceBitmap.getHeight() - offsetY) / 6, offsetX, offsetY + (pieceBitmap.getHeight() - offsetY) / 3);
                    path.close();
                }

                // mask the piece
                Paint paint = new Paint();
                paint.setColor(0XFF000000);
                paint.setStyle(Paint.Style.FILL);

                canvas.drawPath(path, paint);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                canvas.drawBitmap(pieceBitmap, 0, 0, paint);

                // draw a white border
                Paint border = new Paint();
                border.setColor(0X80FFFFFF);
                border.setStyle(Paint.Style.STROKE);
                border.setStrokeWidth(8.0f);
                canvas.drawPath(path, border);

                // draw a black border
                border = new Paint();
                border.setColor(0X80000000);
                border.setStyle(Paint.Style.STROKE);
                border.setStrokeWidth(3.0f);
                canvas.drawPath(path, border);

                // set the resulting bitmap to the piece
                piece.setImageBitmap(puzzlePiece);

                pieces.add(piece);
                xCoord += pieceWidth;
            }
            yCoord += pieceHeight;
        }

        return pieces;
    }

    private int[] getBitmapPositionInsideImageView() {
        int[] ret = new int[4];

        if (imageView == null || imageView.getDrawable() == null)
            return ret;

        // Get image dimensions
        // Get image matrix values and place them in an array
        float[] f = new float[9];
        imageView.getImageMatrix().getValues(f);

        // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];

        // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
        final Drawable d = imageView.getDrawable();
        final int origW = d.getIntrinsicWidth();
        final int origH = d.getIntrinsicHeight();

        // Calculate the actual dimensions
        final int actW = Math.round(origW * scaleX);
        final int actH = Math.round(origH * scaleY);

        ret[2] = actW;
        ret[3] = actH;

        // Get image position
        // We assume that the image is centered into ImageView
        int imgViewW = imageView.getWidth();
        int imgViewH = imageView.getHeight();

        int top = (int) (imgViewH - actH)/2;
        int left = (int) (imgViewW - actW)/2;

        ret[0] = left;
        ret[1] = top;

        return ret;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }
}