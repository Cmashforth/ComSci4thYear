package com.example.chris.eyespy;


import android.Manifest;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;



import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;



public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_LOCATION = 99;

    private ImageView mImageView;
    private Button camButton;
    private TextView logInMessage;
    private Button logInButton;
    private Button getImageButton;
    private Button signOutButton;
    private Button wifiButton;

    private String mCurrentPhotoPath;
    private ProgressBar progressBar;

    private DatabaseReference db;
    private StorageReference myStor;
    private FirebaseStorage storInst;
    private FirebaseAuth mAuth;

    private FusedLocationProviderClient mLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        camButton = findViewById(R.id.button_image);
        logInMessage = findViewById(R.id.LogInMessage);
        logInButton =  findViewById(R.id.LogInButton);
        signOutButton = findViewById(R.id.SignOutButton);
        wifiButton = findViewById(R.id.Wifi);
        progressBar = findViewById(R.id.uploadProgress);
        getImageButton = findViewById(R.id.getImage);
        mImageView = findViewById(R.id.image);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference();
        myStor = FirebaseStorage.getInstance().getReference();
        storInst = FirebaseStorage.getInstance();

        mLocationClient = LocationServices.getFusedLocationProviderClient(this);

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

            }
        } else if(view == logInButton){
            changeLogIn();
        } else if(view == signOutButton){
            signUserOut();
        } else if(view == wifiButton){
            changePageWifi();
        } else if(view == getImageButton){
            getImage();
        }

    }

    private void updateUI(FirebaseUser user){
        if(user != null){
            logInMessage.setText(R.string.SuccessfulLogInMessage);
            logInButton.setVisibility(View.INVISIBLE);
            signOutButton.setVisibility(View.VISIBLE);
        }
        else{
            logInMessage.setText(R.string.LogInMessage);
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
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                    System.out.println("Upload is " + progress + "% done");
                    progressBar.setProgress((int) progress);

                }
            }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    Toast.makeText(MainActivity.this,"Upload Finished",Toast.LENGTH_SHORT).show();
                    progressBar.setProgress(0);
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri newUri = taskSnapshot.getDownloadUrl();
                    final Upload newUpload = new Upload(newUri.toString(),FirebaseAuth.getInstance().getCurrentUser().getUid());
                    getGPS(newUpload);
                    manageUpload(newUpload);
                }
            });
        }

    }

    private void getImage(){
        db.child("maxImageIndex").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer maxIndex = dataSnapshot.getValue(Integer.class);
                if(maxIndex == 0){
                    Toast.makeText(MainActivity.this, "No images to download",Toast.LENGTH_SHORT).show();
                    return;
                }
                imageProcessing(1,maxIndex);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void imageProcessing(final int index, final int maxIndex){
        db.child("users").child(mAuth.getUid()).child("completedImages").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<Integer>> indexList = new GenericTypeIndicator<List<Integer>>(){};
                List<Integer> completedIndexList = dataSnapshot.getValue(indexList);
                if(completedIndexList.contains(index)){
                    int currentIndex = index + 1;
                    imageProcessing(currentIndex,maxIndex);
                } else{
                    if(index > maxIndex){
                        Toast.makeText(MainActivity.this,"All Images already checked",Toast.LENGTH_SHORT).show();
                    }else{
                        db.child("images").child(Integer.toString(index)).child("imageURL").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                StorageReference httpsReference = storInst.getReferenceFromUrl(dataSnapshot.getValue(String.class));
                                Glide.with(MainActivity.this)
                                    .using(new FirebaseImageLoader())
                                    .load(httpsReference)
                                    .into(mImageView);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void manageUpload(final Upload upload){
        db.child("maxImageIndex").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final int maxIndex = dataSnapshot.getValue(Integer.class);
                db.child("maxImageIndex").setValue(maxIndex + 1);
                db.child("images").child(Integer.toString(maxIndex + 1)).setValue(upload);
                db.child("users").child(mAuth.getUid()).child("completedImages").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List uploads = (List<Integer>) dataSnapshot.getValue();
                        uploads.add(maxIndex + 1);
                        db.child("users").child(mAuth.getUid()).child("completedImages").setValue(uploads);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getGPS(final Upload upload){
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION);
        }
        mLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location mLastLocation = task.getResult();
                        upload.setLatitude(mLastLocation.getLatitude());
                        upload.setLongitude(mLastLocation.getLongitude());
                    }
                });
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