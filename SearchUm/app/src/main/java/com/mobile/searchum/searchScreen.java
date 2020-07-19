package com.mobile.searchum;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.Surface;
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
import java.util.Random;
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
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;



import org.tensorflow.lite.Interpreter;
import android.app.Activity;
import android.os.Bundle;

import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.lifecycle.ProcessCameraProvider;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.label.FirebaseVisionLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetector;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetectorOptions;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

public class searchScreen extends AppCompatActivity {

    private TextView mTextView;
    private static final String M_PATH = "detect.tflite";
    private static final String L_PATH = "labelmap.txt";
    private static final String F_PATH = "realStuff.txt";
    private Activity mCurrentActivity = null;
    private String[] Objects = null;
    FirebaseVisionLabelDetectorOptions options =
            new FirebaseVisionLabelDetectorOptions.Builder()
                    .setConfidenceThreshold(0.8f)
                    .build();
    private Vector<String> findAble = null;
    private String current = null;
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
            setFindAble(mCurrentActivity);

        } catch (IOException e) {
            e.printStackTrace();
        }


        startCamera();
        startGame();
    }

    private void startGame(){

        Log.d("game","started");
        chooseObject();
    }



    private void chooseObject()
    {
        if(findAble.size() != 0) {
           // Log.d("choice", String.valueOf(findAble.size()));
            Random r = new Random();
            int choice = r.nextInt(findAble.size());
            current = findAble.get(choice);
            Log.d("choice",current);
            findAble.remove(choice);
            TextView c = (TextView) findViewById(R.id.current);
            c.setText(current);
        }
    }

    // reads optiosn from file
    private void setFindAble(Activity activity) throws IOException{
        if(findAble != null)
        {
            return;
        }
        findAble = new Vector<String>();
        InputStream file = activity.getAssets().open(F_PATH);
        //FileInputStream inStream = new FileInputStream(fileD.getFileDescriptor());
        BufferedReader r = new BufferedReader(new InputStreamReader(file));
        String temp = r.readLine();

        //arrray position
        int i = 0;
        while(temp != null && i < 90)
        {
            Log.d("findable",temp);
            findAble.add(temp);

            i++;
            temp = r.readLine();
        }


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
                        .setTargetResolution(new Size(1280, 960))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
        /*
        OrientationEventListener orientationEventListener = new OrientationEventListener((Context)this) {
            @Override
            public void onOrientationChanged(int orientation) {
                int rotation;
                // Monitors orientation values to determine the target rotation value
                if (orientation >= 45 && orientation < 135) {
                    rotation = Surface.ROTATION_270;
                } else if (orientation >= 135 && orientation < 225) {
                    rotation = Surface.ROTATION_180;
                } else if (orientation >= 225 && orientation < 315) {
                    rotation = Surface.ROTATION_90;
                } else {
                    rotation = Surface.ROTATION_0;
                }

                imageAnalysis.setTargetRotation(rotation);
            }
        };

        orientationEventListener.enable();
*/
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





                    @SuppressLint("UnsafeExperimentalUsageError")
                    Image inputImage =image.getImage();
                    if (inputImage != null) {
                        FirebaseVisionImage inIm = FirebaseVisionImage.fromMediaImage(inputImage, FirebaseVisionImageMetadata.ROTATION_0);//image.getImageInfo().getRotationDegrees());
                        FirebaseVisionLabelDetector detector = FirebaseVision.getInstance()
                                .getVisionLabelDetector();

                        Task<List<FirebaseVisionLabel>> result =
                                detector.detectInImage(inIm)
                                        .addOnSuccessListener(
                                                new OnSuccessListener<List<FirebaseVisionLabel>>() {
                                                    @Override
                                                    public void onSuccess(List<FirebaseVisionLabel> labels) {
                                                        // Task completed successfull
                                                        if(!labels.isEmpty()) {
                                                            for (FirebaseVisionLabel label : labels) {
                                                                String text = label.getLabel();
                                                                Log.d("Pass", text);
                                                                Log.d("Pass", "current: "+current);
                                                                String entityId = label.getEntityId();
                                                                float confidence = label.getConfidence();
                                                                Log.d("Pass", String.valueOf(confidence));
                                                                if(confidence > 0.5 && text.equals(current)) {
                                                                    chooseObject();
                                                                    break;
                                                                   // TextView c = (TextView) findViewById(R.id.current);
                                                                    //c.setText(text);
                                                                }
                                                            }
                                                        }
                                                    }
                                                })
                                        .addOnFailureListener(
                                                new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        // Task failed with an exception
                                                        Log.d("failure",e.getLocalizedMessage());

                                                        // ...
                                                    }
                                                });
                    }


                    /*
                     @SuppressLint("UnsafeExperimentalUsageError")

                    Image inputImage =image.getImage();
                    if (inputImage != null) {
                        InputImage inIm =
                                InputImage.fromMediaImage(inputImage, image.getImageInfo().getRotationDegrees());
                        ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
                        labeler.process(inIm)
                                .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                                    @Override
                                    public void onSuccess(List<ImageLabel> labels) {
                                        Log.d("Pass", String.valueOf(labels.get(0)));

                                        // Task completed successfully
                                        // ...
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("failure",e.getLocalizedMessage());
                                        // Task failed with an exception
                                        // ...
                                    }
                                });


                    }


                    /*

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
                    int [] rgbBytes = new int[iBytes.length];
                    //ImageUtils.convertYUV420ToARGB8888(y.getBuffer().array(),u.getBuffer().array(),v.getBuffer().array(),
                    //        1280,960,y.getRowStride(),u.getRowStride(),u.getPixelStride(),rgbBytes);
  //                  Bitmap rgbFrameBitmap = Bitmap.createBitmap(1280, 960, Bitmap.Config.ARGB_8888);
//                    rgbFrameBitmap.setPixels(rgbBytes, 0, 1280, 0, 0, 1280, 960);

                    Bitmap input = Bitmap.createScaledBitmap(BitmapFactory.decodeByteArray(iBytes, 0, iBytes.length), 300, 300, false);
                    //Bitmap input = BitmapFactory.decodeByteArray(iBytes, 0, iBytes.length);
                    //1280X960 image dimensions
                    //Bitmap input = Bitmap.createScaledBitmap(rgbFrameBitmap,300,300,false);
                    int wSize = input.getWidth();
                    int hSize = input.getHeight();
                    int[] intValues = new int[wSize * hSize];
                    input.getPixels(intValues, 0, input.getWidth(), 0, 0, 300, 300);
                    Log.d("data", String.valueOf(input.getConfig()));


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
                        //Log.d("found", String.valueOf(numDetect[0]));
                        //Log.d("ic", String.valueOf(outClasses[0][0]));
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
                       // if(top3.length == 3) {
                            for (int i = 0; i < qLessClasses.size(); i++) {
                               Log.d("hceck loop",qLessClasses.get(i));
                               Log.d("hceck loop",current);

                                if (qLessClasses.get(i).equals(current)) {
                                    chooseObject();
                                    //return;
                                    break;
                                }
                            }
                        //}
                        /*
                        if (top3.length == 3) {
                            /*
                            TextView t1, t1p, t2, t2p, t3, t3p;
                            t1 = (TextView) findViewById(R.id.top1);
                            t1p = (TextView) findViewById(R.id.top1P);
                            t2 = (TextView) findViewById(R.id.top2);
                            t2p = (TextView) findViewById(R.id.top2P);
                            t3 = (TextView) findViewById(R.id.top3);
                            t3p = (TextView) findViewById(R.id.top3P);
                            t1.setText(qLessClasses.get(top3[0]));
                            t1p.setText(String.valueOf((float) percents.get(top3[0])*10));

                            t2.setText(qLessClasses.get(top3[1]));
                            t2p.setText(String.valueOf((float)percents.get(top3[1])*10));

                            t3.setText(qLessClasses.get(top3[2]));
                            t3p.setText(String.valueOf((float)percents.get(top3[2])*10));


                            for(int i = 0; i < qLessClasses.size();i++)
                            {
                               // Log.d("classes",String.valueOf(i)+" :"+qLessClasses.get(i));
                                //Log.d("classes",String.valueOf(i)+"PErcenets :"+String.valueOf(percents.get(i)));
                            }
                            Log.d("classes","end");
                            Log.d("classes","end");
                            Log.d("classes","end");
                        }
                    /*
                    for (int i = 0; i < 3; i ++)
                    {
                        Log.d("top3",qLessClasses.get(i));
                        Log.d("top3","percent: "+percents.get(i));
                    }*/




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

