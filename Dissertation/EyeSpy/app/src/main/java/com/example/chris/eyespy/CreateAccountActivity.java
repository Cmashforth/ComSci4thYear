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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Chris on 05/02/2018.
 */

public class CreateAccountActivity extends AppCompatActivity {

    private EditText emailField;
    private EditText passwordField;
    private EditText usernameField;
    private Button createButton;
    private Toolbar toolbar;

    private FirebaseAuth mAuth;
    private DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createaccount);

        emailField = findViewById(R.id.Email);
        passwordField = findViewById(R.id.Password);
        usernameField = findViewById(R.id.UserName);
        createButton = findViewById(R.id.SignUpButton);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void onStart(){
        super.onStart();
        if(mAuth.getCurrentUser() != null){
            onClick(null);
        }
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
                            if(user != null){
                                User newUser = new User(user.getEmail(),usernameField.getText().toString());
                                db.child("users").child(user.getUid()).setValue(newUser);
                                List<Integer> completedImages = new ArrayList<>(Collections.singletonList(0));
                                db.child("users").child(user.getUid()).child("completedImages").setValue(completedImages);
                                List<Integer> skippedImages = new ArrayList<>(Collections.singletonList(0));
                                db.child("users").child(user.getUid()).child("skippedImages").setValue(skippedImages);
                                onClick(null);
                            } else{
                                Toast.makeText(CreateAccountActivity.this,"Authentication Error, User Account Does Not Exist ",Toast.LENGTH_SHORT).show();
                            }

                        }else{
                            Toast.makeText(CreateAccountActivity.this, "Authentication Error, Account Creation Failed", Toast.LENGTH_SHORT).show();
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

        String username = usernameField.getText().toString();
        if(TextUtils.isEmpty(username)){
            usernameField.setError("Required");
            valid = false;
        }else{
            usernameField.setError(null);
        }

        return valid;

    }

    public void onClick(View v){
        if(v == createButton){
            createAccount(emailField.getText().toString(),passwordField.getText().toString());
        }else{
            Intent changePageIntent = new Intent(this,MainActivity.class);
            startActivity(changePageIntent);
        }
    }
}
