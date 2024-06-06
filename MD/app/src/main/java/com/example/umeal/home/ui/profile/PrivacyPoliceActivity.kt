package com.example.umeal.home.ui.profile

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.umeal.R
import com.example.umeal.databinding.ActivityPrivacyPoliceBinding

class PrivacyPoliceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPrivacyPoliceBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPrivacyPoliceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonBack.setOnClickListener {
            finish()
        }


    }
}