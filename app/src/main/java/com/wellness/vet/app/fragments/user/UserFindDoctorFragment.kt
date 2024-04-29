package com.wellness.vet.app.fragments.user

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.wellness.vet.app.R
import com.wellness.vet.app.activities.common.MapsActivity
import com.wellness.vet.app.activities.user.ViewDoctorProfileActivity
import com.wellness.vet.app.databinding.FragmentUserFindDoctorBinding
import com.wellness.vet.app.main_utils.AppConstants.Companion.DOCTOR_REF
import com.wellness.vet.app.main_utils.AppConstants.Companion.PROFILE_REF
import com.wellness.vet.app.main_utils.DistanceCalculator
import com.wellness.vet.app.main_utils.LocationPermissionUtils
import com.wellness.vet.app.models.DoctorDetailProfileModel


class UserFindDoctorFragment : Fragment(), OnMapReadyCallback, View.OnClickListener {

    private lateinit var binding: FragmentUserFindDoctorBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var permissionUtils: LocationPermissionUtils
    private var userLatitude: Double = 0.0
    private var userLongitude: Double = 0.0
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var doctorRef: DatabaseReference
    private lateinit var doctorsList: MutableList<DoctorDetailProfileModel>
    private var doctorUId: String = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserFindDoctorBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        init() // get Last Location
        return binding.root
    }

    private fun init() {
        mapFragment =
            (getChildFragmentManager().findFragmentById(R.id.doctorMap) as SupportMapFragment?)!!
        mapFragment.getMapAsync(this)
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        doctorsList = mutableListOf()
        doctorRef = FirebaseDatabase.getInstance().getReference(DOCTOR_REF)
        permissionUtils = LocationPermissionUtils(requireActivity())

        binding.viewDoctorProfileBtn.setOnClickListener(this)

        val statusArray = resources.getStringArray(R.array.location_options)
        // Create ArrayAdapter for AutoCompleteTextView
        val adapter = ArrayAdapter(
            requireActivity(),
            android.R.layout.simple_dropdown_item_1line,
            statusArray
        )
        // Set adapter to AutoCompleteTextView
        binding.userLocation.setAdapter(adapter)

        dropDownSelection()
        permissionUtils.checkAndRequestPermissions()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap;
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.viewDoctorProfileBtn -> {
                if (doctorUId.isNotEmpty()) {
                    val intent = Intent(requireActivity(), ViewDoctorProfileActivity::class.java)
                    intent.putExtra("doctorUId", doctorUId)
                    requireActivity().startActivity(intent)
                } else {
                    Toast.makeText(requireActivity(), R.string.doctor_not_found, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun getDoctorsData() {
        doctorRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                doctorsList.clear()

                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.getChildren()) {
                        val doctorDetailProfileModel: DoctorDetailProfileModel? =
                            dataSnapshot.child(PROFILE_REF)
                                .getValue(DoctorDetailProfileModel::class.java)

                        if (doctorDetailProfileModel != null) {
                            doctorsList.add(doctorDetailProfileModel)
                        }
                    }
                    calculateDistance(doctorsList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireActivity(), error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun dropDownSelection() {
        binding.userLocation.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, _, position, _ ->
                // Get the selected item
                val selectedItem = adapterView.getItemAtPosition(position) as String

                when (selectedItem) {
                    getString(R.string.your_current_location) -> {
                        userLatitude = 0.0
                        userLongitude = 0.0
                        googleMap.clear()

                        getLastLocation() // get Last Location

                    }

                    getString(R.string.choose_on_map) -> {
                        userLatitude = 0.0
                        userLongitude = 0.0
                        googleMap.clear()
                        val intent = Intent(requireActivity(), MapsActivity::class.java)
                        mapsActivityResultLauncher.launch(intent)

                    }
                }
            }
    }

    private val mapsActivityResultLauncher = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            userLatitude = result.data!!.getDoubleExtra("latitude", 0.0)
            userLongitude = result.data!!.getDoubleExtra("longitude", 0.0)
            getDoctorsData()
        }
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationProviderClient.lastLocation.addOnSuccessListener(OnSuccessListener<Location> { location ->
            userLatitude = location.latitude
            userLongitude = location.longitude
            getDoctorsData()
        })
    }

    private fun calculateDistance(docList: List<DoctorDetailProfileModel>) {

        var nearestDoctor: DoctorDetailProfileModel? = null
        var shortestDistance = Double.MAX_VALUE

        for (doctor in docList) {

            val distance = DistanceCalculator.calculateDistance(
                userLatitude,
                userLongitude,
                doctor.clinicLatitude,
                doctor.clinicLongitude
            )
            if (distance < shortestDistance) {
                shortestDistance = distance
                nearestDoctor = doctor
            }
        }

        nearestDoctor?.let {

            doctorUId = it.id
            moveCameraToNearestDoctorLocation(it.name, it.clinicLatitude, it.clinicLongitude)

        }
    }

    private fun moveCameraToNearestDoctorLocation(
        doctorName: String, lat: Double, lng: Double
    ) {
        val latLng = LatLng(lat, lng)
        val marker = googleMap.addMarker(MarkerOptions().position(latLng).title(doctorName))
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
        assert(marker != null)
        marker!!.showInfoWindow()
    }

}
