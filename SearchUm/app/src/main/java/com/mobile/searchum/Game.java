package com.mobile.searchum;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Game extends AppCompatActivity {

    private EditText scoreEditText;
    private Button submitScoreButton;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase database;
    private DatabaseReference mDatabase;

    private final View.OnClickListener submitScoreListener = new View.OnClickListener() {
        @Override
        public void onClick(View view){
            String scoreString = scoreEditText.getText().toString().trim();
            int score = Integer.parseInt(scoreString);
            FirebaseUser user = mFirebaseAuth.getCurrentUser();
            String username = user.getDisplayName();

            // Map to store username and score
            HashMap<String, Integer> scoreMap = new HashMap<>();
            scoreMap.put(username, score);

            // Stores map to highscores database.
            if(score != 0){
                mDatabase.push().setValue(scoreMap);
                Toast.makeText(Game.this, "Score submitted.", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(Game.this, "Invalid Score.", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        mFirebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference("HighScores");

        scoreEditText = findViewById(R.id.scoreEditText);
        submitScoreButton = findViewById(R.id.submitScoreButton);

        submitScoreButton.setOnClickListener(submitScoreListener);

    }
}