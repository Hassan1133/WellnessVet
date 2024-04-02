package com.wellness.vet.app.activities.common

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.wellness.vet.app.R
import com.wellness.vet.app.databinding.ActivityMapsBinding
import com.wellness.vet.app.main_utils.NetworkManager
import java.io.IOException
import java.util.Locale

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var geocoder: Geocoder

    private lateinit var selectedAddress: String

    private lateinit var mapFragment: SupportMapFragment

    private lateinit var networkManager: NetworkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()

    }

    private fun init() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        networkManager = NetworkManager(this@MapsActivity)

        searchLocation()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        //        get location by clicking on marker

        googleMap.setOnMarkerClickListener { marker ->
            getAddress(marker.position.latitude, marker.position.longitude)
            false
        }

        //        get location by clicking on any where on map
        googleMap.setOnMapClickListener { latLng -> getAddress(latLng.latitude, latLng.longitude) }

        //        get location current location and customization of current location button

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        googleMap.isMyLocationEnabled = true
        val locationButton = (mapFragment.view?.findViewById<View>("1".toInt())
            ?.parent as View).findViewById<View>("2".toInt())
        val layoutParams = locationButton.layoutParams as RelativeLayout.LayoutParams
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        layoutParams.setMargins(0, 0, 0, 20)
    }

    private fun searchLocation() {
        binding.mapsSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                mMap.clear()
                if (networkManager.isNetworkAvailable()) {
                    val location: String = binding.mapsSearchView.query.toString()
                    var addressList: List<Address>? = null
                    if (location.isNotEmpty()) {
                        geocoder = Geocoder(this@MapsActivity, Locale.getDefault())
                        try {
                            addressList = geocoder.getFromLocationName(location, 1)
                        } catch (e: IOException) {
                            Toast.makeText(this@MapsActivity, e.message, Toast.LENGTH_SHORT).show()
                        }
                        if (addressList!!.isNotEmpty()) {
                            val address = addressList[0]
                            val latLng = LatLng(address.latitude, address.longitude)
                            mMap.addMarker(MarkerOptions().position(latLng).title(location))
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
                        } else {
                            Toast.makeText(
                                this@MapsActivity,
                                "Please select a valid location",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        this@MapsActivity,
                        "Check your internet connection",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })
    }

    private fun getAddress(latitude: Double, longitude: Double) {
        geocoder = Geocoder(this@MapsActivity, Locale.getDefault())
        var addresses: List<Address>? = null
        if (latitude != 0.0 && longitude != 0.0) {
            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1)
            } catch (e: IOException) {
                Toast.makeText(this@MapsActivity, e.message, Toast.LENGTH_SHORT).show()
            }
            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    selectedAddress = addresses[0].getAddressLine(0)
                    if (selectedAddress.isNotEmpty()) {
                        val returnIntent = Intent()
                        returnIntent.putExtra("latitude", latitude)
                        returnIntent.putExtra("longitude", longitude)
                        returnIntent.putExtra("address", selectedAddress)
                        setResult(RESULT_OK, returnIntent)
                        finish()
                    } else {
                        Toast.makeText(this, "Please choose a valid location", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(this, "Please choose a valid location", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(this, "Please choose a valid location", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please choose a valid location", Toast.LENGTH_SHORT).show()
        }
    }

}