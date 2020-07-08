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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class SignUp extends AppCompatActivity {

    private String TAG = SignUp.class.getSimpleName();

    private EditText displayNameEditText, emailEditText, passEditText, confirmPassEditText;
    private Button createAccountButton;
    private TextView toSignInTextView;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase database;
    private DatabaseReference mDatabase;

    // Creates account and goes to the home screen.
    private final View.OnClickListener createAccountListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final String displayName = displayNameEditText.getText().toString().trim();
            final String email = emailEditText.getText().toString().trim();
            final String password = passEditText.getText().toString().trim();
            String confirmPass = confirmPassEditText.getText().toString().trim();

            Boolean error = false;

            if (displayName.isEmpty()) {
                displayNameEditText.setError("Please enter an display name");
                displayNameEditText.requestFocus();
                error = true;
            }
            if (email.isEmpty()) {
                emailEditText.setError("Please enter an email address.");
                emailEditText.requestFocus();
                error = true;
            }
            if (password.isEmpty()) {
                passEditText.setError("Must enter a password.");
                passEditText.requestFocus();
                error = true;
            }
            if (confirmPass.isEmpty()) {
                confirmPassEditText.setError("Must enter a password.");
                confirmPassEditText.requestFocus();
                error = true;
            }
            if (!password.equals(confirmPass)) {
                passEditText.setError("Passwords must be the same.");
                passEditText.requestFocus();
                confirmPassEditText.requestFocus();
                error = true;
            }

            // Query to check if the username already exist.
            Query usernameQuery = FirebaseDatabase.getInstance().getReference().child("Users").orderByValue().equalTo(displayName);
            if (!error) {
                usernameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // If there is more than one username that is the same in the database.
                        if (snapshot.getChildrenCount() > 0) {
                            displayNameEditText.setError("Username already exist.");
                            displayNameEditText.requestFocus();
                        } else {
                            // Creates the user and adds the username to the database.
                            mFirebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(SignUp.this, "Sign up failed, please try again.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        String keyId = mDatabase.push().getKey();
                                        mDatabase.child(keyId).setValue(displayName);
                                        userProfile();
                                        startActivity(new Intent(SignUp.this, HomeScreen.class));
                                    }
                                }
                            });
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

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
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference("Users");

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