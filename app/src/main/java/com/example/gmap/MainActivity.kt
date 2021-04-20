package com.example.gmap

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task

class MainActivity : FragmentActivity(), OnMapReadyCallback {

    var map: GoogleMap? = null
    var currentLocation: Location? = null
    var fusedLocationProviderClient: FusedLocationProviderClient? = null
    var REQUEST_CODE: Int? = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fetchLastLocation()
    }

    private fun fetchLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            var permissions: Array<String> = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

            ActivityCompat.requestPermissions(
                this,
                permissions,
                REQUEST_CODE!!
            )
            return
        }
        var task: Task<Location> = fusedLocationProviderClient?.lastLocation!!
        task.addOnSuccessListener { location ->
            if (location != null) {
                currentLocation = location
                Toast.makeText(
                    this,
                    currentLocation?.latitude.toString() + " " + currentLocation?.longitude.toString(),
                    Toast.LENGTH_SHORT
                ).show()
                var mapFragment: SupportMapFragment =
                    supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
                mapFragment.getMapAsync(this)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap

        var latlng: LatLng = LatLng(currentLocation?.latitude!!, currentLocation?.longitude!!)
        var markeroption: MarkerOptions = MarkerOptions().position(latlng).title("I am Here")
        map?.animateCamera(CameraUpdateFactory.newLatLng(latlng))
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 5F))
        map?.addMarker(markeroption)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fetchLastLocation()
                }
            }
        }

    }
}