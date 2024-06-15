package com.example.umeal.auth.signup.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.umeal.R
import com.example.umeal.auth.login.ui.LoginActivity
import com.example.umeal.data.retrofit.ApiConfig
import com.example.umeal.data.repository.DataRepository
import com.example.umeal.data.ResultState
import com.example.umeal.databinding.ActivityRegisterBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var edtEmail: TextInputEditText

    private lateinit var viewModel: RegisterViewModel
    private var registerJob: Job = Job()

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        emailInputLayout = binding.emailInputLayout
        edtEmail = binding.edtEmail

        edtEmail.doOnTextChanged { text, _, _, _ -> validateEmail(text.toString()) }

        val apiService = ApiConfig.getApiService()
        val dataRepository = DataRepository(apiService)
        val factory = RegisterViewModelFactory(dataRepository)
        viewModel = ViewModelProvider(this, factory)[RegisterViewModel::class.java]

        setupWindowInsets()
        setupClickListeners()
        setupPasswordInputWatcher()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.btnSignup.setOnClickListener {
            val username = binding.edtUsername.text.toString().trim()
            val email = binding.edtEmail.text.toString().trim()
            val phoneNumber = binding.edtPhoneNumber.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()

            if (username.isEmpty() || email.isEmpty() || phoneNumber.isEmpty() || password.isEmpty()) {
                showToast(getString(R.string.warning_input))
            } else {
                registerUser(username, email, phoneNumber, password)
            }
        }
    }

    private fun registerUser(username: String, email: String, phoneNumber: String, password: String) {
        setLoadingState(true)
        lifecycleScope.launch {
            if (registerJob.isActive) registerJob.cancel()
            registerJob = launch {
                viewModel.register(username, email, phoneNumber, password).collect { result ->
                    when (result) {
                        is ResultState.Success -> handleRegisterSuccess()
                        is ResultState.Loading -> setLoadingState(true)
                        is ResultState.Error -> handleRegisterError(result.message)
                    }
                }
            }
        }
    }

    private fun handleRegisterSuccess() {
        setLoadingState(false)
        showToast(getString(R.string.success_register))
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun handleRegisterError(errorMessage: String?) {
        setLoadingState(false)
        showToast(errorMessage ?: getString(R.string.check))
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun setLoadingState(loading: Boolean) {
        binding.btnSignup.visibility = if (loading) View.INVISIBLE else View.VISIBLE
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun setupPasswordInputWatcher() {
        binding.edtPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                binding.passwordInputLayout.helperText =
                    if (password.length < 8) getString(R.string.invalid_password) else null
            }
        })
    }

    private fun validateEmail(email: String) {
        emailInputLayout.helperText =
            if (email.endsWith("@gmail.com")) null else getString(R.string.error_email)
    }
}
