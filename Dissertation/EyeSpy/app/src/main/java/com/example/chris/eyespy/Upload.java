package com.example.chris.eyespy;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

/**
 * Created by Chris on 23/11/2017.
 */

@IgnoreExtraProperties
class Upload implements Serializable{

    private String imageURL;
    private String userUploadID;
    private int correctCheckCount;
    private int skipCount;
    private double latitude;
    private double longitude;

    public Upload(String imageURL, String userUploadID){
        this.imageURL = imageURL;
        this.userUploadID = userUploadID;
        this.correctCheckCount = 0;
        this.skipCount = 0;
    }

    public String getImageURL(){
        return this.imageURL;
    }

    public String getUserUploadID(){
        return this.userUploadID;
    }

    public int getCorrectCheckCount(){
        return this.correctCheckCount;
    }

    public int getSkipCount(){
        return this.skipCount;
    }

    public double getLatitude(){
        return this.latitude;
    }

    public double getLongitude(){
        return this.longitude;
    }

    public void setLatitude(double latitude){
        this.latitude = latitude;
    }

    public void setLongitude(double longitude){
        this.longitude = longitude;
    }

}
