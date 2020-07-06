package com.mobile.searchum;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePassword extends AppCompatActivity {
    private EditText currentPassEditText, newPassEditText, confirmPassEditText;
    private Button changePassButton;

    private FirebaseAuth mFirebaseAuth;

    //TODO: Change password button
    private final View.OnClickListener changePassListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String currentPass, newPass, confirmPass;
            FirebaseUser user = mFirebaseAuth.getCurrentUser();

            currentPass = currentPassEditText.getText().toString();
            newPass = newPassEditText.getText().toString();
            confirmPass = confirmPassEditText.getText().toString();

            Toast.makeText(ChangePassword.this, "Test", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        mFirebaseAuth = FirebaseAuth.getInstance();

        currentPassEditText = findViewById(R.id.currentPassEditText);
        newPassEditText = findViewById(R.id.newPassEditText);
        confirmPassEditText = findViewById(R.id.confirmPassEditText);
        changePassButton = findViewById(R.id.changePassButton);

        changePassButton.setOnClickListener(changePassListener);
    }
}