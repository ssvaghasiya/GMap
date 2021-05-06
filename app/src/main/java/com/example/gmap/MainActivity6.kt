package com.example.gmap

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity6 : AppCompatActivity() {

    var locationManager: LocationManager? = null
    var txtLat: TextView? = null
    var REQUEST_PERMISSIONS_REQUEST_CODE = 1001
    private var locationListener: LocationListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main6)
        txtLat = findViewById<TextView>(R.id.textview2);
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (location != null) {
                    val userLocation =
                        LatLng(location.latitude, location.longitude)
                    txtLat = findViewById<TextView>(R.id.textview2)
                    txtLat?.setText("Latitude:" + userLocation.latitude + ", Longitude:" + userLocation.longitude);
                }
            }

            override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}
            override fun onProviderEnabled(s: String) {}
            override fun onProviderDisabled(s: String) {}
        }
        locationManager =
            this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
            return
        }
        getCurrentLocation()
    }


    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    }

    fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            1
        )
    }

    //Check request code
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (grantResults.isNotEmpty()) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                getCurrentLocation()
            } else {

            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2) {
            if (!locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                buildAlertMessageNoGps();
            } else {
                getCurrentLocation()
            }
        }
    }

    private fun buildAlertMessageNoGps() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes",
                DialogInterface.OnClickListener { dialog, id ->
                    startActivityForResult(
                        Intent(
                            Settings.ACTION_LOCATION_SOURCE_SETTINGS
                        ), 2
                    )
                })
            .setNegativeButton("No",
                DialogInterface.OnClickListener { dialog, id ->
                    dialog.cancel()
                    finish()
                })
        val alert: AlertDialog = builder.create()
        alert.show()
    }

    private fun getCurrentLocation() {
        try {
            if (checkPermission()) {
                locationManager!!.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    2,
                    2f,
                    locationListener!!
                )
                locationManager!!.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    2,
                    2f,
                    locationListener as LocationListener
                )
                val lastLocation =
                    locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (lastLocation != null) {
                    val lastUserLocation =
                        LatLng(lastLocation.latitude, lastLocation.longitude)
                    txtLat = findViewById<TextView>(R.id.textview2)
                    txtLat?.setText("Latitude:" + lastUserLocation.latitude + ", Longitude:" + lastUserLocation.longitude);
                }
            } else {
                requestPermission()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}