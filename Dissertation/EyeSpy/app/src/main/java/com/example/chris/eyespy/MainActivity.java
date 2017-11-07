package com.example.chris.eyespy;


import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView mImageView;
    private Button camButton;
    private Button changeGPS;
    private TextView logInMessage;
    private Button logInButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView) findViewById(R.id.imageView);
        camButton = (Button) findViewById(R.id.button_image);
        changeGPS = (Button) findViewById(R.id.GPS_Page);
        logInMessage = (TextView) findViewById(R.id.LogInMessage);
        logInButton = (Button) findViewById(R.id.LogInButton);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart(){
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser user){
        if(user != null){
            logInMessage.setText("You are logged in, well done!!!");
            logInButton.setVisibility(View.INVISIBLE);
        }
        else{

        }
    }

    public void takePicture(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }

    }

    public void changePage(View view){
        Intent changePageIntent = new Intent(this,GPSPage.class);
        startActivity(changePageIntent);
    }

    public void changePageWifi(View view){
        Intent changePageIntent = new Intent(this,WifiActivity.class);
        startActivity(changePageIntent);
    }

    public void changeLogIn(View view){
        Intent changePageIntent = new Intent(this,LogInActivity.class);
        startActivity(changePageIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mImageView.setImageBitmap(imageBitmap);
        }
    }

}
