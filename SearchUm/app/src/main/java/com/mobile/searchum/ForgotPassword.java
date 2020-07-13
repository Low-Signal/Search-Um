package com.mobile.searchum;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {

    private EditText emailEditText;
    private Button resetPassButton;
    private FirebaseAuth mFirebaseAuth;

    private final View.OnClickListener resetPassListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String userEmail = emailEditText.getText().toString();

            mFirebaseAuth.sendPasswordResetEmail(userEmail)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ForgotPassword.this, "Please check your email.", Toast.LENGTH_LONG).show();

                                startActivity(new Intent(ForgotPassword.this, MainActivity.class));
                            }
                        }
                    });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mFirebaseAuth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.emailEditText);
        resetPassButton = findViewById(R.id.resetPassButton);

        resetPassButton.setOnClickListener(resetPassListener);
    }
}