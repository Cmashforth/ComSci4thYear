package com.example.chris.eyespy;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
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
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView) findViewById(R.id.imageView);
        camButton = (Button) findViewById(R.id.button_image);
        changeGPS = (Button) findViewById(R.id.GPS_Page);
        logInMessage = (TextView) findViewById(R.id.LogInMessage);
        logInButton = (Button) findViewById(R.id.LogInButton);
        signOutButton = (Button) findViewById(R.id.SignOutButton);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        myRef = db.getReference();

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
            signOutButton.setVisibility(View.VISIBLE);
        }
        else{
            logInMessage.setText("You are not Logged In");
            signOutButton.setVisibility(View.INVISIBLE);
            logInButton.setVisibility(View.VISIBLE);
        }
    }

    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName,".jpg",storageDir);
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void takePicture(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try{
                photoFile = createImageFile();
            } catch(IOException ex){
                Toast.makeText(MainActivity.this, "Failed Image Creation",Toast.LENGTH_SHORT).show();
            }
            if(photoFile != null){
                Uri photoURI = FileProvider.getUriForFile(this,"com.example.android.fileprovider",photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }

    }

    @Override
    protected void onActivityResult(int resultCode, int requestCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);

        if(requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            File imageFile = new File(imageUri.toString());
            String encodedImage = encodeImage(imageFile.getAbsolutePath());
        }
    }

    private String encodeImage(String path){
        File imageFile = new File(path);
        FileInputStream fis = null;
        try{
            fis = new FileInputStream(imageFile);
        } catch(FileNotFoundException e){
            e.printStackTrace();
        }
        Bitmap bm = BitmapFactory.decodeStream(fis);
        ByteArrayOutputStream boas = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG,100,boas);
        byte[] b = boas.toByteArray();
        String encImage = Base64.encodeToString(b,Base64.DEFAULT);
        return encImage;
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

    public void signUserOut(View view){
        mAuth.signOut();
        updateUI(null);
    }






}
