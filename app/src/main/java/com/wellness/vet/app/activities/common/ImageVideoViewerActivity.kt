package com.wellness.vet.app.activities.common

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.wellness.vet.app.R
import com.wellness.vet.app.databinding.ActivityImageVideoViewerBinding

class ImageVideoViewerActivity : AppCompatActivity() {

    private lateinit var binding : ActivityImageVideoViewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageVideoViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val dataType = intent.getStringExtra("dataType")
        val dataSource = intent.getStringExtra("dataSource")
        if(dataType == "image"){
            binding.singleVideoView.visibility  = View.GONE
            binding.singleImageView.visibility  = View.VISIBLE
            Glide.with(this@ImageVideoViewerActivity)
                .load(dataSource)
                .into(binding.singleImageView)
        }else{
            binding.singleImageView.visibility  = View.GONE
            binding.singleVideoView.visibility  = View.VISIBLE
            binding.singleVideoView.setVideoPath(dataSource);
            binding.singleVideoView.start();
        }
    }

}