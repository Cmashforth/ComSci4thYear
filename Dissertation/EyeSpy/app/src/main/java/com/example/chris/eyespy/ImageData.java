package com.example.chris.eyespy;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chris on 23/11/2017.
 */

@IgnoreExtraProperties
class ImageData implements Serializable{

    private String imageURL;
    private String userID;
    private int correctCheckCount;
    private int skipCount;
    private double latitude;
    private double longitude;
    private List wifiNetworks;

    ImageData(){};

    ImageData(String imageURL, String userID){
        this.imageURL = imageURL;
        this.userID = userID;
        this.correctCheckCount = 0;
        this.skipCount = 0;
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.wifiNetworks = new ArrayList<>();
    }

    public String getImageURL(){
        return this.imageURL;
    }

    String getUserID(){
        return this.userID;
    }

    public int getCorrectCheckCount(){
        return this.correctCheckCount;
    }

    public int getSkipCount(){
        return this.skipCount;
    }

    double getLatitude(){
        return this.latitude;
    }

    double getLongitude(){
        return this.longitude;
    }

    List getWifiNetworks(){
        return this.wifiNetworks;
    }

    void setLatitude(double latitude){
        this.latitude = latitude;
    }

    void setLongitude(double longitude){
        this.longitude = longitude;
    }

    void setWifiNetworks(List networkList){
        this.wifiNetworks = networkList;
    }

    void setUserID(String userID){this.userID = userID;}



}
