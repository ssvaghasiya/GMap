package com.example.gmap

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import java.io.IOException
import java.util.*


class MainActivity4 : AppCompatActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null
    var myAddress = "No identified address"
    var myLatitude = 0.0
    var myLongitude = 0.0
    var marker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main4)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        val apiKey = getString(R.string.map_key)

        /**
         * Initialize Places. For simplicity, the API key is hard-coded. In a production
         * environment we recommend using a secure mechanism to manage API keys.
         */
        /**
         * Initialize Places. For simplicity, the API key is hard-coded. In a production
         * environment we recommend using a secure mechanism to manage API keys.
         */
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }


        // Create a new Places client instance.
        val placesClient: PlacesClient = Places.createClient(this)

        // Initialize the AutocompleteSupportFragment.

        // Initialize the AutocompleteSupportFragment.
        val autocompleteFragment: AutocompleteSupportFragment? =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment?
//        autocompleteFragment!!.setLocationBias(RectangularBounds.newInstance(LatLng(0.313611,32.581111),LatLng(0.313611,32.581111)))
//        autocompleteFragment!!.setCountries("UG")
        autocompleteFragment!!.setPlaceFields(
            Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG
            )
        )

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // TODO: Get info about the selected place.
                Log.e(
                    "TAG",
                    "Place: " + place.getName()
                        .toString() + ", " + place.getId() + ", " + place.address
                )
                myLatitude = place.latLng!!.latitude
                myLongitude = place.latLng!!.longitude

                mMap!!.clear()
                if (marker != null) {
                    marker!!.remove();
                }
                marker = mMap!!.addMarker(
                    MarkerOptions().position(place.latLng!!).title("Current Location")
                )
                mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(place.latLng, 17f))
            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                Log.e("TAG", "An error occurred: $status")
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap;
        getAddress()
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (location != null) {
                    mMap!!.clear()
                    val userLocation =
                        LatLng(location.getLatitude(), location.longitude)
                    if (marker != null) {
                        marker!!.remove();
                    }
                    marker = mMap!!.addMarker(
                        MarkerOptions().position(userLocation).title("Current Location")
                    )
                    mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 17f))
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


        //Check permission
        getCurrentLocation()

    }

    private fun getAddress() {
        mMap!!.setOnMapLongClickListener { latLng ->
            mMap!!.clear()
            //Get full address
            val geocoder: Geocoder
            val addresses: List<Address>
            geocoder = Geocoder(this@MainActivity4, Locale.getDefault())
            try {
                addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                val fullAddress: String = addresses[0].getAddressLine(0)
                myAddress = fullAddress
            } catch (e: IOException) {
                e.printStackTrace()
            }
            Toast.makeText(this@MainActivity4, myAddress, Toast.LENGTH_LONG).show()
            myLatitude = latLng.latitude
            myLongitude = latLng.longitude
            if (marker != null) {
                marker!!.remove();
            }
            marker = mMap!!.addMarker(MarkerOptions().position(latLng).title(myAddress))
        }
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

    private fun getCurrentLocation() {
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
            mMap!!.clear()
            mMap!!.isMyLocationEnabled = true
            mMap!!.uiSettings.isMyLocationButtonEnabled = true
            val lastLocation =
                locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastLocation != null) {
                val lastUserLocation =
                    LatLng(lastLocation.latitude, lastLocation.longitude)
                mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 17f))
                if (marker != null) {
                    marker!!.remove();
                }
                marker = mMap!!.addMarker(
                    MarkerOptions().position(lastUserLocation).title("Current Location")
                )
            }
        } else {
            requestPermission()
        }
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

    fun onMapSearch(view: View?) {
        val locationSearch: EditText = findViewById<View>(R.id.editText) as EditText
        val location: String = locationSearch.getText().toString()
        var addressList: List<Address>? = null
        if (location != null || location != "") {
            val geocoder = Geocoder(this)
            try {
                addressList = geocoder.getFromLocationName(location, 1)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val address = addressList!![0]
            val latLng = LatLng(address.latitude, address.longitude)
            mMap!!.clear()
            mMap!!.addMarker(MarkerOptions().position(latLng).title("Marker"))
            mMap!!.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        }
    }
}