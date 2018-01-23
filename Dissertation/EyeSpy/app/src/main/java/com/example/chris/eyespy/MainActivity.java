package com.example.chris.eyespy;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.provider.MediaStore;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_LOCATION = 99;

    private ImageView mImageView;
    private Button camButton;
    private TextView topMessage;
    private Button getImageButton;
    private Button signOutButton;
    private Button checkButton;

    private String mCurrentPhotoPath;
    private ProgressBar progressBar;
    private ImageData currentImageData;
    private int currentImageIndex;

    private DatabaseReference db;
    private StorageReference myStor;
    private FirebaseStorage storInst;
    private FirebaseAuth mAuth;

    private FusedLocationProviderClient mLocationClient;
    private WifiManager wifi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        camButton = findViewById(R.id.button_image);
        topMessage = findViewById(R.id.topMessage);
        signOutButton = findViewById(R.id.SignOutButton);
        progressBar = findViewById(R.id.uploadProgress);
        getImageButton = findViewById(R.id.getImage);
        mImageView = findViewById(R.id.image);
        checkButton = findViewById(R.id.checkButton);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference();
        myStor = FirebaseStorage.getInstance().getReference();
        storInst = FirebaseStorage.getInstance();

        mLocationClient = LocationServices.getFusedLocationProviderClient(this);
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        currentImageData = new ImageData();
        currentImageIndex = 1;

    }

    //Methods that override inbuilt methods
    @Override
    public void onStart(){
        super.onStart();
        if(mAuth.getCurrentUser() == null){
            signUserOut();
        }
    }


    @Override
    public void onClick(View view){
        if(view  == camButton){
            try{
                takePicture();
            } catch(IOException ex){
                Toast.makeText(MainActivity.this, "Camera Error",Toast.LENGTH_SHORT).show();
            }
        } else if(view == signOutButton){
            signUserOut();
        } else if(view == getImageButton){
            getImage();
        } else if(view == checkButton){
            ImageData playerData = new ImageData(null,null);
            buttonSettings(false);
            getGPS(playerData);
        }

    }

    //Image ImageData Methods
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
            Toast.makeText(MainActivity.this, "File Creation Error",Toast.LENGTH_SHORT).show();
            return null;
        }

    }

    private void takePicture() throws IOException {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1024);
        }
        Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePic.resolveActivity(getPackageManager())!= null){
            try{
                File photoFile = createImageFile();
                if(photoFile != null && mAuth != null){
                    Uri outputFileUri = FileProvider.getUriForFile(MainActivity.this,BuildConfig.APPLICATION_ID + ".provider",createImageFile());
                    takePic.putExtra(MediaStore.EXTRA_OUTPUT,outputFileUri);
                    startActivityForResult(takePic,REQUEST_TAKE_PHOTO);
                }
            } catch(IOException ex){
                Toast.makeText(MainActivity.this, "Can't Create File",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK){
            buttonSettings(false);
            topMessage.setText(R.string.UploadText);
            Uri imageUri = Uri.parse(mCurrentPhotoPath);
            StorageReference uploadRef = myStor.child("images/"+imageUri.getLastPathSegment());
            UploadTask uploadTask = uploadRef.putFile(imageUri);
            uploadTask.addOnFailureListener(new OnFailureListener() {


                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this,"Failed Upload",Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                    progressBar.setProgress((int) progress);
                }
            }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    progressBar.setProgress(0);
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri newUri = taskSnapshot.getDownloadUrl();
                    if(newUri != null){
                        final ImageData newImageData = new ImageData(newUri.toString(),mAuth.getUid());
                        getGPS(newImageData);
                    }else{
                        //final ImageData newImageData = new ImageData("Error",mAuth.getUid());
                        //manageUpload(newImageData,mAuth.getUid());
                    }

                }
            });
        }

    }

    private void manageUpload(final ImageData imageData, final String mAuthID){
        db.child("maxImageIndex").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Integer maxIndex = dataSnapshot.getValue(Integer.class);
                if (maxIndex != null) {
                    imageData.setIndex(maxIndex + 1);
                    db.child("maxImageIndex").setValue(maxIndex + 1);
                    db.child("images").child(Integer.toString(maxIndex + 1)).setValue(imageData);
                    db.child("images").child(Integer.toString(maxIndex + 1)).child("userID").setValue(mAuthID);
                    db.child("images").child(Integer.toString(maxIndex + 1)).child("latitude").setValue(imageData.getLatitude());
                    db.child("images").child(Integer.toString(maxIndex + 1)).child("longitude").setValue(imageData.getLongitude());
                    db.child("images").child(Integer.toString(maxIndex + 1)).child("wifiNetworks").setValue(imageData.getWifiNetworks());
                    addToCompleteList(mAuthID,maxIndex + 1);
                }else{
                    Toast.makeText(MainActivity.this,"Max Integer Error",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    //Image Retrieval Methods
    private void getImage(){
        db.child("maxImageIndex").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer maxIndex = dataSnapshot.getValue(Integer.class);
                if(maxIndex == null){
                    Toast.makeText(MainActivity.this, "No Maximum Index Exists",Toast.LENGTH_SHORT).show();
                    return;
                }
                imageProcessing(maxIndex);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void imageProcessing(final int maxIndex){
        if(mAuth.getUid() != null) {
            db.child("users").child(mAuth.getUid()).child("completedImages").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    GenericTypeIndicator<List<Integer>> indexList = new GenericTypeIndicator<List<Integer>>() {};
                    List<Integer> completedIndexList = dataSnapshot.getValue(indexList);
                    if (completedIndexList != null) {
                        while (completedIndexList.contains(currentImageIndex)) {
                            currentImageIndex = currentImageIndex + 1;
                        }
                        if(currentImageIndex > maxIndex){
                            Toast.makeText(MainActivity.this,"All images presented, Resetting Index",Toast.LENGTH_SHORT).show();
                            currentImageIndex = 0;
                        }
                        Toast.makeText(MainActivity.this,"Index Value: " + currentImageIndex,Toast.LENGTH_SHORT).show();
                        db.child("images").child(Integer.toString(currentImageIndex)).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String imageURL  = dataSnapshot.child("imageURL").getValue(String.class);
                                if (imageURL != null) {
                                    StorageReference httpsReference = storInst.getReferenceFromUrl(imageURL);
                                    Glide.with(MainActivity.this)
                                            .using(new FirebaseImageLoader())
                                            .load(httpsReference)
                                            .into(mImageView);

                                } else {
                                    Toast.makeText(MainActivity.this, "Download URL does not Exist", Toast.LENGTH_SHORT).show();
                                }

                                Double imageLatitude = dataSnapshot.child("latitude").getValue(Double.class);
                                if(imageLatitude != null){
                                    currentImageData.setLatitude(imageLatitude);
                                }

                                Double imageLongitude = dataSnapshot.child("longitude").getValue(Double.class);
                                if(imageLongitude != null){
                                    currentImageData.setLongitude(imageLongitude);
                                }

                                GenericTypeIndicator<List<String>> list = new GenericTypeIndicator<List<String>>(){};
                                List<String> wifiList = dataSnapshot.child("wifiNetworks").getValue(list);
                                currentImageData.setWifiNetworks(wifiList);

                                String imageUserID = dataSnapshot.child("userID").getValue(String.class);
                                if(imageUserID != null){
                                    currentImageData.setUserID(imageUserID);
                                }

                                currentImageData.setIndex(currentImageIndex);
                                currentImageIndex = currentImageIndex + 1;
                                checkButton.setVisibility(View.VISIBLE);

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {}
                        });
                    } else{
                        Toast.makeText(MainActivity.this,"Error: No Completed Index List",Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }else{
            Toast.makeText(MainActivity.this,"No User Logged In",Toast.LENGTH_SHORT).show();
        }
    }

    //getGps and Wifi Methods
    private void getGPS(final ImageData imageData){
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION);
        }
        mLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location mLastLocation = task.getResult();
                        imageData.setLatitude(mLastLocation.getLatitude());
                        imageData.setLongitude(mLastLocation.getLongitude());
                        scanWifiNetworks(imageData);
                    }
                });
    }

    private void scanWifiNetworks(final ImageData imageData){ //needs fixing
        wifi.startScan();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                List<ScanResult> results = wifi.getScanResults();
                ArrayList<String> connections = new ArrayList<>();
                for(int i = 0; i < results.size(); i++){
                    connections.add(results.get(i).SSID + " "+ results.get(i).BSSID);
                }
                imageData.setWifiNetworks(connections);
                if(imageData.getUserID() == null){
                    checkData(imageData);
                }else{
                    manageUpload(imageData, imageData.getUserID());
                }


            }
        },filter);
    }

    //Check Method
    public void checkData(ImageData playerData){
        double wifiCount = 0.0;
        for(int i = 0; i < currentImageData.getWifiNetworks().size();i++){
            if(currentImageData.getWifiNetworks().contains(playerData.getWifiNetworks().get(i))){
                wifiCount = wifiCount + 1.0;
            }
        }
        if(wifiCount/currentImageData.getWifiNetworks().size() >= 0.5 &&
                ((double)Math.round(playerData.getLatitude() * 10000d) / 10000d) == ((double)Math.round(currentImageData.getLatitude() * 10000d) / 10000d) &&
                ((double)Math.round(playerData.getLongitude() * 10000d) / 10000d) == ((double)Math.round(currentImageData.getLongitude() * 10000d) / 10000d)) {

            addToCompleteList(mAuth.getUid(), currentImageData.getIndex());
            pointsAllocation(currentImageData.getUserID(), 1);
            pointsAllocation(mAuth.getUid(), 5);

            db.child("images").child(Integer.toString(currentImageData.getIndex())).child("correctCheckCount").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Integer count = dataSnapshot.getValue(Integer.class);
                    if (count != null) {
                        count = count + 1;
                        db.child("images").child(Integer.toString(currentImageData.getIndex())).child("correctCheckCount").setValue(count);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            topMessage.setText("Correct");
            getImage();
        } else{
            topMessage.setText("Incorrect");
        }
        buttonSettings(true);
    }

    //Database Methods
    public void addToCompleteList(final String userID, final int imageIndex){
        db.child("users").child(userID).child("completedImages").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<Integer>> indexList = new GenericTypeIndicator<List<Integer>>() {
                };
                List<Integer> uploads = dataSnapshot.getValue(indexList);
                if (uploads != null) {
                    uploads.add(imageIndex);
                    db.child("users").child(userID).child("completedImages").setValue(uploads);
                } else {
                    Toast.makeText(MainActivity.this, "No ImageData List Exists", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        buttonSettings(true);
    }

    public void pointsAllocation(final String playerID,final int value){
        db.child("users").child(playerID).child("points").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer points = dataSnapshot.getValue(Integer.class);
                if(points != null){
                    points = points + value;
                    db.child("users").child(playerID).child("points").setValue(points);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    //onClick Methods
    private void signUserOut(){
        mAuth.signOut();
        Intent exitIntent = new Intent(this,StartUpActivity.class);
        startActivity(exitIntent);
    }

    //Display methods
    private void buttonSettings(boolean setting){
        camButton.setEnabled(setting);
        getImageButton.setEnabled(setting);
        checkButton.setEnabled(setting);
        signOutButton.setEnabled(setting);
    }


}