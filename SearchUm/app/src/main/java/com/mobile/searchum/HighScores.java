package com.mobile.searchum;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class HighScores extends AppCompatActivity {
    private String TAG = HighScores.class.getSimpleName();

    // Used to only display one header.
    private Boolean oneHeader = false;

    private FirebaseDatabase database;
    private DatabaseReference mDatabase;

    private ArrayList<String> scores = new ArrayList<>();


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
                scores.clear();
                for(DataSnapshot scoreFromDb : dataSnapshot.getChildren()) {
                    scores.add(scoreFromDb.getValue().toString().replace("{", "").replace("}", ""));
                    String[] scoreString = scores.toArray(new String[scores.size()]);

                    ListView scoreListView = (ListView)findViewById(R.id.scoreListView);
                    ViewGroup headerView = (ViewGroup)getLayoutInflater().inflate(R.layout.scoreboard_header, scoreListView, false);

                    if(!oneHeader){
                        scoreListView.addHeaderView(headerView);
                        oneHeader = true;
                    }

                    ListAdapter adapter = new ListAdapter(getApplicationContext(), R.layout.row_layout, R.id.usernameTextView, scoreString);
                    scoreListView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}