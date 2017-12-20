package com.example.chris.eyespy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by Chris on 20/12/2017.
 */

public class StartUpActivity extends AppCompatActivity {

    private Button loginButton;
    private Button signupButton;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        loginButton = findViewById(R.id.LogInButton);
        signupButton = findViewById(R.id.SignUpButton);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart(){
        super.onStart();
        if(mAuth.getCurrentUser() != null){
            onClick(null);
        }
    }

    public void onClick(View view){
        if(view == null){
            Intent changePageIntent = new Intent(this,MainActivity.class);
            startActivity(changePageIntent);
        }else{
            Intent changePageIntent = new Intent(this,LogInActivity.class);
            startActivity(changePageIntent);
        }
    }


}

