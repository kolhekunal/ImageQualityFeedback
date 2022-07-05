/*
 * Copyright (C) 2019 University of Washington Ubicomp Lab
 * All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of a BSD-style license that can be found in the LICENSE file.
 */

package edu.washington.cs.ubicomplab.rdt_reader.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import edu.washington.cs.ubicomplab.rdt_reader.R;
import edu.washington.cs.ubicomplab.rdt_reader.fragments.SettingsDialogFragment;
import edu.washington.cs.ubicomplab.rdt_reader.interfaces.SettingsDialogListener;
import edu.washington.cs.ubicomplab.rdt_reader.core.Constants;
import edu.washington.cs.ubicomplab.rdt_reader.views.ViewportUsingBitmap;

import static java.text.DateFormat.getDateTimeInstance;
import static edu.washington.cs.ubicomplab.rdt_reader.core.Constants.TAG;
import static edu.washington.cs.ubicomplab.rdt_reader.views.ViewportUsingBitmap.x;

import org.opencv.core.Mat;

/**
 * The {@link android.app.Activity} for showing the results of RDT image post-processing and
 * automatic analysis
 * Note: In this example app, this activity is launched as an {@link Intent} from {@link MainActivity}
 * with the target RDT's name passed in the bundle to support multiple RDT designs simultaneously
 */
public class ImageResultActivity extends AppCompatActivity implements View.OnClickListener, SettingsDialogListener {
    // Image saving variables
    Bitmap mBitmapToSave;
    byte[] capturedByteArray, windowByteArray;
    boolean isImageSaved = false;
    ImageView resultImageView;

    // Capture time variable
    long timeTaken = 0;

    /**
     * {@link android.app.Activity} onCreate()
     * @param savedInstanceState: the bundle object in case this is launched from an intent
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_result);

        // Initialize UI elements
        initViews();
    }

    /**
     * Initializes UI elements based on that data that was passed through the intent
     */
    private void initViews() {
        Intent intent = getIntent();

        // Captured image
        if (intent.hasExtra("captured")) {
            capturedByteArray = intent.getExtras().getByteArray("captured");
            mBitmapToSave = BitmapFactory.decodeByteArray(capturedByteArray, 0, capturedByteArray.length);

             resultImageView = findViewById(R.id.RDTImageView);
            storeImage(getBitmap());
            resultImageView.setImageBitmap(getBitmap());
            //resultImageView.setImageBitmap(BitmapFactory.decodeByteArray(capturedByteArray, 0, capturedByteArray.length));
        }

        // Enhanced image
        if (intent.hasExtra("window")) {
            windowByteArray = intent.getExtras().getByteArray("window");
          //  mBitmapToSave = BitmapFactory.decodeByteArray(windowByteArray, 0, windowByteArray.length);

            ImageView windowImageView = findViewById(R.id.WindowImageView);
          //  windowImageView.setImageBitmap(BitmapFactory.decodeByteArray(windowByteArray, 0, windowByteArray.length));
        }

        // Capture time
        if (intent.hasExtra("timeTaken")) {
            timeTaken = intent.getLongExtra("timeTaken", 0);
            TextView timeTextView = findViewById(R.id.TimeTextView);
            timeTextView.setText(String.format("%.2f seconds", timeTaken/1000.0));
        }

        //Number of lines
        int numberOfLines = 2;
        if (intent.hasExtra("numberOfLines")) {
            numberOfLines = intent.getIntExtra("numberOfLines", 2);
        }

        // Top line
        if (intent.hasExtra("topLine")) {
            boolean topLine = intent.getBooleanExtra("topLine", false);
            TextView topLineTextView = findViewById(R.id.topLineTextView);
            topLineTextView.setText(String.format("%s", topLine ? "True" : "False"));
        }
        if (intent.hasExtra("topLineName")) {
            String topLineName = intent.getStringExtra("topLineName");
            TextView topLineNameTextView = findViewById(R.id.topLineNameTextView);
            topLineNameTextView.setText(topLineName);
        }

        // Middle line
        if (intent.hasExtra("middleLine")) {
            boolean middleLine = intent.getBooleanExtra("middleLine", false);
            TextView middleLineTextView = findViewById(R.id.middleLineTextView);
            middleLineTextView.setText(String.format("%s", middleLine ? "True" : "False"));
        }
        if (intent.hasExtra("middleLineName")) {
            String middleLineName = intent.getStringExtra("middleLineName");
            TextView middleLineNameTextView = findViewById(R.id.middleLineNameTextView);
            middleLineNameTextView.setText(middleLineName);
        }

        // Bottom line
        if (numberOfLines > 2 && intent.hasExtra("bottomLine")) {
            boolean bottomLine = intent.getBooleanExtra("bottomLine", false);
            TextView bottomLineTextView = findViewById(R.id.bottomLineTextView);
            bottomLineTextView.setVisibility(View.VISIBLE);
            bottomLineTextView.setText(String.format("%s", bottomLine ? "True" : "False"));
        }
        if (numberOfLines > 2 &&  intent.hasExtra("bottomLineName")) {
            String bottomLineName = intent.getStringExtra("bottomLineName");
            TextView bottomLineNameTextView = findViewById(R.id.bottomLineNameTextView);
            bottomLineNameTextView.setVisibility(View.VISIBLE);
            bottomLineNameTextView.setText(bottomLineName);
        }

        if (intent.hasExtra("hasTooMuchBlood")) {
            boolean hasTooMuchBlood = intent.getBooleanExtra("hasTooMuchBlood", false);
            TextView warningView = findViewById(R.id.WarningView);
            if (hasTooMuchBlood) {
                warningView.setText(getString(R.string.too_much_blood_warning));
            } else {
                warningView.setText("");
            }
        }

        // Buttons
        Button saveImageButton = findViewById(R.id.saveButton);
        saveImageButton.setOnClickListener(this);
        Button sendImageButton = findViewById(R.id.doneButton);
        sendImageButton.setOnClickListener(this);
    }

