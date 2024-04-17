package com.wellness.vet.app.activities.doctor

//noinspection SuspiciousImport
import android.R
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.wellness.vet.app.activities.common.MapsActivity
import com.wellness.vet.app.databinding.ActivityEditDoctorProfileBinding
import com.wellness.vet.app.main_utils.AppConstants
import com.wellness.vet.app.main_utils.AppConstants.Companion.DOCTOR_REF
import com.wellness.vet.app.main_utils.AppConstants.Companion.PROFILE_REF
import com.wellness.vet.app.main_utils.AppSharedPreferences
import com.wellness.vet.app.main_utils.LoadingDialog
import com.wellness.vet.app.main_utils.LocationPermissionUtils
import com.wellness.vet.app.main_utils.NetworkManager
import java.text.SimpleDateFormat
import java.util.Calendar

class EditDoctorProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditDoctorProfileBinding
    private lateinit var appSharedPreferences: AppSharedPreferences
    private val cityList = mutableListOf<String>()
    private lateinit var doctorProfileRef: DatabaseReference
    private lateinit var networkManager: NetworkManager
    private lateinit var permissionUtils: LocationPermissionUtils
    private var selectedClinicLatitude: Double = 0.0
    private var selectedClinicLongitude: Double = 0.0;
    private var imageUri = Uri.EMPTY
    private lateinit var storageRef: StorageReference
    private lateinit var loadingDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditDoctorProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    @SuppressLint("SimpleDateFormat")
    private fun init() {
        appSharedPreferences = AppSharedPreferences(this@EditDoctorProfileActivity)
        networkManager = NetworkManager(this@EditDoctorProfileActivity)
        permissionUtils = LocationPermissionUtils(this@EditDoctorProfileActivity)
        permissionUtils.checkAndRequestPermissions()
        doctorProfileRef = FirebaseDatabase.getInstance().getReference(DOCTOR_REF).child(
            appSharedPreferences.getString("doctorUid")!!
        ).child(PROFILE_REF)
        storageRef = FirebaseStorage.getInstance().getReference()
        getProfileDataFromSharedPref()
        getCityNamesFromDb()

        binding.startTimeLayout.setEndIconOnClickListener {
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                binding.startTime.setText(SimpleDateFormat("hh:mm a").format(cal.time))
            }
            TimePickerDialog(
                this,
                timeSetListener,
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                false
            ).show()
        }


        binding.endTimeLayout.setEndIconOnClickListener() {
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                binding.endTime.setText(SimpleDateFormat("hh:mm a").format(cal.time))
            }
            TimePickerDialog(
                this,
                timeSetListener,
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                false
            ).show()
        }

        binding.clinicLocationLayout.setEndIconOnClickListener {
            val intent: Intent = Intent(this@EditDoctorProfileActivity, MapsActivity::class.java)

            if (networkManager.isNetworkAvailable()) {
                if (permissionUtils.isMapsEnabled()) {
                    if (permissionUtils.isLocationPermissionGranted()) {
                        mapsActivityResultLauncher.launch(intent)
                    } else {
                        permissionUtils.getLocationPermission()
                    }
                }
            } else {
                Toast.makeText(
                    this@EditDoctorProfileActivity,
                    "Please check your internet connection",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.updateProfileBtn.setOnClickListener {
            if (isValid()) {
                if (imageUri == Uri.EMPTY) {
                    loadingDialog = LoadingDialog.showLoadingDialog(this@EditDoctorProfileActivity)
                    updateDataToDb(
                        binding.name.text.toString(),
                        binding.city.text.toString(),
                        binding.clinicLocation.text.toString(),
                        binding.startTime.text.toString(),
                        binding.endTime.text.toString(),
                        binding.fees.text.toString(),
                        selectedClinicLatitude,
                        selectedClinicLongitude,
                        appSharedPreferences.getString("doctorImgUrl")!!
                    )
                } else if (imageUri != Uri.EMPTY) {
                    loadingDialog = LoadingDialog.showLoadingDialog(this@EditDoctorProfileActivity)
                    uploadImage(
                        binding.name.text.toString(),
                        binding.city.text.toString(),
                        binding.clinicLocation.text.toString(),
                        binding.startTime.text.toString(),
                        binding.endTime.text.toString(),
                        binding.fees.text.toString(),
                        selectedClinicLatitude,
                        selectedClinicLongitude,
                        appSharedPreferences.getString("doctorUid")!!
                    )
                }
            }
        }

        binding.pickImgBtn.setOnClickListener {
            pickNewImage()
        }
    }

    private fun uploadImage(
        name: String,
        city: String,
        clinicLocation: String,
        startTime: String,
        endTime: String,
        fees: String,
        selectedClinicLatitude: Double,
        selectedClinicLongitude: Double,
        docId: String
    ) {

        val filepath: StorageReference = storageRef.child("DoctorProfileImages/" + docId)

        val uploadTask = filepath.putFile(imageUri)

        uploadTask.addOnSuccessListener { taskSnapshot ->
            // Get the download URL
            val downloadUrlTask = taskSnapshot.storage.downloadUrl
            downloadUrlTask.addOnSuccessListener { uri ->
                updateDataToDb(
                    name,
                    city,
                    clinicLocation,
                    startTime,
                    endTime,
                    fees,
                    selectedClinicLatitude,
                    selectedClinicLongitude,
                    uri.toString()
                )
            }.addOnFailureListener {
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(this@EditDoctorProfileActivity, it.message, Toast.LENGTH_SHORT)
                    .show()
            }
        }.addOnFailureListener {
            LoadingDialog.hideLoadingDialog(loadingDialog)
            Toast.makeText(this@EditDoctorProfileActivity, it.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun pickNewImage() {
        getContent.launch("image/*")
    }

    private var getContent =
        registerForActivityResult<String, Uri>(
            ActivityResultContracts.GetContent()
        ) { result ->
            if (result != null) {
                imageUri = result
                binding.userImg.setImageURI(imageUri)
            }
        }

    private fun updateDataToDb(
        name: String,
        city: String,
        clinicLocation: String,
        startTime: String,
        endTime: String,
        fees: String,
        clinicLatitude: Double,
        clinicLongitude: Double,
        imgUrl: String,
    ) {

        val doctorMap = hashMapOf<String, Any>(
            "name" to name,
            "city" to city,
            "startTime" to startTime,
            "endTime" to endTime,
            "fees" to fees,
            "clinicLocation" to clinicLocation,
            "clinicLatitude" to clinicLatitude,
            "clinicLongitude" to clinicLongitude,
            "imgUrl" to imgUrl,
        )

        doctorProfileRef.updateChildren(doctorMap)
            .addOnSuccessListener {
                updateSharedPreferences(
                    name,
                    city,
                    startTime,
                    endTime,
                    fees,
                    clinicLocation,
                    imgUrl
                )
            }.addOnFailureListener {
                LoadingDialog.hideLoadingDialog(loadingDialog)
                Toast.makeText(
                    this@EditDoctorProfileActivity,
                    it.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun updateSharedPreferences(
        name: String,
        city: String,
        startTime: String,
        endTime: String,
        fees: String,
        clinicLocation: String,
        imgUrl: String
    ) {
        appSharedPreferences.put("doctorName", name)
        appSharedPreferences.put("doctorCity", city)
        appSharedPreferences.put("doctorClinicLocation", clinicLocation)
        appSharedPreferences.put("doctorStartTime", startTime)
        appSharedPreferences.put("doctorEndTime", endTime)
        appSharedPreferences.put("doctorFees", fees)
        appSharedPreferences.put("doctorImgUrl", imgUrl)
        LoadingDialog.hideLoadingDialog(loadingDialog)
        Toast.makeText(
            this@EditDoctorProfileActivity,
            "Profile updated Successfully",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun getProfileDataFromSharedPref() {
        binding.name.setText(appSharedPreferences.getString("doctorName"))
        binding.city.setText(appSharedPreferences.getString("doctorCity"))
        binding.clinicLocation.setText(appSharedPreferences.getString("doctorClinicLocation"))
        binding.startTime.setText(appSharedPreferences.getString("doctorStartTime"))
        binding.endTime.setText(appSharedPreferences.getString("doctorEndTime"))
        binding.fees.setText(appSharedPreferences.getString("doctorFees"))
        selectedClinicLatitude = appSharedPreferences.getFloat("doctorClinicLatitude").toDouble()
        selectedClinicLongitude = appSharedPreferences.getFloat("doctorClinicLongitude").toDouble()
        Glide.with(applicationContext).load(appSharedPreferences.getString("doctorImgUrl"))
            .diskCacheStrategy(
                DiskCacheStrategy.DATA
            ).into(binding.userImg)
    }

    private fun getCityNamesFromDb() {
        FirebaseDatabase.getInstance().getReference(AppConstants.CITY_NAMES)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    cityList.clear()
                    snapshot.children.forEach {
                        cityList.add(it.value.toString())
                    }

                    if (cityList.isNotEmpty()) {
                        binding.city.setAdapter(
                            ArrayAdapter(
                                this@EditDoctorProfileActivity,
                                R.layout.simple_dropdown_item_1line,
                                cityList
                            )
                        )
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@EditDoctorProfileActivity, error.toString(), Toast.LENGTH_SHORT
                    ).show()
                }

            })
    }

    private fun isValid(): Boolean {
        var valid = true

        if (binding.name.text.isNullOrEmpty()) {
            binding.name.error = "Please enter valid name"
            valid = false
        }

        if (binding.city.text.isNullOrEmpty()) {
            binding.city.error = "Please select city"
            valid = false
        }

        if (binding.clinicLocation.text.isNullOrEmpty()) {
            Toast.makeText(
                this@EditDoctorProfileActivity,
                "Please select location",
                Toast.LENGTH_SHORT
            ).show()
            valid = false
        }

        if (binding.startTime.text.isNullOrEmpty()) {
            binding.startTime.error = "Please select start time"
            valid = false
        }
        if (binding.endTime.text.isNullOrEmpty()) {
            binding.endTime.error = "Please select end time"
            valid = false
        }

        if (binding.fees.text.isNullOrEmpty()) {
            binding.fees.error = "Please enter fees"
            valid = false
        }

        if (selectedClinicLatitude == 0.0 && selectedClinicLongitude == 0.0) {
            Toast.makeText(
                this@EditDoctorProfileActivity,
                "Select clinic location again",
                Toast.LENGTH_SHORT
            ).show()
            valid = false
        }
        return valid
    }

    private val mapsActivityResultLauncher = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result!!.resultCode == RESULT_OK) {
            binding.clinicLocation.setText(result.data!!.getStringExtra("address"))
            selectedClinicLatitude = result.data!!.getDoubleExtra("latitude", 0.0)
            selectedClinicLongitude = result.data!!.getDoubleExtra("longitude", 0.0)
        }
    }
}