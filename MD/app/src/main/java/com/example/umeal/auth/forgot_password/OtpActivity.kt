package com.example.umeal.auth.forgot_password

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.umeal.R
import com.example.umeal.auth.login.ui.LoginActivity
import com.example.umeal.data.ResultState
import com.example.umeal.data.repository.DataRepository
import com.example.umeal.data.retrofit.ApiConfig
import com.example.umeal.databinding.ActivityOtpBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class OtpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpBinding
    private lateinit var viewModel: ForgotPasswordViewModel
    private var passwordRecoverJob: Job = Job()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOtpBinding.inflate(layoutInflater)
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

        binding.btnConfirm.setOnClickListener {
            val otp = binding.edtOtp.text.toString().trim()
            val newPassword = binding.edtPassword.text.toString().trim()
            if (otp.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(this, getString(R.string.warning_input), Toast.LENGTH_SHORT).show()
            } else {
                showLoading(true)
                lifecycleScope.launch {
                    if (passwordRecoverJob.isActive) passwordRecoverJob.cancel()
                    passwordRecoverJob = launch {
                        viewModel.changePassword(otp, newPassword).collect { result ->
                            when (result) {
                                is ResultState.Loading -> {
                                    showLoading(true)
                                }

                                is ResultState.Error -> {
                                    showLoading(false)
                                    Toast.makeText(
                                        this@OtpActivity,
                                        result.message.toString(),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                is ResultState.Success -> {
                                    showLoading(false)
                                    AlertDialog.Builder(this@OtpActivity).apply {
                                        setMessage("Password berhasil diubah")
                                        setPositiveButton("Login") { _, _ ->
                                            startActivity(
                                                Intent(
                                                    this@OtpActivity,
                                                    LoginActivity::class.java
                                                )
                                            )
                                            finish()
                                        }
                                        create()
                                        show()
                                    }
                                }
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