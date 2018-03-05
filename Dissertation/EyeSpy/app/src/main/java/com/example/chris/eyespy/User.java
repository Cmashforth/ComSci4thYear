package com.example.chris.eyespy;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Chris on 23/11/2017.
 */

@IgnoreExtraProperties
class User implements Serializable{

    private String email;
    private int points;
    private List completedImages;
    private List skippedImages;
    private String username;


    User(String email,String username){
        this.email = email;
        this.points = 0;
        this.completedImages = new ArrayList<>();
        this.skippedImages = new ArrayList<>();
        this.username = username;

    }

    public String getEmail(){
        return this.email;
    }

    public int getPoints(){
        return this.points;
    }

    public List getCompletedImages() {return this.completedImages;}

    public List getSkippedImages() {return this.skippedImages;}

    public String getUsername() {return this.username;}

}
