package com.example.chris.eyespy;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class LogInActivity extends AppCompatActivity {

    private EditText emailField;
    private EditText passwordField;
    private Button loginButton;
    private Button signupButton;
    private Toolbar toolbar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailField = findViewById(R.id.Email);
        passwordField = findViewById(R.id.Password);
        loginButton = findViewById(R.id.LogInButton);
        signupButton = findViewById(R.id.SignUpButton);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart(){
        super.onStart();
        if(mAuth.getCurrentUser() != null){
            onClick(null);
        }
    }

    private void signIn(String email, String password){
        if(!validateForm()){
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            onClick(null);
                        }else{
                            Toast.makeText(LogInActivity.this, "Authentication Error, Login Failed",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean validateForm(){
        boolean valid = true;

        String email = emailField.getText().toString();
        if(TextUtils.isEmpty(email)){
            emailField.setError("Required");
            valid = false;
        }else{
            emailField.setError(null);
        }

        String password = passwordField.getText().toString();
        if(TextUtils.isEmpty(password)){
            passwordField.setError("Required");
            valid = false;
        }else{
            passwordField.setError(null);
        }

        return valid;

    }

    public void onClick(View v){
        if(v == signupButton){
            Intent changePageIntent = new Intent(this,CreateAccountActivity.class);
            startActivity(changePageIntent);
        }
        else if(v == loginButton){
            signIn(emailField.getText().toString(),passwordField.getText().toString());
        }else{
            Intent changePageIntent = new Intent(this,MainActivity.class);
            startActivity(changePageIntent);
        }
    }

}
