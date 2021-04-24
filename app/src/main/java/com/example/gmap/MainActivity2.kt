@file:Suppress("DEPRECATION")

package com.example.gmap

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*


class MainActivity2 : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener,
    LocationListener {

    private var _latitude: TextView? = null
    private var _longitude: TextView? = null
    private var _progressBar: ProgressBar? = null
    var mGoogleApiClient: GoogleApiClient? = null
    var mLastLocation: Location? = null
    var mLocationRequest: LocationRequest? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        _latitude = findViewById<TextView>(R.id.latitude)
        _longitude = findViewById<TextView>(R.id.longitude)
        _progressBar = findViewById<ProgressBar>(R.id.progressBar)

        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()

        if (mGoogleApiClient != null) {
            mGoogleApiClient!!.connect()
        } else Toast.makeText(this, "Not Connected!", Toast.LENGTH_SHORT).show()

    }

    override fun onStop() {
        mGoogleApiClient!!.disconnect()
        super.onStop()
    }

    override fun onConnected(p0: Bundle?) {
        if (!mGoogleApiClient!!.isConnected) mGoogleApiClient!!.connect()

        settingRequest()
    }

    override fun onConnectionSuspended(p0: Int) {
        Toast.makeText(this, "Connection Suspended!", Toast.LENGTH_SHORT).show()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Toast.makeText(this, "Connection Failed!", Toast.LENGTH_SHORT).show()
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, 90000)
            } catch (e: SendIntentException) {
                e.printStackTrace()
            }
        } else {
            Log.i(
                "Current Location",
                "Location services connection failed with code " + connectionResult.getErrorCode()
            )
        }
    }

    override fun onLocationChanged(location: Location) {
        mLastLocation = location
        _progressBar!!.visibility = View.INVISIBLE
        _latitude!!.text = "Latitude: " + mLastLocation!!.latitude.toString()
        _longitude!!.text = "Longitude: " + mLastLocation!!.longitude.toString()
    }

    private fun settingRequest() {
        try {
            mLocationRequest = LocationRequest()
            mLocationRequest!!.interval = 10000 // 10 seconds, in milliseconds
            mLocationRequest!!.fastestInterval = 1000 // 1 second, in milliseconds
            mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest)
            val result =
                LocationServices.SettingsApi.checkLocationSettings(
                    mGoogleApiClient,
                    builder.build()
                )
            result.setResultCallback { result ->
                val status = result.status
                val state = result.locationSettingsStates
                when (status.statusCode) {
                    LocationSettingsStatusCodes.SUCCESS ->                         // All location settings are satisfied. The client can
                        // initialize location requests here.
                        getLocation()
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->                         // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(this@MainActivity2, 1000)
                        } catch (e: SendIntentException) {
                            // Ignore the error.
                        }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    var REQUEST_CODE: Int? = 101


    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    var REQUEST_PERMISSIONS_REQUEST_CODE = 1001

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        Log.e("requestPermissions", "Requesting permission")
        // previously and checked "Never ask again".
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_PERMISSIONS_REQUEST_CODE
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation()
                }
            }
            REQUEST_PERMISSIONS_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation()
                }
            }
        }
    }


    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        val states = LocationSettingsStates.fromIntent(data)
        when (requestCode) {
            1000 -> when (resultCode) {
                Activity.RESULT_OK ->                         // All required changes were successfully made
                    getLocation()
                Activity.RESULT_CANCELED ->                         // The user was asked to change settings, but chose not to
                    Toast.makeText(this, "Location Service not Enabled", Toast.LENGTH_SHORT).show()
                else -> {
                }
            }
        }
    }

    @Suppress("DEPRECATED_IDENTITY_EQUALS")
    fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) !== PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) !== PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions()
            return
        } else {
            /*Getting the location after aquiring location service*/
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient
            )
            if (mLastLocation != null) {
                _progressBar!!.visibility = View.INVISIBLE
                _latitude!!.text = "Latitude: " + mLastLocation!!.latitude.toString()
                _longitude!!.text = "Longitude: " + mLastLocation!!.longitude.toString()
            } else {
                /*if there is no last known location. Which means the device has no data for the loction currently.
                * So we will get the current location.
                * For this we'll implement Location Listener and override onLocationChanged*/
                Log.i("Current Location", "No data for location found")
                if (!mGoogleApiClient!!.isConnected) mGoogleApiClient!!.connect()
                LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient,
                    mLocationRequest,
                    this@MainActivity2
                )
            }
        }
    }
}