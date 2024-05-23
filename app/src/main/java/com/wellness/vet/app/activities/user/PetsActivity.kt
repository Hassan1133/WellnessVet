package com.wellness.vet.app.activities.user

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.wellness.vet.app.R
import com.wellness.vet.app.databinding.ActivityPetsBinding

class PetsActivity : AppCompatActivity() {

    private lateinit var binding : ActivityPetsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPetsBinding.inflate(layoutInflater)
        setContentView(binding.root)



    }
}