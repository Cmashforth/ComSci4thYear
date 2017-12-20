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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class LogInActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "EmailPassword";

    private EditText emailField;
    private EditText passwordField;
    private EditText usernameField;
    private Button loginButton;
    private Button signupButton;
    private Button closeButton;

    private FirebaseAuth mAuth;
    private DatabaseReference db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popuplogin);

        emailField = findViewById(R.id.Email);
        passwordField = findViewById(R.id.Password);
        usernameField = findViewById(R.id.UserName);
        loginButton = findViewById(R.id.LogInButton);
        signupButton = findViewById(R.id.SignUpButton);
        closeButton = findViewById(R.id.CloseButton);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference();

    }

    private void createAccount(String email,String password){
        if(!validateForm(1)){
            return;
        }

        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user = mAuth.getCurrentUser();
                            if(user != null){
                                User newUser = new User(user.getEmail(),usernameField.getText().toString());
                                db.child("users").child(user.getUid()).setValue(newUser);
                                List<Integer> startUpload = new ArrayList<>(Collections.singletonList(0));
                                db.child("users").child(user.getUid()).child("completedImages").setValue(startUpload);
                                onClick(closeButton);
                            } else{
                                Toast.makeText(LogInActivity.this,"User Databasing Error",Toast.LENGTH_SHORT).show();
                            }

                        }else{
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(LogInActivity.this, "Failed Sign Up", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    private void signIn(String email, String password){
        if(!validateForm(2)){
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            onClick(closeButton);
                        }else{
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LogInActivity.this, "Failed Log In",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean validateForm(int code){
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

        if(code == 1){
            String username = usernameField.getText().toString();
            if(TextUtils.isEmpty(username)){
                usernameField.setError("Required");
                valid = false;
            } else{
                usernameField.setError(null);
            }
        }
        return valid;

    }

    public void onClick(View v){
        if(v == signupButton){
            createAccount(emailField.getText().toString(),passwordField.getText().toString());
        }
        else if(v == loginButton){
            signIn(emailField.getText().toString(),passwordField.getText().toString());
        }
        else if(v == closeButton){
            Intent changePageIntent = new Intent(this,StartUpActivity.class);
            startActivity(changePageIntent);
        }
    }





}