    /**
     * {@link android.app.Activity} onBackPressed()
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    /**
     * The listener for all of the Activity's buttons
     * @param view the button that was selected
     */
    @Override
    public void onClick(View view) {
        // Save the photo locally on the user's device
        if (view.getId() == R.id.saveButton) {

           storeImage(getBitmap());

            resultImageView.setImageBitmap(getBitmap());


        } else if (view.getId() == R.id.doneButton) {
            Intent data = new Intent();
            data.putExtra("RDTCaptureByteArray", capturedByteArray);
            setResult(RESULT_OK, data);
            finish();
        }
    }

    /**
     * {@link SettingsDialogFragment} onClickPositiveButton()
     */
    @Override
    public void onClickPositiveButton() {
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        android.content.res.Configuration conf = res.getConfiguration();
        conf.setLocale(new Locale(Constants.LANGUAGE));
        res.updateConfiguration(conf, dm);

        setContentView(R.layout.activity_image_quality);
        initViews();
    }

    @SuppressLint("WrongThread")
    private void storeImage(Bitmap image) {
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            Log.d(TAG,
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
    }

    /** Create a File for saving an image or video */
    private  File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + getPackageName()
                + "/Files");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName="image"+ timeStamp +".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

    private Bitmap getBitmap(){
        Bitmap dstBmp;
        if (mBitmapToSave.getWidth() >= mBitmapToSave.getHeight()){

            dstBmp = Bitmap.createBitmap(
                    mBitmapToSave,
                    mBitmapToSave.getWidth()/2 - mBitmapToSave.getHeight()/2,
                    0,
                    mBitmapToSave.getHeight(),
                    mBitmapToSave.getHeight()
            );

        }else{

            dstBmp = Bitmap.createBitmap(
                    mBitmapToSave,
                    0,
                    mBitmapToSave.getHeight()/2- mBitmapToSave.getWidth()/2,
                    mBitmapToSave.getWidth(),
                    mBitmapToSave.getWidth()-50
            );
        }
        return dstBmp;
    }

    public Bitmap BITMAP_RESIZER(Bitmap bitmap,int newWidth,int newHeight) {
        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);

        float ratioX = newWidth / (float) bitmap.getWidth();
        float ratioY = newHeight / (float) bitmap.getHeight();
        float middleX = newWidth / 2.0f;
        float middleY = newHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, middleX - bitmap.getWidth() / 2, middleY - bitmap.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaledBitmap;
    }

    private static Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > ratioBitmap) {
                finalWidth = (int) ((float)maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float)maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }
}
