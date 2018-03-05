package com.example.chris.eyespy;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
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
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;



import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_LOCATION = 99;

    private ImageView mImageView;
    private Button camButton;
    private TextView topMessage;
    private Button getImageButton;
    private Button signOutButton;
    private Button checkButton;
    private ProgressBar spinner;

    private String mCurrentPhotoPath;
    private ImageData currentImageData;
    private int currentImageIndex;
    private ArrayList<String> connections;

    private DatabaseReference db;
    private StorageReference myStor;
    private FirebaseStorage storInst;
    private FirebaseAuth mAuth;

    private Location currentLocation;
    private WifiManager wifi;
    private int wifiScanCount;

    private List<Integer> playerCompletedImages;
    private List<Integer> playerSkippedImages;

    //Methods that override inbuilt methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        camButton = findViewById(R.id.button_image);
        topMessage = findViewById(R.id.topMessage);
        signOutButton = findViewById(R.id.SignOutButton);
        getImageButton = findViewById(R.id.getImage);
        mImageView = findViewById(R.id.image);
        checkButton = findViewById(R.id.checkButton);
        spinner = findViewById(R.id.spinner);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference();
        myStor = FirebaseStorage.getInstance().getReference();
        storInst = FirebaseStorage.getInstance();

        currentImageData = new ImageData();
        currentImageIndex = 0;
        connections = new ArrayList<>();
        wifiScanCount = 1;

        playerCompletedImages = new ArrayList<>();
        playerSkippedImages = new ArrayList<>();

        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (!wifi.isWifiEnabled()) {
            wifi.setWifiEnabled(true);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }

        spinner.setVisibility(View.INVISIBLE);
        nameDisplay(currentImageData.getUserID());
        createLocationRequest();

    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            signUserOut();
            createDialog("No User Account available, redirecting...");
            Intent redirectIntent = new Intent(this, LogInActivity.class);
            startActivity(redirectIntent);
        }
        if (mAuth.getUid() != null) {
            db.child("users").child(mAuth.getUid()).child("completedImages").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    GenericTypeIndicator<List<Integer>> indexList = new GenericTypeIndicator<List<Integer>>() {
                    };
                    playerCompletedImages = dataSnapshot.getValue(indexList);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    createDialog("Database Error, Completed List Assignment Cancelled");
                }
            });

            db.child("users").child(mAuth.getUid()).child("skippedImages").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    GenericTypeIndicator<List<Integer>> indexList = new GenericTypeIndicator<List<Integer>>() {
                    };
                    playerSkippedImages = dataSnapshot.getValue(indexList);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    createDialog("Database Error, Complete List Assignment Cancelled");
                }
            });
        } else {
            createDialog("Authentication Error: No User Logged In");
        }
    }

    @Override
    public void onClick(View view) {
        if (view == camButton) {
            try {
                takePicture();
            } catch (IOException ex) {
                createDialog("Camera Error, Cannot Access Camera");
            }
        } else if (view == signOutButton) {
            signUserOut();
        } else if (view == getImageButton) {
            if(!playerSkippedImages.contains(currentImageIndex)){
                playerSkippedImages.add(currentImageIndex);
                uploadSkippedList();
            }
            getImage();
        } else if (view == checkButton) {
            topMessage.setText(R.string.CheckingMessage);
            ImageData playerData = new ImageData(null, null);
            displaySettings(false);
            getGPS(playerData);
        }

    }

    //Image Upload Methods
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        try {
            File image = File.createTempFile(
                    imageFileName,
                    ".jpg",
                    storageDir
            );
            mCurrentPhotoPath = "file:" + image.getAbsolutePath();
            return image;
        } catch (IOException ex) {
            ex.printStackTrace();
            createDialog("File Error, File Creation Error");
            return null;
        }

    }

    private void takePicture() throws IOException {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1024);
        }
        Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePic.resolveActivity(getPackageManager()) != null) {
            try {
                File photoFile = createImageFile();
                if (photoFile != null && mAuth != null) {
                    Uri outputFileUri = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".provider", createImageFile());
                    takePic.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                    startActivityForResult(takePic, REQUEST_TAKE_PHOTO);
                } else {
                    createDialog("Image Capture Error, null values");
                }
            } catch (IOException ex) {
                createDialog("File Error, Cannot Access File");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            displaySettings(false);
            topMessage.setText(R.string.UploadProgress);
            Uri imageUri = Uri.parse(mCurrentPhotoPath);
            StorageReference uploadRef = myStor.child("images/" + imageUri.getLastPathSegment());
            UploadTask uploadTask = uploadRef.putFile(imageUri);
            uploadTask.addOnFailureListener(new OnFailureListener() {

                @Override
                public void onFailure(@NonNull Exception e) {
                    createDialog("Upload Error, Failed Upload");
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri newUri = taskSnapshot.getDownloadUrl();
                    if (newUri != null) {
                        final ImageData newImageData = new ImageData(newUri.toString(), mAuth.getUid());
                        getGPS(newImageData);
                    } else {
                        createDialog("Upload Error, No Download URL For File");
                        displaySettings(true);
                        nameDisplay(currentImageData.getUserID());
                    }
                }
            });
        } else {
            createDialog("Camera Error, Incorrect Request Codes");
        }

    }

    private void manageUpload(final ImageData imageData, final String mAuthID) {
        db.child("maxImageIndex").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Integer maxIndex = dataSnapshot.getValue(Integer.class);
                if (maxIndex != null) {
                    db.child("maxImageIndex").setValue(maxIndex + 1);
                    db.child("images").child(Integer.toString(maxIndex + 1)).setValue(imageData);
                    db.child("images").child(Integer.toString(maxIndex + 1)).child("userID").setValue(mAuthID);
                    db.child("images").child(Integer.toString(maxIndex + 1)).child("latitude").setValue(imageData.getLatitude());
                    db.child("images").child(Integer.toString(maxIndex + 1)).child("longitude").setValue(imageData.getLongitude());
                    db.child("images").child(Integer.toString(maxIndex + 1)).child("wifiNetworks").setValue(imageData.getWifiNetworks());
                    addToCompleteList(mAuthID, maxIndex + 1, true);
                } else {
                    createDialog("Database Error, Cannot Retrieve Maximum Index");
                    displaySettings(true);
                    nameDisplay(currentImageData.getUserID());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                createDialog("Upload Cancelled");
                displaySettings(true);
                nameDisplay(currentImageData.getUserID());
            }
        });

    }


    //Image Retrieval Methods
    private void getImage() {
        displaySettings(false);
        topMessage.setText(R.string.ImageMessage);
        db.child("maxImageIndex").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer maxIndex = dataSnapshot.getValue(Integer.class);
                if (maxIndex == null) {
                    createDialog("Database Error, No Maximum Index Exists");
                    displaySettings(true);
                    nameDisplay(currentImageData.getUserID());
                    return;
                }
                imageProcessing(maxIndex);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                displaySettings(true);
                nameDisplay(currentImageData.getUserID());
                createDialog("Database Error, Retreival Cancelled");
            }
        });
    }

    private void imageProcessing(int maxIndex) {
        List<Integer> availableImages = new ArrayList<>();
        if(playerCompletedImages.size() + playerSkippedImages.size() - 2 == maxIndex) {
            nameDisplay(null);
            createDialog("All images presented");
            currentImageIndex = 0;
            mImageView.setImageDrawable(null);
            getImageButton.setText(R.string.GetImageButtonText);
            playerSkippedImages.clear();
            displaySettings(true);
            uploadSkippedList();
            return;
        }
        for(int i = 1; i < maxIndex + 1; i++) {
            if (!playerCompletedImages.contains(i) && !playerSkippedImages.contains(i)) {
                availableImages.add(i);
            }
        }

        Random rand = new Random();
        int value = rand.nextInt(availableImages.size());
        currentImageIndex = availableImages.get(value);
        db.child("images").child(Integer.toString(currentImageIndex)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String imageURL = dataSnapshot.child("imageURL").getValue(String.class);
                if (imageURL != null) {
                    StorageReference httpsReference = storInst.getReferenceFromUrl(imageURL);
                    Glide.with(MainActivity.this)
                            .using(new FirebaseImageLoader())
                            .load(httpsReference)
                            .into(mImageView);

                } else {
                    createDialog("Download Error: Download URL does not Exist");
                }

                Double imageLatitude = dataSnapshot.child("latitude").getValue(Double.class);
                if (imageLatitude != null) {
                    currentImageData.setLatitude(imageLatitude);
                }

                Double imageLongitude = dataSnapshot.child("longitude").getValue(Double.class);
                if (imageLongitude != null) {
                    currentImageData.setLongitude(imageLongitude);
                }

                GenericTypeIndicator<List<String>> list = new GenericTypeIndicator<List<String>>() {
                };
                List<String> wifiList = dataSnapshot.child("wifiNetworks").getValue(list);
                currentImageData.setWifiNetworks(wifiList);

                String imageUserID = dataSnapshot.child("userID").getValue(String.class);
                if (imageUserID != null) {
                    currentImageData.setUserID(imageUserID);
                }
                getImageButton.setText(R.string.NextText);
                checkButton.setVisibility(View.VISIBLE);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                createDialog("Database Error, Image Retrieval Cancelled");
            }
        });

        displaySettings(true);
        nameDisplay(currentImageData.getUserID());
    }




    //Gps and Wifi Methods
    private void getGPS(final ImageData imageData){
        imageData.setLatitude(currentLocation.getLatitude());
        imageData.setLongitude(currentLocation.getLongitude());
        scanWifiNetworks(imageData);
    }

    private void createLocationRequest(){
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION);
        }else {
            getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    currentLocation = locationResult.getLastLocation();
                }
            }, Looper.myLooper());
        }

    }

    private void scanWifiNetworks(final ImageData imageData){
        if(wifi.getWifiState() == WifiManager.WIFI_STATE_ENABLED){
            connections.clear();
            IntentFilter filter = new IntentFilter();
            filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    List<ScanResult> results = wifi.getScanResults();
                    unregisterReceiver(this);
                    for(int i = 0; i < results.size(); i++){
                        connections.add(results.get(i).SSID + " "+ results.get(i).BSSID);
                    }
                    imageData.setWifiNetworks(connections);
                    if(imageData.getUserID() == null){
                        checkData(imageData);
                    }else{
                        manageUpload(imageData,imageData.getUserID());
                    }
                }
            },filter);
            wifi.startScan();
        }
    }


    //Check Method
    public void checkData(ImageData playerData){
        int wifiCount = 0;
        if(playerData.getWifiNetworks() == null){
            getGPS(playerData);
        }
        if(currentImageData.getWifiNetworks().size() > playerData.getWifiNetworks().size()){
            for(int i = 0; i < playerData.getWifiNetworks().size(); i++){
                if(currentImageData.getWifiNetworks().contains(playerData.getWifiNetworks().get(i))){
                    wifiCount = wifiCount + 1;
                }
            }
        }else{
            for(int i = 0; i < currentImageData.getWifiNetworks().size();i++){
                if(playerData.getWifiNetworks().contains(currentImageData.getWifiNetworks().get(i))){
                    wifiCount = wifiCount + 1;
                }
            }
        }

        if(coordCheck(playerData.getLatitude(),currentImageData.getLatitude()) && coordCheck(playerData.getLongitude(),currentImageData.getLongitude())) {
            if ((wifiCount >= currentImageData.getWifiNetworks().size() / 2 || wifiCount >= playerData.getWifiNetworks().size() / 2)) {
                addToCompleteList(mAuth.getUid(), currentImageIndex, false);
                pointsAllocation(currentImageData.getUserID(), 1);
                pointsAllocation(mAuth.getUid(), 2);

                db.child("images").child(Integer.toString(currentImageIndex)).child("correctCheckCount").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Integer count = dataSnapshot.getValue(Integer.class);
                        if (count != null) {
                            count = count + 1;
                            db.child("images").child(Integer.toString(currentImageIndex)).child("correctCheckCount").setValue(count);
                        } else {
                            createDialog("Database Error, Correct Check does not Exist");
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        createDialog("Database Error, Checking Cancelled");
                    }
                });
                createDialog("Correct, 2 points allocated");
                wifiScanCount = 1;
                getImage();
            } else {
                if (wifiScanCount < 3) {
                    wifiScanCount++;
                    getGPS(playerData);
                } else {
                    wifiScanCount = 1;
                    createDialog("Incorrect, Not Enough Matching Wifi Addresses. Location cannot be Verified");
                    displaySettings(true);
                    nameDisplay(currentImageData.getUserID());
                }
            }
        }else{
            createDialog("Incorrect, More than 22m away from Location of Picture");
            displaySettings(true);
            nameDisplay(currentImageData.getUserID());
        }
    }

    private boolean coordCheck(double playerCoord, double imageCoord){
        if( (playerCoord > 0.0 && imageCoord > 0.0) || (playerCoord < 0.0 && imageCoord < 0.0) ){
            double diff = playerCoord*1000 - imageCoord*1000;
            return (Math.round(Math.abs(diff*10)) <= 2);
        }else{
            double diff = playerCoord*1000 + imageCoord*1000;
            return (Math.round(Math.abs(diff*10)) <= 2);
        }
    }


    //Database Methods
    public void addToCompleteList(final String userID, final int imageIndex, boolean processEnd){
        playerCompletedImages.add(imageIndex);
        db.child("users").child(userID).child("completedImages").setValue(playerCompletedImages);
        if(processEnd){
            createDialog("Upload Finished");
            displaySettings(true);
            nameDisplay(currentImageData.getUserID());
        }
    }

    public void pointsAllocation(final String playerID,final int value){
        db.child("users").child(playerID).child("points").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer points = dataSnapshot.getValue(Integer.class);
                if(points != null){
                    points = points + value;
                    db.child("users").child(playerID).child("points").setValue(points);
                }else{
                    createDialog("Database Error, User Points Does Not Exist");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                createDialog("Database Error, Points Allocation Cancelled");
            }
        });

    }

    public void uploadSkippedList(){
        if(mAuth.getUid() != null) {
            db.child("users").child(mAuth.getUid()).child("skippedImages").setValue(playerSkippedImages);
        }else{
            createDialog("Database Error, Skipped Images not uploaded");
        }
    }

    //onClick Methods
    private void signUserOut(){
        mAuth.signOut();
        Intent exitIntent = new Intent(this,LogInActivity.class);
        startActivity(exitIntent);
    }

    //Display methods
    private void displaySettings(boolean setting){
        camButton.setEnabled(setting);
        getImageButton.setEnabled(setting);
        checkButton.setEnabled(setting);
        signOutButton.setEnabled(setting);

        if(!setting){
            mImageView.setVisibility(View.INVISIBLE);
            spinner.setVisibility(View.VISIBLE);
        }else{
            spinner.setVisibility(View.INVISIBLE);
            mImageView.setVisibility(View.VISIBLE);
        }
    }

    private void nameDisplay(String userID){
        if(userID == null) {
            if (mAuth.getUid() != null) {
                db.child("users").child(mAuth.getUid()).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String message = dataSnapshot.getValue(String.class);
                        db.child("users").child(mAuth.getUid()).child("points").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Integer numPoints = dataSnapshot.getValue(Integer.class);
                                if (numPoints != null) {
                                    topMessage.setText(message + ": " + numPoints.toString() + " Points");
                                } else {
                                    createDialog("Database Error, User Point Value Does Not Exist");

                                    topMessage.setText(message);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                createDialog("Database Error, Display Cancelled");
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        createDialog("Database Error, Display Cancelled");
                    }
                });
            } else {
                createDialog("Database Error, User Account Does Not Exist");
            }
        }else{
            db.child("users").child(userID).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Resources res = getResources();
                    String message = String.format(res.getString(R.string.UploaderMessage),dataSnapshot.getValue(String.class));
                    topMessage.setText(message);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void createDialog(String message){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}