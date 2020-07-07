package com.mobile.searchum;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignUp extends AppCompatActivity {

    private String TAG = SignUp.class.getSimpleName();

    private EditText displayNameEditText, emailEditText, passEditText, confirmPassEditText;
    private Button createAccountButton;
    private TextView toSignInTextView;
    private FirebaseAuth mFirebaseAuth;

    // Creates account and goes to the home screen.
    private final View.OnClickListener createAccountListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String displayName = displayNameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passEditText.getText().toString().trim();
            String confirmPass = confirmPassEditText.getText().toString().trim();

            Boolean error = false;

            if(displayName.isEmpty()){
                displayNameEditText.setError("Please enter an display name");
                displayNameEditText.requestFocus();
                error = true;
            }
            if(email.isEmpty()){
                emailEditText.setError("Please enter an email address.");
                emailEditText.requestFocus();
                error = true;
            }
            if(password.isEmpty()){
                passEditText.setError("Must enter a password.");
                passEditText.requestFocus();
                error = true;
            }
            if(confirmPass.isEmpty()){
                confirmPassEditText.setError("Must enter a password.");
                confirmPassEditText.requestFocus();
                error = true;
            }
            if(!password.equals(confirmPass)){
                passEditText.setError("Passwords must be the same.");
                passEditText.requestFocus();
                confirmPassEditText.requestFocus();
                error = true;
            }

            // If there were no errors attempt to create account.
            if(!error){
                mFirebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(SignUp.this, "Sign up failed, please try again.", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            userProfile();
                            startActivity(new Intent(SignUp.this, HomeScreen.class));
                        }
                    }
                });
            }
        }
    };

    // Goes to the sign in page.
    private final View.OnClickListener toSignInListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startActivity(new Intent(SignUp.this, MainActivity.class));
        }
    };

    private void userProfile(){
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        if(user != null) {
            UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                    .setDisplayName(displayNameEditText.getText().toString().trim()).build();

            user.updateProfile(profileUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        Log.d("TAG", "Display name update successful");
                    }
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mFirebaseAuth = FirebaseAuth.getInstance();

        displayNameEditText = findViewById(R.id.displayNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passEditText = findViewById(R.id.passwordEditText);
        confirmPassEditText = findViewById(R.id.confirmPassEditText);
        createAccountButton= findViewById(R.id.createAccountButton);
        toSignInTextView = findViewById(R.id.toSignInTextView);

        createAccountButton.setOnClickListener(createAccountListener);
        toSignInTextView.setOnClickListener(toSignInListener);

    }

    @Override
    protected void onPause(){
        super.onPause();
        displayNameEditText.setError(null);
        emailEditText.setError(null);
        passEditText.setError(null);
        confirmPassEditText.setError(null);

        displayNameEditText.clearFocus();
        emailEditText.clearFocus();
        passEditText.clearFocus();
        confirmPassEditText.clearFocus();

    }
}