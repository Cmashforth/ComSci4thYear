package com.example.chris.eyespy;


import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.media.Image;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import static android.os.Environment.DIRECTORY_PICTURES;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    static final int REQUEST_TAKE_PHOTO = 1;
    private ImageView mImageView;
    private Button camButton;
    private Button changeGPS;
    private TextView logInMessage;
    private Button logInButton;
    private FirebaseAuth mAuth;
    private Button signOutButton;
    private String mCurrentPhotoPath;
    private FirebaseDatabase db;
    private DatabaseReference myRef;
    private String pictureImagePath;
    private Button wifiButton;
    private StorageReference myStor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        camButton = (Button) findViewById(R.id.button_image);
        changeGPS = (Button) findViewById(R.id.GPS_Page);
        logInMessage = (TextView) findViewById(R.id.LogInMessage);
        logInButton = (Button) findViewById(R.id.LogInButton);
        signOutButton = (Button) findViewById(R.id.SignOutButton);
        wifiButton = (Button)findViewById(R.id.Wifi);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        myRef = db.getReference();
        myStor = FirebaseStorage.getInstance().getReference();

    }

    @Override
    public void onStart(){
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }


    @Override
    public void onClick(View view){
        if(view  == camButton){
            try{
                takePicture();
            } catch(IOException ex){
                Toast.makeText(MainActivity.this, "onClick Toast",Toast.LENGTH_SHORT).show();
                return;
            }
        }
        else if(view == changeGPS){
            changePage();
        }
        else if(view == logInButton){
            changeLogIn();
        }
        else if(view == signOutButton){
            signUserOut();
        }
        else if(view == wifiButton){
            changePageWifi();
        }
    }

    private void updateUI(FirebaseUser user){
        if(user != null){
            logInMessage.setText("You are logged in, well done!!!");
            logInButton.setVisibility(View.INVISIBLE);
            signOutButton.setVisibility(View.VISIBLE);
        }
        else{
            logInMessage.setText("You are not Logged In");
            signOutButton.setVisibility(View.INVISIBLE);
            logInButton.setVisibility(View.VISIBLE);
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        try{
            File image = File.createTempFile(
                    imageFileName,
                    ".jpg",
                    storageDir
            );
            mCurrentPhotoPath = "file:" + image.getAbsolutePath();
            return image;
        }catch(IOException ex){
            ex.printStackTrace();
            Toast.makeText(MainActivity.this, "Temp Toast",Toast.LENGTH_SHORT).show();
            return null;
        }

    }

    private void takePicture() throws IOException {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1024);
        }
        Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(takePic.resolveActivity(getPackageManager())!= null){
            File photoFile = null;
            try{
                photoFile = createImageFile();
            } catch(IOException ex){
                Toast.makeText(MainActivity.this, "Picture Toast",Toast.LENGTH_SHORT).show();
                return;
            }

            if(photoFile != null){
                Uri outputFileUri = FileProvider.getUriForFile(MainActivity.this,BuildConfig.APPLICATION_ID + ".provider",createImageFile());
                takePic.putExtra(MediaStore.EXTRA_OUTPUT,outputFileUri);
                startActivityForResult(takePic,REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK){
            Uri imageUri = Uri.parse(mCurrentPhotoPath);


            StorageReference uploadRef = myStor.child("images/"+imageUri.getLastPathSegment());
            UploadTask uploadTask = uploadRef.putFile(imageUri);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this,"Failed",Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    private String createString(Bitmap myBitmap){
        ByteArrayOutputStream boas = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG,100,boas);
        byte[] b = boas.toByteArray();
        String output = Base64.encodeToString(b,Base64.DEFAULT);
        return output;
    }

    private void changePage(){
        Intent changePageIntent = new Intent(this,GPSPage.class);
        startActivity(changePageIntent);
    }

    private void changePageWifi(){
        Intent changePageIntent = new Intent(this,WifiActivity.class);
        startActivity(changePageIntent);
    }

    private void changeLogIn(){
        Intent changePageIntent = new Intent(this,LogInActivity.class);
        startActivity(changePageIntent);
    }

    private void signUserOut(){
        mAuth.signOut();
        updateUI(null);
    }






}