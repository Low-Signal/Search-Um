package com.mobile.searchum;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeScreen extends AppCompatActivity {
    private Button playButton, highScoreButton, changePassButton, logoutButton;
    private TextView testTextView;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    // Goes to the game activity.
    private final View.OnClickListener playListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startActivity(new Intent(HomeScreen.this, Game.class));
        }
    };

    // Goes to the high scores activity.
    private final View.OnClickListener highScoreListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startActivity(new Intent(HomeScreen.this, HighScores.class));
        }
    };

    // Goes to the change password activity.
    private final View.OnClickListener changePassListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startActivity(new Intent(HomeScreen.this, ChangePassword.class));
        }
    };

    // Logs the user out and goes back to sign in page
    private final View.OnClickListener logoutListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mFirebaseAuth.getInstance().signOut();
            startActivity(new Intent(HomeScreen.this, MainActivity.class));
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        testTextView = findViewById(R.id.testTextView);
        playButton = findViewById(R.id.playButton);
        highScoreButton = findViewById(R.id.highScoreButton);
        changePassButton = findViewById(R.id.changePassButton);
        logoutButton = findViewById(R.id.logoutButton);

        playButton.setOnClickListener(playListener);
        highScoreButton.setOnClickListener(highScoreListener);
        changePassButton.setOnClickListener(changePassListener);
        logoutButton.setOnClickListener(logoutListener);

        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        String displayName = user.getDisplayName();
        testTextView.setText(displayName);
    }
}