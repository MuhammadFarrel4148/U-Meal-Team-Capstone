package com.example.umeal.auth.login.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import com.example.umeal.auth.signup.ui.RegisterActivity
import com.example.umeal.auth.signup.ui.RegisterViewModel
import com.example.umeal.auth.signup.ui.RegisterViewModelFactory
import com.example.umeal.data.ResultState
import com.example.umeal.data.repository.DataRepository
import com.example.umeal.data.retrofit.ApiConfig
import com.example.umeal.databinding.ActivityLoginBinding
import com.example.umeal.home.HomeActivity
import com.example.umeal.utils.PreferenceManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var edtEmail: TextInputEditText
    private var loginJob: Job = Job()

    private lateinit var viewModel: LoginViewModel
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        emailInputLayout = binding.emailInputLayout
        edtEmail = binding.edtEmail

        preferenceManager = PreferenceManager(this)

        val apiService = ApiConfig.getApiService()
        val dataRepository = DataRepository(apiService)
        val factory = LoginViewModelFactory(dataRepository)
        viewModel = ViewModelProvider(this, factory)[LoginViewModel::class.java]

        edtEmail.doOnTextChanged { text, _, _, _ ->
            validateEmail(text.toString())
        }

        setupWindowInsets()
        setupClickListeners()
        setupPasswordInputWatcher()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupClickListeners() {
        binding.btnSignup.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        binding.btnLogin.setOnClickListener {
            val name = binding.edtName.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                showToast(getString(R.string.warning_input))
            } else {
                loginUser(name, email, password)
            }
        }
    }

    private fun setupPasswordInputWatcher() {
        val passwordInputLayout: TextInputLayout = binding.passwordInputLayout
        val edtPassword: TextInputEditText = binding.edtPassword

        edtPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                if (password.length < 8) {
                    passwordInputLayout.helperText = getString(R.string.invalid_password)
                } else {
                    passwordInputLayout.helperText = null
                }
            }
        })
    }

    private fun loginUser(name: String, email: String, password: String) {
        setLoadingState(true)
        lifecycleScope.launch {
            if (loginJob.isActive) loginJob.cancel()
            loginJob = launch {
                viewModel.login(email, password).collect { result ->
                    when (result) {
                        is ResultState.Success -> handleLoginSuccess(
                            result.data?.token,
                            name,
                            email
                        )

                        is ResultState.Loading -> setLoadingState(true)
                        is ResultState.Error -> handleLoginError(result.message)
                    }
                }
            }
        }
    }

    private fun setLoadingState(loading: Boolean) {
        binding.btnLogin.visibility = if (loading) View.INVISIBLE else View.VISIBLE
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun handleLoginSuccess(token: String?, name: String, email: String) {
        setLoadingState(false)
        preferenceManager.name = name
        preferenceManager.email = email
        token?.let {
            preferenceManager.exampleBoolean = true
            preferenceManager.token = it
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

    private fun handleLoginError(errorMessage: String?) {
        setLoadingState(false)
        showToast(errorMessage ?: getString(R.string.check))
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun validateEmail(email: String) {
        if (email.endsWith("@gmail.com")) {
            emailInputLayout.helperText = null
        } else {
            emailInputLayout.helperText = getString(R.string.error_email)
        }
    }
}
