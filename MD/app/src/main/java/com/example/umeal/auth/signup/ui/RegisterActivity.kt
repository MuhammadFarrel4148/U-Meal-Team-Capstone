package com.example.umeal.auth.signup.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import com.example.umeal.R
import com.example.umeal.auth.login.ui.LoginActivity
import com.example.umeal.databinding.ActivityLoginBinding
import com.example.umeal.databinding.ActivityRegisterBinding
import com.example.umeal.home.HomeActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var edtEmail: TextInputEditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        emailInputLayout = findViewById(R.id.emailInputLayout)
        edtEmail = findViewById(R.id.edtEmail)

        edtEmail.doOnTextChanged { text, _, _, _ ->
            validateEmail(text.toString())
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        binding.btnSignup.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        val passwordInputLayout: TextInputLayout = findViewById(R.id.passwordInputLayout)
        val edtPassword: TextInputEditText = findViewById(R.id.edtPassword)

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

    private fun validateEmail(email: String) {
        if (email.endsWith("@gmail.com")) {
            emailInputLayout.helperText = null
        } else {
            emailInputLayout.helperText = getString(R.string.error_email)
        }
    }
}