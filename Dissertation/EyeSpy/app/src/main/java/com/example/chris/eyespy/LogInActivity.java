package com.example.chris.eyespy;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class LogInActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "EmailPassword";

    private EditText emailField;
    private EditText passwordField;
    private Button loginButton;
    private Button signupButton;
    private Button closeButton;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popuplogin);

        emailField = (EditText) findViewById(R.id.Email);
        passwordField = (EditText) findViewById(R.id.Password);
        loginButton = (Button) findViewById(R.id.LogInButton);
        signupButton = (Button) findViewById(R.id.SignUpButton);
        closeButton = (Button) findViewById(R.id.CloseButton);

        mAuth = FirebaseAuth.getInstance();

    }

    private void createAccount(String email,String password){
        if(!validateForm()){
            return;
        }

        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user = mAuth.getCurrentUser();
                            onClick(closeButton);
                        }else{
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(LogInActivity.this, "Failed Sign Up", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
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
                            FirebaseUser user = mAuth.getCurrentUser();
                        }else{
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LogInActivity.this, "Failed Log In",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean validateForm(){
        boolean valid = true;

        String email = emailField.getText().toString();
        if(TextUtils.isEmpty(email)){
            emailField.setError("Required.");
            valid = false;
        }else{
            emailField.setError(null);
        }

        String password = passwordField.getText().toString();
        if(TextUtils.isEmpty(password)){
            passwordField.setError("Required.");
            valid = false;
        }else{
            passwordField.setError(null);
        }

        return valid;

    }


    public void onClick(View v){
        int i = v.getId();
        if(i == R.id.SignUpButton){
            createAccount(emailField.getText().toString(),passwordField.getText().toString());
        }
        else if(i == R.id.LogInButton){
            signIn(emailField.getText().toString(),passwordField.getText().toString());
        }
        else if(i == R.id.CloseButton){
            Intent changePageIntent = new Intent(this,MainActivity.class);
            startActivity(changePageIntent);
        }
    }





}
