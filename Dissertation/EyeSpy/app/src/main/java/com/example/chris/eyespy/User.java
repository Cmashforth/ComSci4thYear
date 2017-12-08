package com.example.chris.eyespy;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chris on 23/11/2017.
 */

@IgnoreExtraProperties
class User implements Serializable{

    private String email;
    private int points;
    private List uploads;


    public User(String email){
        this.email = email;
        this.points = 0;
        this.uploads = new ArrayList<>();

    }

    public String getEmail(){
        return this.email;
    }

    public int getPoints(){
        return this.points;
    }

    public List getUploads() {return this.uploads;}

}
