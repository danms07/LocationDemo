package com.demo.hms.locationdemo.kotlin

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.demo.hms.locationdemo.R
import com.huawei.hms.common.ResolvableApiException
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), GPS.OnGPSEventListener, View.OnClickListener {

    companion object{
        const val REQUEST_CODE=1
        const val TAG="MainActivity"
    }

    private var gps: GPS?=null

    private var lat=0.0
    private var lon=0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!checkLocationPermissions())
            requestLocationPermissions()
        else setupGPS()
        share.setOnClickListener(this)
    }

    private fun checkLocationPermissions():Boolean {
        val cl=checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        val fl=checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        val bl= if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
                checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
            else{
                PackageManager.PERMISSION_GRANTED
            }

        return cl==PackageManager.PERMISSION_GRANTED&&fl==PackageManager.PERMISSION_GRANTED&&bl==PackageManager.PERMISSION_GRANTED

    }

    override fun onResume() {
        super.onResume()
        if (checkLocationPermissions()){
            if(gps==null) setupGPS()
            else gps?.let {
                if(!it.isStarted){
                    it.startLocationRequest()
                }
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        gps?.removeLocationUpdates()
    }

    private fun requestLocationPermissions() {
        val afl:String=Manifest.permission.ACCESS_FINE_LOCATION
        val acl:String=Manifest.permission.ACCESS_COARSE_LOCATION
        val permissions:Array<String>
        permissions = if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
            arrayOf(afl, acl, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } else arrayOf(afl, acl)
        requestPermissions(permissions, REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (checkLocationPermissions()){
            setupGPS()
        }
        else tv1.text = "You don't have GPS permissions"

    }

    private fun setupGPS() {
        gps= GPS(this)
        gps?.startLocationRequest()

    }

    override fun onResolutionRequired(e: Exception) {
        tv1.text = e.toString()
        try{
            val rae: ResolvableApiException = e as ResolvableApiException
            rae.startResolutionForResult(this, 0)

        }
        catch (sie: IntentSender.SendIntentException) {
            Log.e(TAG, "PendingIntent unable to execute request.")
        }
    }


    override fun onLocationUpdate(lat: Double, lon: Double) {
        val string="Latitude: $lat \t Longitude:$lon"
        tv1.text = string
        this.lat =lat
        this.lon =lon
    }

    override fun onClick(v: View?) {
        if(checkLocationPermissions())
            gps?.isStarted.let {
                if (it==true) shareLocation()
                else setupGPS()
            }
        else requestLocationPermissions()
    }

    private fun shareLocation(){
        val uri = Uri.parse("geo:$lat,$lon")
        val intent=Intent()
        intent.action=(Intent.ACTION_VIEW)
        intent.data = uri
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }
}
