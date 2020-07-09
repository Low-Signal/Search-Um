package com.mobile.searchum;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.storage.StorageManager;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

public class HighScores extends AppCompatActivity {
    private String TAG = HighScores.class.getSimpleName();

    // Used to only display one header.
    private Boolean oneHeader = false;

    private FirebaseDatabase database;
    private DatabaseReference mDatabase;

    private ArrayList<ScoreObject> scoreStorage = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_scores);

        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference("HighScores");

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            // Stores all of the elements from the firebase in a array list.
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot scoreFromDb : dataSnapshot.getChildren()) {

                    String[] currentScore = null;
                    currentScore = scoreFromDb.getValue().toString().replace("{", "").replace("}", "").split("=");

                    scoreStorage.add(new ScoreObject(currentScore[0], currentScore[1]));
                }

                ScoreObject scoreArray[] = new ScoreObject[scoreStorage.size()];
                for(int j =0;j<scoreStorage.size();j++){
                    scoreArray[j] = scoreStorage.get(j);
                }

                Arrays.sort(scoreArray);
                ArrayList<String> scoresFinalList = new ArrayList<>();

                for(ScoreObject obj : scoreArray){
                    scoresFinalList.add(obj.getUsername() + "=" + obj.getScore());
                    String[] scoreString = scoresFinalList.toArray(new String[scoresFinalList.size()]);

                    ListView scoreListView = (ListView)findViewById(R.id.scoreListView);
                    ViewGroup headerView = (ViewGroup)getLayoutInflater().inflate(R.layout.scoreboard_header, scoreListView, false);

                    if(!oneHeader){
                        scoreListView.addHeaderView(headerView);
                        oneHeader = true;
                    }

                    ListAdapter adapter = new ListAdapter(getApplicationContext(), R.layout.row_layout, R.id.usernameTextView, scoreString);
                    scoreListView.setAdapter(adapter);

                }
                //Log.d("TAG", "Check this out: " + scoresFinal);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}