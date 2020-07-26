package com.mobile.searchum;
import android.content.Intent;
import android.os.CountDownTimer;
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
import android.widget.Button;
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
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.lifecycle.ProcessCameraProvider;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase database;
    private DatabaseReference mDatabase;
    private TextView mTextView;
    private static final String M_PATH = "detect.tflite";
    private static final String L_PATH = "labelmap.txt";
    private static final String F_PATH = "realStuff.txt";
    private Activity mCurrentActivity = null;
    private TextView clock;
    private String[] Objects = null;
    double Score;
    double Streak;
    FirebaseVisionLabelDetectorOptions options =
            new FirebaseVisionLabelDetectorOptions.Builder()
                    .setConfidenceThreshold(0.8f)
                    .build();
    private Vector<String> findAble = null;
    private String current = null;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    int Frame = 0;
    double mode;
    int tick;

    Button skip;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_screen2);

        mFirebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference("HighScores");

        mCurrentActivity = this;
        clock = findViewById(R.id.timer);
        mode = 0.6;
        new CountDownTimer(50000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                clock.setText(String.valueOf(60-tick)+" seconds remaining");
                tick++;
            }

            @Override
            public void onFinish() {
                clock.setText("Done");
            }
        }.start();
        ToggleButton babyMode = findViewById((R.id.babyMode));
        babyMode.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mode = 0.1;
                Toast.makeText(getApplicationContext(), "BABY", Toast.LENGTH_LONG);

            }

        });




        try {
            labels(mCurrentActivity);
            setFindAble(mCurrentActivity);

        } catch (IOException e) {
            e.printStackTrace();
        }


        skip = findViewById(R.id.skip);
        skip.setOnClickListener(skipListener);
        startCamera();
        startGame();
    }

    private final View.OnClickListener skipListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Streak = 1.0;
            chooseObject();
        }

    };


    private void startGame(){

        Log.d("game","started");
        Score = 0;
        Streak = 1.0;
        chooseObject();
    }



    private void chooseObject() {
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
        else
        {
            current = "";
            TextView c = (TextView) findViewById(R.id.current);
            c.setText("you found it all");

            FirebaseUser user = mFirebaseAuth.getCurrentUser();
            String username = user.getDisplayName();
            HashMap<String, Integer> scoreMap = new HashMap<>();
            scoreMap.put(username, (int)(Score*100));
            mDatabase.push().setValue(scoreMap);
            //Thread.sleep(10000);
            startActivity(new Intent(searchScreen.this, HomeScreen.class));
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
        //Toast.makeText(getApplicationContext(), "abind", Toast.LENGTH_LONG);

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

        imageAnalysis.setAnalyzer(getMainExecutor(),new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(ImageProxy image) {
                int rotationDegrees = image.getImageInfo().getRotationDegrees();

                //image format is 35 or YUV_420_888
                Toast.makeText(getApplicationContext(), "analyze", Toast.LENGTH_LONG);
                Log.d("ANAL","in anal");

                    // input 300x300x3
                    //Interpreter tflite = new Interpreter(loadModelFile(mCurrentActivity));
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
                                                                if(confidence > mode && text.equals(current)) {
                                                                    Score += 1 *Streak;
                                                                    Streak+=0.5;
                                                                    TextView c = (TextView) findViewById(R.id.Score);
                                                                    c.setText(String.valueOf(Score*100));
                                                                    TextView b = (TextView) findViewById(R.id.current);
                                                                    b.setText("Nice Searching!");

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

