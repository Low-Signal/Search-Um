package com.mobile.searchum;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private EditText emailEditText, passEditText;
    private Button loginButton, signUpButton;
    private FirebaseAuth mFirebaseAuth;

    private FirebaseAuth.AuthStateListener mAuthStateListener;

    // Goes to the home screen.
    private final View.OnClickListener loginListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String email = emailEditText.getText().toString();
            String password = passEditText.getText().toString();

            if(email.isEmpty()){
                emailEditText.setError("Please enter an email address.");
            }
            if(password.isEmpty()){
                passEditText.setError("Must enter a password.");
            }
            if(!(email.isEmpty() && password.isEmpty())){
                mFirebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            emailEditText.requestFocus();
                            emailEditText.setError("Invalid");
                            passEditText.setError("Invalid");
                            Toast.makeText(MainActivity.this, "Invalid username or password.", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            startActivity(new Intent(MainActivity.this, HomeScreen.class));
                        }
                    }
                });
            }
        }
    };

    // Goes to the sign up activity.
    private final View.OnClickListener signUpListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent (MainActivity.this, SignUp.class);
            startActivity(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAuth = FirebaseAuth.getInstance();
        emailEditText = findViewById(R.id.emailEditText);
        passEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signUpButton = findViewById(R.id.signUpButton);

        loginButton.setOnClickListener(loginListener);
        signUpButton.setOnClickListener(signUpListener);

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(mFirebaseUser != null){
                    Toast.makeText(MainActivity.this, "You are logged in.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, HomeScreen.class));
                }
                else{
                    //TODO might not need this.
                    //Toast.makeText(MainActivity.this, "Please login.", Toast.LENGTH_SHORT).show();
                }
            }
        };

    }

    @Override
    protected void onStart(){
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause(){
        super.onPause();
        emailEditText.setError(null);
        passEditText.setError(null);
        emailEditText.clearFocus();
        passEditText.clearFocus();
    }
}