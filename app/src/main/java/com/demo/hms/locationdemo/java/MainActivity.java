package com.demo.hms.locationdemo.java;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.hms.locationdemo.R;
import com.huawei.hms.common.ResolvableApiException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, GPS.OnGPSEventListener{

    private static final int REQUEST_CODE=1;
    private static final String TAG="MainActivity";

    private double lat=0.0;
    private double lon=0.0;
    private GPS gps;
    TextView tv1;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!checkLocationPermissions())
            requestLocationPermissions();
        else setupGPS();
        Button share=findViewById(R.id.start);
        share.setOnClickListener(this);
        //tv1=findViewById(R.id.tv1);
    }

    private boolean checkLocationPermissions() {
        int cl=checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
        int fl=checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        int bl= Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q?
            checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION):
            PackageManager.PERMISSION_GRANTED;
        return cl==PackageManager.PERMISSION_GRANTED&&fl==PackageManager.PERMISSION_GRANTED&&bl==PackageManager.PERMISSION_GRANTED;

    }

    private void requestLocationPermissions() {
        String afl= Manifest.permission.ACCESS_FINE_LOCATION;
        String acl=Manifest.permission.ACCESS_COARSE_LOCATION;
        String[] permissions;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q)
            permissions=new String[]{afl, acl, Manifest.permission.ACCESS_BACKGROUND_LOCATION};
        else permissions=new String[]{afl, acl};
        requestPermissions(permissions, REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (checkLocationPermissions()){
            setupGPS();
        }
        else tv1.setText("You don't have GPS permissions");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(checkLocationPermissions()){
            if(gps!=null){
                if(!gps.isStarted()){
                    gps.startLocationRequest();
                }
            } else setupGPS();
        }
    }

    private void shareLocation() {
        Uri uri=Uri.parse("geo:"+lat+","+lon);
        Intent intent =new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void setupGPS() {
        gps= new GPS(this);
        gps.startLocationRequest();

    }

    @Override
    public void onResolutionRequired(Exception e) {
        tv1.setText(e.toString());
        try{
            ResolvableApiException rae =  (ResolvableApiException) e;
            rae.startResolutionForResult(this, 0);

        }
        catch (IntentSender.SendIntentException sie) {
            Log.e(TAG, "PendingIntent unable to execute request.");
        }
    }

    @Override
    public void onLocationUpdate(Double lat, Double lon) {
        String string="Latitude: "+lat+"\t Longitude: "+lon;
        tv1.setText(string);
        this.lat=lat;
        this.lon=lon;
    }

    @Override
    public void onClick(View v) {
        if(checkLocationPermissions())
            if(gps!=null&&gps.isStarted()) shareLocation();
            else setupGPS();
        else requestLocationPermissions();
    }
}
