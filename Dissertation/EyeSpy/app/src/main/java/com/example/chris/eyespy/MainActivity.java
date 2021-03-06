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
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;



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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_LOCATION = 99;

    //Objects that represent UI elements
    private ImageView mImageView;
    private Button camButton;
    private TextView topMessage;
    private Button getImageButton;
    private Button checkButton;
    private ProgressBar spinner;
    private Toolbar toolbar;

    //Global variables used in various methods
    private String mCurrentPhotoPath;
    private ImageData currentImageData;
    private int currentImageIndex;
    private ArrayList<String> connections;

    //Firebase variables
    private DatabaseReference db;
    private StorageReference myStor;
    private FirebaseStorage storInst;
    private FirebaseAuth mAuth;

    //Variables used within location gathering
    private Location currentLocation;
    private WifiManager wifi;
    private int wifiScanCount;

    //Lists for image retrieval
    private List<Integer> playerCompletedImages;
    private List<Integer> playerSkippedImages;

    //Called upon creation of the activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Register UI elements
        setContentView(R.layout.activity_main);
        camButton = findViewById(R.id.button_image);
        topMessage = findViewById(R.id.topMessage);
        getImageButton = findViewById(R.id.getImage);
        mImageView = findViewById(R.id.image);
        checkButton = findViewById(R.id.checkButton);
        spinner = findViewById(R.id.spinner);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Register Firebase variables
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference();
        myStor = FirebaseStorage.getInstance().getReference();
        storInst = FirebaseStorage.getInstance();

        //Initalise global variables
        currentImageData = new ImageData();
        currentImageIndex = 0;
        connections = new ArrayList<>();
        wifiScanCount = 1;
        playerCompletedImages = new ArrayList<>();
        playerSkippedImages = new ArrayList<>();

        //Turn on devices WiFi if not turned on
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (!wifi.isWifiEnabled()) {
            wifi.setWifiEnabled(true);
        }

        //Requests Permission for the application to track location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }

        //Set the spinner to be invisible, display the players name on the screen, start requesting location
        spinner.setVisibility(View.INVISIBLE);
        nameDisplay(currentImageData.getUserID());
        createLocationRequest();

    }

    //Call upon startup of activity
    @Override
    public void onStart() {
        super.onStart();
        //If no account information can be found, send user back to login screen
        if (mAuth.getCurrentUser() == null) {
            signUserOut();
            createDialog("No User Account available, redirecting...");
            Intent redirectIntent = new Intent(this, LogInActivity.class);
            startActivity(redirectIntent);
        }
        if (mAuth.getUid() != null) {
            //Retrieve the player's completedImages list from the database
            db.child("users").child(mAuth.getUid()).child("completedImages").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    GenericTypeIndicator<List<Integer>> indexList = new GenericTypeIndicator<List<Integer>>() {
                    };
                    playerCompletedImages = dataSnapshot.getValue(indexList);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    createDialog("Database Error, Completed List Assignment Cancelled. Restart Application and Contact Moderator if the problem persists");
                }
            });

            //Retrieve the players skippedImages list from the database
            db.child("users").child(mAuth.getUid()).child("skippedImages").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    GenericTypeIndicator<List<Integer>> indexList = new GenericTypeIndicator<List<Integer>>() {
                    };
                    playerSkippedImages = dataSnapshot.getValue(indexList);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    createDialog("Database Error, Skipped List Assignment Cancelled. Restart Application and Contact Moderator if the problem persists");
                }
            });
        } else {
            createDialog("No User Account available, redirecting...");
            Intent redirectIntent = new Intent(this, LogInActivity.class);
            startActivity(redirectIntent);
        }
    }

    //Methods that run following the tap of a button
    @Override
    public void onClick(View view) {
        //Press 'submit Image' button, open camera
        if (view == camButton) {
            try {
                takePicture();
            } catch (IOException ex) {
                createDialog("Camera Error, Cannot Access Camera");
            }
            //Press 'Next Image', retrieve image. Add current images index to skippedList if one is on screen
        } else if (view == getImageButton) {
            if(!playerSkippedImages.contains(currentImageIndex)){
                playerSkippedImages.add(currentImageIndex);
                uploadSkippedList();
            }
            getImage();
            //Press 'Verify Image', change topMessage, create new ImageData object and run method to assign location data
        } else if (view == checkButton) {
            topMessage.setText(R.string.CheckingMessage);
            ImageData playerData = new ImageData(null, null);
            displaySettings(false);
            getGPS(playerData);
        }

    }

    //Generate options Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.contents,menu);
        return super.onCreateOptionsMenu(menu);
    }

    //Assigns the correct methods for options selected in the options menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.signOut:
                signUserOut();
                return true;
            case R.id.LeaderBoard:
                generateLeaderBoard();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Creates a new temporary image file
    private File createImageFile() throws IOException {
        //Generate file name and generate new file within public storage directory
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
            createDialog("File Creation Error");
            return null;
        }

    }

    //Method used to capture an image
    private void takePicture() throws IOException {
        //Gets permission for writing to external storage
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1024);
        }
        //Creates intent for opening camera, executes and waits for activity result
        Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePic.resolveActivity(getPackageManager()) != null) {
            try {
                File photoFile = createImageFile();
                if (photoFile != null && mAuth != null) {
                    Uri outputFileUri = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".provider", createImageFile());
                    takePic.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                    startActivityForResult(takePic, REQUEST_TAKE_PHOTO);
                } else {
                    createDialog("Image Capture Error");
                }
            } catch (IOException ex) {
                createDialog("File Error, Cannot Access File");
            }
        }
    }

    //Method for when an image is successfully captured
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //On successful capture, change display settings to generate spinner
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            displaySettings(false);
            topMessage.setText(R.string.UploadProgress);
            //Upload image to Firebase Cloud Storage
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
                    //On success, create new ImageData object and start to assign data
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

    //Method for uploading image data to Database
    private void manageUpload(final ImageData imageData, final String mAuthID) {
        //Retreieve the value of maxIndex from the database, upload image with root maxIndex + 1
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
                    //On completion, add to players list of completed images
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


    //Method used to retrieve an image
    private void getImage() {
        displaySettings(false);
        topMessage.setText(R.string.ImageMessage);
        //Retrieve database value of maxIndex and pass to imageProcessing()
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

    //Image selection and displaying
    private void imageProcessing(int maxIndex) {
        List<Integer> availableImages = new ArrayList<>();
        //If all indexes are within either the skippedList or completedList. Reset skippedList to empty
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
        //Generate a list of the indexes of images that do not appear within the completedList or skippedList
        for(int i = 1; i < maxIndex + 1; i++) {
            if (!playerCompletedImages.contains(i) && !playerSkippedImages.contains(i)) {
                availableImages.add(i);
            }
        }
        //Generate a random index for the position of the image in availableImages list
        Random rand = new Random();
        int value = rand.nextInt(availableImages.size());
        currentImageIndex = availableImages.get(value);
        //Retrieve image from Cloud Storage and display using Glide
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
                //Retrieve imageData for the image from the database and assign to currentImageData
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
                    nameDisplay(currentImageData.getUserID());
                    displaySettings(true);
                }
                getImageButton.setText(R.string.NextText);
                checkButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                createDialog("Database Error, Image Retrieval Cancelled");
                displaySettings(true);
            }
        });

    }

    //Set ImageData's GPS coordinates to be those of the current location of the device
    private void getGPS(final ImageData imageData){
        imageData.setLatitude(currentLocation.getLatitude());
        imageData.setLongitude(currentLocation.getLongitude());
        scanWifiNetworks(imageData);
    }

    //Request location every 0.5-1 seconds, updating currentLocation when coordinates are successfully received
    private void createLocationRequest(){
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(500);
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

    //Method for WiFi network scans
    private void scanWifiNetworks(final ImageData imageData){
        if(wifi.getWifiState() == WifiManager.WIFI_STATE_ENABLED){
            connections.clear();
            IntentFilter filter = new IntentFilter();
            filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    //Following a successful scan, add SSID and BSSID pairs to current imageData object
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


    //Check Location of player when verifying image
    public void checkData(ImageData playerData){
        int wifiCount = 0;
        //If the player has no WiFi network data, generate new data
        if(playerData.getWifiNetworks() == null){
            getGPS(playerData);
        }
        //Create a statistic for the number of matches between the player and images's network list
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

        //Check GPS coordinates and Network Size conditions
        if(coordCheck(playerData.getLatitude(),currentImageData.getLatitude()) && coordCheck(playerData.getLongitude(),currentImageData.getLongitude())) {
            if ((wifiCount >= currentImageData.getWifiNetworks().size() / 2 || wifiCount >= playerData.getWifiNetworks().size() / 2)) {
                //On success, add image index to players completeList and allocate points to submitter and verifier
                addToCompleteList(mAuth.getUid(), currentImageIndex, false);
                pointsAllocation(currentImageData.getUserID(), 1);
                pointsAllocation(mAuth.getUid(), 2);
                //Update Image's correct verification count
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
                //If check fails, run method again, up to 2 times
                if (wifiScanCount < 3) {
                    wifiScanCount++;
                    getGPS(playerData);
                } else {
                    //If extra scans also fail check, player is not in the create location due to wifiNetworks
                    wifiScanCount = 1;
                    createDialog("Incorrect, Not Enough Matching Wifi Addresses. Location cannot be Verified");
                    displaySettings(true);
                    nameDisplay(currentImageData.getUserID());
                }
            }
        }else{
            //For the GPS coordinates failing to pass the check
            createDialog("Incorrect, More than 22m away from Location of Picture");
            displaySettings(true);
            nameDisplay(currentImageData.getUserID());
        }
    }

    //Tests whether a player is within 22m of an image's registered location
    private boolean coordCheck(double playerCoord, double imageCoord){
        if( (playerCoord > 0.0 && imageCoord > 0.0) || (playerCoord < 0.0 && imageCoord < 0.0) ){
            double diff = playerCoord*1000 - imageCoord*1000;
            return (Math.round(Math.abs(diff*10)) <= 2);
        }else{
            double diff = playerCoord*1000 + imageCoord*1000;
            return (Math.round(Math.abs(diff*10)) <= 2);
        }
    }


    //Adds image's index to players completeList, both locally and on the database
    public void addToCompleteList(final String userID, final int imageIndex, boolean processEnd){
        playerCompletedImages.add(imageIndex);
        db.child("users").child(userID).child("completedImages").setValue(playerCompletedImages);
        if(processEnd){
            createDialog("Upload Finished");
            displaySettings(true);
            nameDisplay(currentImageData.getUserID());
        }
    }

    //Updates points values on the database for the respective player and value
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

    //Updates the player's skippedList on the database
    public void uploadSkippedList(){
        if(mAuth.getUid() != null) {
            db.child("users").child(mAuth.getUid()).child("skippedImages").setValue(playerSkippedImages);
        }else{
            createDialog("Database Error, Skipped Images not uploaded");
        }
    }

    //Signs a player out, taking them back to the Login Screen
    private void signUserOut(){
        mAuth.signOut();
        Intent exitIntent = new Intent(this,LogInActivity.class);
        startActivity(exitIntent);
    }

    //Disables buttons and enables the spinner or vice versa, depending on the boolean setting
    private void displaySettings(boolean setting){
        camButton.setEnabled(setting);
        getImageButton.setEnabled(setting);
        checkButton.setEnabled(setting);

        if(!setting){
            mImageView.setVisibility(View.INVISIBLE);
            spinner.setVisibility(View.VISIBLE);
        }else{
            spinner.setVisibility(View.INVISIBLE);
            mImageView.setVisibility(View.VISIBLE);
        }
    }

    //Display method for the message at the top of the main screen
    private void nameDisplay(String userID){
        if(userID == null) {
            if (mAuth.getUid() != null) {
                //If no image is currently displayed on screen, then set message to be the players username and points value
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
            //If an image is being displayed, then set message to show the username of the uploader of the image
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

    //Create a popup dialog which displays 'message'
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

    //Generates Leaderboard
    private void generateLeaderBoard(){
        //Retrieve list of all players from database
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
        userRef.orderByChild("points").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<Map<String,Map<String,Object>>> map = new GenericTypeIndicator<Map<String,Map<String,Object>>>() {};
                Map<String,Map<String,Object>> hp = dataSnapshot.getValue(map);
                //Generate a Map of PlayerID and points pairs. Ordered by points value
                if(hp != null){
                    Map<String,Long> pointsMap = new HashMap<String, Long>() {};

                    for(String key : hp.keySet()){
                        Map<String,Object> entry = hp.get(key);
                        Long value = (Long) entry.get("points");
                        pointsMap.put(key,value);
                    }
                    Set<Map.Entry<String,Long>> entries = pointsMap.entrySet();
                    List<Map.Entry<String,Long>> entriesList = new LinkedList<>(entries);
                    Collections.sort(entriesList, new Comparator<Map.Entry<String, Long>>() {
                        @Override
                        public int compare(Map.Entry<String, Long> o1, Map.Entry<String, Long> o2) {
                            return o1.getValue().compareTo(o2.getValue());
                        }
                    });
                    Map<String,Long> sortedPointsMap = new LinkedHashMap<>();
                    ArrayList<String> userIDList = new ArrayList<>();
                    for(Map.Entry<String,Long> entry : entriesList){
                        sortedPointsMap.put(entry.getKey(),entry.getValue());
                        userIDList.add(entry.getKey());
                    }
                    //Find the position of the player in the map and generate the leaderboard depending on ppints values
                    int userListPosition = userIDList.indexOf(mAuth.getUid());
                    String message = "You have " + sortedPointsMap.get(userIDList.get(userListPosition)) + " points";
                    //Generate position of player overall
                    message = message + "\nYou are in position " + (userIDList.size() - userListPosition) + " out of " + userIDList.size();
                    //Message depends on if player is top, has equal points as the player above them or otherwise
                    if((userIDList.size() - userListPosition) == 1){
                        message = message + "\nThere are no players ahead of you";
                    }else if((sortedPointsMap.get(userIDList.get(userListPosition + 1)) - sortedPointsMap.get(userIDList.get(userListPosition))) == 0){
                        message = message + "\nYou have the same amount of points as the player above you";
                    }else{
                        message = message + "\nThe player above you is ahead by " + (sortedPointsMap.get(userIDList.get(userListPosition + 1)) - sortedPointsMap.get(userIDList.get(userListPosition))) + " points";
                    }
                    //Likewise for players below.
                    if((userIDList.size() - userListPosition) == userIDList.size()){
                        message = message + "\nThere are no players below you";
                    }else if((sortedPointsMap.get(userIDList.get(userListPosition)) - sortedPointsMap.get(userIDList.get(userListPosition - 1))) == 0){
                        message = message + "\nYou have the same number of points as the player below you";
                    } else{
                        message = message + "\nThe player below you is behind by " + (sortedPointsMap.get(userIDList.get(userListPosition)) - sortedPointsMap.get(userIDList.get(userListPosition - 1)))  + " points";
                    }
                    //Display leaderboard to player
                    createDialog(message);
                }else{
                    createDialog("Cannot produce Leaderboard");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                createDialog("Cannot produce Leaderboard");
            }
        });
    }
}