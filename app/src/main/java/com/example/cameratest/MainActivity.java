package com.example.cameratest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity implements SensorEventListener {

    private ImageView mImageView;
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    float x, y, z, lx, ly, lz, angle;
    String string;
    String imageFileName;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Action onCreate Activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = (ImageView) findViewById(R.id.imageView1);
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onClick(View view) {
        //Action onClick button1
        if (view.getId() == R.id.button1) {
            //Takes Picture
            dispatchTakePictureIntent();
            //Fix accelerometer data
            x = lx;
            y = ly;
            z = lz;
            string = "x=" + Float.toString(x) + "y=" + Float.toString(y) + "z=" + Float.toString(z);
            //for ASUS Zenfone
            //angle =(float) ((Math.atan(y/x) * 180) / Math.PI);
            //for Samsung Tablet
            angle = (float) -((Math.atan(x / y) * 180) / Math.PI);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Action after successfully take picture
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = rotate((Bitmap) extras.get("data"), (float) angle);
            createImageFileName();
            saveImage(imageBitmap, imageFileName);
            mImageView.setImageBitmap(imageBitmap);
            TextView tv = (TextView) findViewById(R.id.textView);
            tv.setText(string);
        }
    }

    public static Bitmap rotate(Bitmap source, float angle) {
        //Method that rotates bitmap
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        source = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, false);
        source = Bitmap.createBitmap(source, source.getWidth() / 4, source.getHeight() / 4, source.getWidth() / 3, source.getHeight() / 3);
        return source;
    }

    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void dispatchTakePictureIntent() {
        //Intent to take picture
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        //Get g values from sensor
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            lx = sensorEvent.values[0];
            ly = sensorEvent.values[1];
            lz = sensorEvent.values[2];
        }
        //Output g values
        TextView tv2 = (TextView) findViewById(R.id.textView2);
        String string2;
        string2 = "x=" + Float.toString(lx) + "y=" + Float.toString(ly) + "z=" + Float.toString(lz);
        tv2.setText(string2);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void saveImage(Bitmap finalBitmap, String image_name) {
        //Save image file to storage
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root);
        myDir.mkdirs();
        String fname = "Image-" + image_name + ".jpg";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        Log.i("LOAD", root + fname);
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void createImageFileName() {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imageFileName = "JPEG_" + timeStamp + "_";
    }
}
