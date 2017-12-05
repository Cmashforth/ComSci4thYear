package com.example.chris.eyespy;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Chris on 23/11/2017.
 */

@IgnoreExtraProperties
public class Upload {

    public String value;

    public Upload(String value){
        this.value = value;
    }
}
