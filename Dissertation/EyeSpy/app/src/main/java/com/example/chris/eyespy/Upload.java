package com.example.chris.eyespy;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

/**
 * Created by Chris on 23/11/2017.
 */

@IgnoreExtraProperties
public class Upload implements Serializable{

    private String imageURL;

    public Upload(String imageURL){
        this.imageURL = imageURL;
    }

    public String getImageURL(){
        return this.imageURL;
    }
}
