package com.example.umeal.auth.forgot_password

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.umeal.R
import com.example.umeal.auth.login.ui.LoginViewModel
import com.example.umeal.auth.login.ui.LoginViewModelFactory
import com.example.umeal.data.ResultState
import com.example.umeal.data.repository.DataRepository
import com.example.umeal.data.retrofit.ApiConfig
import com.example.umeal.databinding.ActivityForgotPasswordBinding
import com.example.umeal.databinding.ArticleItemBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var viewModel: ForgotPasswordViewModel
    private var passwordRecoverJob: Job = Job()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val apiService = ApiConfig.getApiService()
        val dataRepository = DataRepository(apiService)
        val factory = ForgotPasswordViewModelFactory(dataRepository)
        viewModel = ViewModelProvider(this, factory)[ForgotPasswordViewModel::class.java]

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnSendOtp.setOnClickListener {
            val email = binding.edtEmail.text.toString().trim()
            showLoading(true)
            lifecycleScope.launch {
                if (passwordRecoverJob.isActive) passwordRecoverJob.cancel()
                passwordRecoverJob = launch {
                    viewModel.forgotPassword(email).collect { result ->
                        when (result) {
                            is ResultState.Loading -> {
                                showLoading(true)
                            }

                            is ResultState.Error -> {
                                showLoading(false)
                                Toast.makeText(
                                    this@ForgotPasswordActivity,
                                    result.message.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            is ResultState.Success -> {
                                showLoading(false)
                                startActivity(
                                    Intent(
                                        this@ForgotPasswordActivity,
                                        OtpActivity::class.java
                                    )
                                )
                                finish()
                            }
                        }
                    }
                }
            }
        }


    }


    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }
}