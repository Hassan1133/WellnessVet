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
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.wellness.vet.app.R
import com.wellness.vet.app.activities.common.MapsActivity
import com.wellness.vet.app.databinding.FragmentUserFindDoctorBinding
import com.wellness.vet.app.main_utils.AppConstants.Companion.DOCTOR_REF
import com.wellness.vet.app.main_utils.DistanceCalculator
import com.wellness.vet.app.main_utils.LocationPermissionUtils
import com.wellness.vet.app.models.DoctorDetailProfileModel


class UserFindDoctorFragment : Fragment(), OnMapReadyCallback, View.OnClickListener {

    private lateinit var binding: FragmentUserFindDoctorBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var permissionUtils: LocationPermissionUtils
    private var nearestClinicLatitude: Double = 0.0
    private var nearestClinicLongitude: Double = 0.0
    private var userLatitude: Double = 0.0
    private var userLongitude: Double = 0.0
    private var nearestClinicLocation: String = ""
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var doctorRef: DatabaseReference
    private lateinit var doctorsList: MutableList<DoctorDetailProfileModel>
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

    }

    private fun getDoctorsData() {
        doctorRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                doctorsList.clear()

                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.getChildren()) {
                        val doctorDetailProfileModel: DoctorDetailProfileModel? =
                            dataSnapshot.child("Profile")
                                .getValue(DoctorDetailProfileModel::class.java)

                        doctorsList.add(doctorDetailProfileModel!!)
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
                    "Your Current Location" -> {
                        nearestClinicLocation = ""
                        nearestClinicLatitude = 0.0
                        nearestClinicLongitude = 0.0
                        userLatitude = 0.0
                        userLongitude = 0.0
                        googleMap.clear()

                        getLastLocation() // get Last Location

                    }

                    "Choose on Map" -> {
                        nearestClinicLocation = ""
                        nearestClinicLatitude = 0.0
                        nearestClinicLongitude = 0.0
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
            nearestClinicLatitude = it.clinicLatitude
            nearestClinicLongitude = it.clinicLongitude
            // Here you can handle the nearest doctor's clinic location
            Toast.makeText(
                requireActivity(),
                "Nearest doctor's clinic: ${it.name}",
                Toast.LENGTH_SHORT
            ).show()
        }
}

}
