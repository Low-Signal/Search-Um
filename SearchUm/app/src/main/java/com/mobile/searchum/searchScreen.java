package com.mobile.searchum;
import android.app.Activity;
import android.os.Bundle;

import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.util.Size;
import android.widget.Toast;

import com.google.android.gms.common.util.Strings;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.ListenableFuture;


import org.tensorflow.lite.Interpreter;
import android.app.Activity;
import android.os.Bundle;

import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.lifecycle.ProcessCameraProvider;

import com.google.common.util.concurrent.ListenableFuture;

public class searchScreen extends AppCompatActivity {

    private TextView mTextView;
    private static final String M_PATH = "detect.tflite";
    private static final String L_PATH = "labelmap.txt";
    private Activity mCurrentActivity = null;
    private String[] Objects = null;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    int Frame = 0;
    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_screen2);

        mCurrentActivity = this;


        try {
            labels(mCurrentActivity);
        } catch (IOException e) {
            e.printStackTrace();
        }


        startCamera();
    }

    // reads in from label file
    private void labels(Activity activity) throws IOException {
        if(Objects != null)
        {
            return;
        }

        // hard setting to size of label file.
        Objects = new String[90];

        // reads labels line by line
        //FileDescriptor fileD = activity.getAssets().openFd(L_PATH);
        InputStream file = activity.getAssets().open(L_PATH);
        //FileInputStream inStream = new FileInputStream(fileD.getFileDescriptor());
        BufferedReader r = new BufferedReader(new InputStreamReader(file));
        String temp = r.readLine();

        //arrray position
        int i = 0;
        while(temp != null && i < 90)
        {
            Log.d("label",temp);
            Objects[i] =temp;
            i++;
            temp = r.readLine();
        }
    }




















    private ByteBuffer loadModelFile(Activity activity) throws IOException
    {
        AssetFileDescriptor fileD = activity.getAssets().openFd(M_PATH);

        FileInputStream inStream = new FileInputStream(fileD.getFileDescriptor());

        FileChannel fileChannel = inStream.getChannel();
        long startOffset = fileD.getStartOffset();
        long declaredLength = fileD.getDeclaredLength();

        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,declaredLength);

    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void startCamera() {
        //https://developer.android.com/training/camerax/preview

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        // need to change language compat to 1.8 java to use the arrow function
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));


    }


    @RequiresApi(api = Build.VERSION_CODES.P)
    private void bindPreview(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();
        Toast.makeText(getApplicationContext(), "abind", Toast.LENGTH_LONG);

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        PreviewView previewView = findViewById(R.id.camera);
        preview.setSurfaceProvider(previewView.createSurfaceProvider());
        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();



        imageAnalysis.setAnalyzer(getMainExecutor(),new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(ImageProxy image) {
                int rotationDegrees = image.getImageInfo().getRotationDegrees();
                //image format is 35 or YUV_420_888
                Toast.makeText(getApplicationContext(), "analyze", Toast.LENGTH_LONG);
                Log.d("ANAL","in anal");

                try {
                    // input 300x300x3
                    Interpreter tflite = new Interpreter(loadModelFile(mCurrentActivity));
                    Log.d("LOAD", "loaded");
                    Toast.makeText(getApplicationContext(), "hello", Toast.LENGTH_LONG);
                    // image to butmatp insprried by https://heartbeat.fritz.ai/image-classification-on-android-with-tensorflow-lite-and-camerax-4f72e8fdca79
                    // https://stackoverflow.com/questions/56772967/converting-imageproxy-to-bitmap
                    // helped
                    ImageProxy.PlaneProxy y = image.getPlanes()[0];
                    ImageProxy.PlaneProxy u = image.getPlanes()[1];
                    ImageProxy.PlaneProxy v = image.getPlanes()[2];
                    int ysize = y.getBuffer().remaining();
                    int usize = u.getBuffer().remaining();
                    int vsize = v.getBuffer().remaining();
                    byte[] temp = new byte[ysize + usize + vsize];
                    y.getBuffer().get(temp, 0, ysize);
                    u.getBuffer().get(temp, ysize, vsize);
                    v.getBuffer().get(temp, ysize + vsize, usize);

                    YuvImage yuvImage = new YuvImage(temp, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 75, out);
                    byte[] iBytes = out.toByteArray();
                    Bitmap input = Bitmap.createScaledBitmap(BitmapFactory.decodeByteArray(iBytes, 0, iBytes.length), 300, 300, true);
                    //1280X960 image dimensions
                    int wSize = input.getWidth();
                    int hSize = input.getHeight();
                    int[] intValues = new int[wSize * hSize];
                    input.getPixels(intValues, 0, input.getWidth(), 0, 0, input.getWidth(), input.getHeight());
                    Log.d("data", String.valueOf(input.getPixel(20, 30)));


                    // for quantised model
                    ByteBuffer imgData;
                    imgData = ByteBuffer.allocateDirect(wSize * hSize * 3);
                    //imgData.rewind();
                    for (int i = 0; i < wSize; ++i) {
                        for (int j = 0; j < hSize; ++j) {
                            int pixelValue = intValues[i * wSize + j];
                            imgData.put((byte) ((pixelValue >> 16) & 0xFF));
                            imgData.put((byte) ((pixelValue >> 8) & 0xFF));
                            imgData.put((byte) (pixelValue & 0xFF));
                        }
                    }

                    Map<Integer, Object> output = new HashMap<>();

                    // how many detections
                    float[] numDetect = new float[1];
                    float[][] outClasses = new float[1][10];
                    float[][] outScores = new float[1][10];
                    float[][][] loc = new float[1][10][4];

                    output.put(3, numDetect);
                    //classes
                    output.put(1, outClasses);
                    // scores
                    output.put(2, outScores);
                    //locations
                    output.put(0, loc);

                        Object[] tfliteInput = {imgData};
                        tflite.runForMultipleInputsOutputs(tfliteInput, output);
                        // Log.d("ic", output.get(3)));

                        int numDetectionsOutput = Math.min(10, (int) numDetect[0]);
                        Log.d("found", String.valueOf(numDetectionsOutput));
                        Log.d("ic", String.valueOf(outClasses[0][0]));
                        Vector<String> qLessClasses = new Vector();
                        Vector percents = new Vector();
                        for (int i = 0; i < outClasses[0].length; i++) {
                            String cla = Objects[(int) outClasses[0][i]];
                            if (!cla.equals("???")) {
                                qLessClasses.add(cla);
                                percents.add(outScores[0][i]);
                            }
                        }

                        int[] top3 = findTop3(percents);
                        if (top3.length == 3) {
                            TextView t1, t1p, t2, t2p, t3, t3p;
                            t1 = (TextView) findViewById(R.id.top1);
                            t1p = (TextView) findViewById(R.id.top1P);
                            t2 = (TextView) findViewById(R.id.top2);
                            t2p = (TextView) findViewById(R.id.top2P);
                            t3 = (TextView) findViewById(R.id.top3);
                            t3p = (TextView) findViewById(R.id.top3P);
                            t1.setText(qLessClasses.get(top3[0]));
                            t1p.setText(String.valueOf(percents.get(top3[0])));

                            t2.setText(qLessClasses.get(top3[1]));
                            t2p.setText(String.valueOf(percents.get(top3[1])));

                            t3.setText(qLessClasses.get(top3[2]));
                            t3p.setText(String.valueOf(percents.get(top3[2])));
                        }
                   
                    /*
                    for (int i = 0; i < 3; i ++)
                    {
                        Log.d("top3",qLessClasses.get(i));
                        Log.d("top3","percent: "+percents.get(i));
                    }*/
                    /*
                    for(int i = 0; i < outClasses[0].length;i++)
                    {
                        Log.d("classes",String.valueOf(i)+" :"+Objects[(int) outClasses[0][i]]);
                    }
                    Log.d("classes","end");
                    Log.d("classes","end");
                    Log.d("classes","end");
                    */

                    tflite.close();
                } catch (IOException e) {
                    Log.d("FAIL","faileded");
                    Toast.makeText(getApplicationContext(), "no", Toast.LENGTH_LONG);
                    e.printStackTrace();
                }





                image.close();
            }

        });
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector,imageAnalysis,preview);
    }

    private int[] findTop3(Vector array)
    {
        float first,second,third;
        int fpos,spos,tpos;

        first = 0;
        second = 0;
        third = 0;
        fpos = 0;
        spos = 0;
        tpos = 0;
        if(array.isEmpty())
        {
            int [] x = {};
            return x;
        }
        for(int i = 0; i < array.size(); i++)
        {
            if(i > first)
            {
                float temp =first;

                first = (float) array.get(i);

                float temp2 = second;
                second = temp;
                third = temp2;
                int t,t2;
                t =fpos;
                t2 = spos;
                fpos = i;

                spos = t;
                tpos = t2;
            }
            else if(i > second)
            {
                float temp = second;
                second = (float) array.get(i);
                third = temp;
                int t;
                t = spos;
                spos = i;
                tpos = t;
            }
            else if(i > third)
            {
                third = (float) array.get(i);
                tpos = i;
            }

        }
        //float[] x = {fpos,first,spos,second,tpos,third};
        int[]x = {fpos,spos,tpos};
        return x;
    }


}

