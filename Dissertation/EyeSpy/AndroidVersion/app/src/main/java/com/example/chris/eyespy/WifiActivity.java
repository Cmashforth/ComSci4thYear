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
    ListView list;
    TextView textStatus;
    Button scan;
    int size = 0;
    List<ScanResult> results;

    String ITEM_KEY = "key";
    ArrayList<HashMap<String,String>> arrayList = new ArrayList<HashMap<String, String>>();
    SimpleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);

        textStatus = (TextView) findViewById(R.id.textStatus);
        scan = (Button) findViewById(R.id.scanWifi);
        list = (ListView)findViewById(R.id.wifiList);

        wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(wifi.isWifiEnabled() == false){
            Toast.makeText(getApplicationContext(), "wifi is disabled, making it enabled", Toast.LENGTH_LONG).show();
            wifi.setWifiEnabled(true);
        }
        this.adapter = new SimpleAdapter(WifiActivity.this, arrayList, R.layout.row, new String[] { ITEM_KEY }, new int[] { R.id.list_value });
        list.setAdapter(this.adapter);

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                results = wifi.getScanResults();
                size = results.size();
            }
        }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    public void getWifi(View view){
        arrayList.clear();
        wifi.startScan();

        Toast.makeText(this, "Scanning...." + size, Toast.LENGTH_SHORT).show();
        try{
            size = size - 1;
            while(size >= 0){
                HashMap<String,String> item = new HashMap<String,String>();
                item.put(ITEM_KEY, results.get(size).SSID + " " + results.get(size).BSSID);
                arrayList.add(item);
                size--;
                adapter.notifyDataSetChanged();
            }
        } catch (Exception e){}

    }
}
