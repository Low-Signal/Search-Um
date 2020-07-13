package com.mobile.searchum;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
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
            final String currentPass, newPass, confirmPass;
            final FirebaseUser user = mFirebaseAuth.getCurrentUser();

            currentPass = currentPassEditText.getText().toString().trim();
            newPass = newPassEditText.getText().toString().trim();
            confirmPass = confirmPassEditText.getText().toString().trim();

            if(newPass.isEmpty() || confirmPass.isEmpty()){
                newPassEditText.setError("Must enter a password.");
                confirmPassEditText.setError("Must enter a password.");
                newPassEditText.requestFocus();
            }
            else if(!newPass.equals(confirmPass)){
                newPassEditText.setError("Passwords must match.");
                confirmPassEditText.setError("Passwords must match.");
                newPassEditText.requestFocus();
            }
            else{
                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPass);

                user.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    user.updatePassword(confirmPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(ChangePassword.this, "Password updated", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(ChangePassword.this, MainActivity.class));
                                            }
                                            else{
                                                Toast.makeText(ChangePassword.this, "Password update failed", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                                else{
                                    Toast.makeText(ChangePassword.this, "Failed authentication", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
            
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