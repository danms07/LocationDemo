package com.demo.hms.locationdemo.kotlin

import android.content.Context
import android.os.Looper
import android.util.Log
import com.huawei.hms.common.ApiException
import com.huawei.hms.location.*


class GPS(private val context: Context) : LocationCallback() {

    companion object{
        const val TAG = "GPS Tracker"
    }
    private var _isStarted:Boolean=false
    val isStarted: Boolean
    get() {return _isStarted}

    var listener:OnGPSEventListener?=null



    init {
        if(context is OnGPSEventListener){
            this.listener=context
        }
    }

    private val fusedLocationProviderClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun startLocationRequest(interval :Long=10000) {
        val settingsClient: SettingsClient = LocationServices.getSettingsClient(context)
        val mLocationRequest = LocationRequest()
        // set the interval for location updates, in milliseconds.
        mLocationRequest.interval = interval
        // set the priority of the request
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest)
        val locationSettingsRequest = builder.build()
        // check devices settings before request location updates.
        settingsClient.checkLocationSettings(locationSettingsRequest)
            .addOnSuccessListener {
                Log.i(TAG, "check location settings success")
                //request location updates
                fusedLocationProviderClient.requestLocationUpdates(
                    mLocationRequest,
                    this,
                    Looper.getMainLooper()
                ).addOnSuccessListener {
                    Log.i(
                        TAG,
                        "requestLocationUpdatesWithCallback onSuccess"
                    )
                    _isStarted=true
                }
                    .addOnFailureListener { e ->
                        Log.e(
                            TAG,
                            "requestLocationUpdatesWithCallback onFailure:" + e.message
                        )
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "checkLocationSetting onFailure:" + e.message)
                val apiException: ApiException = e as ApiException
                when (apiException.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        Log.e(TAG, "Resolution required")
                        listener?.onResolutionRequired(e)
                    }
                }
            }
    }

    fun removeLocationUpdates() {
        if(_isStarted) try {
            fusedLocationProviderClient.removeLocationUpdates(this)
                .addOnSuccessListener {
                    Log.i(
                        TAG,
                        "removeLocationUpdatesWithCallback onSuccess"
                    )
                    _isStarted=false
                }
                .addOnFailureListener { e ->
                    Log.e(
                        TAG,
                        "removeLocationUpdatesWithCallback onFailure:" + e.message
                    )
                }
        } catch (e: Exception) {
            Log.e(TAG, "removeLocationUpdatesWithCallback exception:" + e.message)
        }
    }

    override fun onLocationResult(locationResult: LocationResult?) {

        if (locationResult != null) {
            val lastLocation=locationResult.lastLocation
            listener?.onLocationUpdate(lastLocation.latitude,lastLocation.longitude)
        }
    }

    interface OnGPSEventListener {
        fun onResolutionRequired(e: Exception)
        fun onLocationUpdate(lat:Double, lon:Double)
    }
}