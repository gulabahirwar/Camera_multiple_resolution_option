package com.example.gsadev.camera;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ImageSurfaceView mImageSurfaceView;
    private Camera camera;

    private FrameLayout cameraPreviewLayout;
    private ImageView capturedImageHolder;
    private Spinner spinner;
    private Camera.Size size;

    private static final int MY_CAMERA_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkForPermission();

        cameraPreviewLayout = (FrameLayout)findViewById(R.id.camera_preview);
        capturedImageHolder = (ImageView)findViewById(R.id.captured_image);
        spinner=(Spinner) findViewById(R.id.spinner);

        initCameraSurfaceView();

        Button captureButton = (Button)findViewById(R.id.button);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.takePicture(null, null, pictureCallback);
            }
        });

        if (isPermissionGranted())
            setResolution(spinner);
    }

    private void initCameraSurfaceView() {
        camera = checkDeviceCamera();
        mImageSurfaceView = new ImageSurfaceView(MainActivity.this, camera);
        cameraPreviewLayout.addView(mImageSurfaceView);

    }

    private boolean isPermissionGranted() {
        int result = checkCallingOrSelfPermission(Manifest.permission.CAMERA);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.release();
            camera=null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
            if (camera==null){
                initCameraSurfaceView();

                if (isPermissionGranted() && camera.getParameters()!=null){
                    Camera.Parameters parameters=camera.getParameters();
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                    parameters.setPictureSize(size.width,size.height);
                    camera.setParameters(parameters);
                    camera.startPreview();
                }
            }
    }


    private Camera checkDeviceCamera(){
        Camera mCamera = null;
        try {
            mCamera = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mCamera;
    }
    private void setResolution(Spinner spinner){

        Camera.Parameters params=null;
        if (camera.getParameters()!=null) {
            params = camera.getParameters();
        }

        final List<Camera.Size> sizes = params.getSupportedPictureSizes();
        spinner.setAdapter(new SpinnerCustomAdapter(this,sizes));

        final Camera.Parameters finalParams = params;
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                size=sizes.get(i);
                finalParams.setPictureSize(sizes.get(i).width,sizes.get(i).height);
                camera.setParameters(finalParams);
                camera.startPreview();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
    Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            if(bitmap==null){
                Toast.makeText(MainActivity.this, "Captured image is empty", Toast.LENGTH_LONG).show();
                return;
            }
            capturedImageHolder.setImageBitmap(scaleDownBitmapImage(bitmap, 300, 300 ));
            saveImageInStorage(bitmap);
        }
    };

    private Bitmap scaleDownBitmapImage(Bitmap bitmap, int newWidth, int newHeight){
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        return resizedBitmap;
    }

    private void saveImageInStorage(Bitmap bitmapImage){
        FileOutputStream outStream = null;
        File f=new File(Environment.getExternalStorageDirectory()+"/NewCameraImage/");
        f.mkdir();
        String extStorageDirectory = f.toString();
        File file = new File(extStorageDirectory, System.nanoTime()+"image.jpg");
        try {
            outStream = new FileOutputStream(file);
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            Toast.makeText(getApplicationContext(), "Saved at "+f.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {e.printStackTrace();}
        try {
            outStream.flush();
            outStream.close();
        } catch (IOException e) {e.printStackTrace();}
    }

    private void checkForPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED ) {
                requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_CAMERA_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE ) {
            boolean cameraPermission=grantResults[0] == PackageManager.PERMISSION_GRANTED;
            boolean writeStoragePermission=grantResults[0] == PackageManager.PERMISSION_GRANTED;

            if(!cameraPermission){
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
                showDialogBox();
            }

            if(!writeStoragePermission){
                Toast.makeText(this, "storage permission denied", Toast.LENGTH_LONG).show();
                showDialogBox();
            }

            if (cameraPermission && writeStoragePermission) {

                initCameraSurfaceView();
                setResolution(spinner);

            }

        }

}

    private void showDialogBox() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Camera permission required!");
        builder.setCancelable(false);

        builder.setPositiveButton(
                "Grant",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                       checkForPermission();
                    }
                });

        builder.setNegativeButton(
                "Not now",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        MainActivity.this.finish();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }
}
