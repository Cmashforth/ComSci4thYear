package com.example.chris.eyespy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WifiActivity extends AppCompatActivity {

    WifiManager wifi;
    WifiReceiver receiver;
    Button wifiButton;
    TextView list;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);

        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiButton = findViewById(R.id.Button);
        list = findViewById(R.id.list);
    }

    public void onClick(View view){
        if(view == wifiButton){
            receiver = new WifiReceiver();
            registerReceiver(receiver,new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            wifi.startScan();
        }

    }

    class WifiReceiver extends BroadcastReceiver{
        public void onReceive(Context c, Intent intent){

            ArrayList<String> connections = new ArrayList<>();
            List<ScanResult> wifiList = wifi.getScanResults();
            for(int i = 0; i < wifiList.size(); i++){
                connections.add(wifiList.get(i).SSID + " "+ wifiList.get(i).BSSID);
            }
            String displayString = "";
            for(int i = 0;i < connections.size();i++){
                displayString = displayString + connections.get(i) + "\n";
            }
            list.setText(displayString);
        }
    }



}
