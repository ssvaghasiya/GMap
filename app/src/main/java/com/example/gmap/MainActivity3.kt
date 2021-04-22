package com.example.gmap

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat


class MainActivity3 : AppCompatActivity(), LocationListener {

    var locationManager: LocationManager? = null
    var locationListener: LocationListener? = null
    var context: Context? = null
    var txtLat: TextView? = null
    var lat: String? = null
    var provider: String? = null
    var latitude: String? = null
    var longitude: kotlin.String? = null
    var gps_enabled = false
    var network_enabled: kotlin.Boolean = false
    var REQUEST_PERMISSIONS_REQUEST_CODE = 1001


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)
        txtLat = findViewById<TextView>(R.id.textview1);
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        checkPermission()
    }

    fun checkPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            requestPermissions()
            return
        } else {
            locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0F, this);
            locationManager!!.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                0L,
                0F,
                this
            );
        }
    }


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
            REQUEST_PERMISSIONS_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkPermission()
                }
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        txtLat = findViewById<TextView>(R.id.textview1)
        txtLat?.setText("Latitude:" + location.latitude + ", Longitude:" + location.longitude);

    }


    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        Log.d("Latitude", "status")
    }

    override fun onProviderEnabled(provider: String) {
        Log.d("Latitude", "disable")
    }

    override fun onProviderDisabled(provider: String) {
        Log.d("Latitude", "enable")
    }
}