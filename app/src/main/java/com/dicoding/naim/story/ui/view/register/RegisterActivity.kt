package com.dicoding.naim.story.ui.view.register

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import androidx.lifecycle.ViewModelProvider
import com.dicoding.naim.story.R
import com.dicoding.naim.story.database.AuthRepository
import com.dicoding.naim.story.databinding.ActivityRegisterBinding
import com.dicoding.naim.story.helper.hideKeyboard
import com.dicoding.naim.story.helper.showSnackBar
import com.dicoding.naim.story.helper.visibility
import com.dicoding.naim.story.network.ApiConfig
import com.dicoding.naim.story.ui.custom.MyEditText
import com.dicoding.naim.story.ui.custom.MyEditTextEmail
import com.dicoding.naim.story.ui.custom.MyEditTextPassword
import com.dicoding.naim.story.ui.view.AuthFactory
import com.dicoding.naim.story.ui.view.login.LoginActivity

class RegisterActivity : AppCompatActivity() {

    private var _binding: ActivityRegisterBinding? = null
    private val binding get() = _binding

    private lateinit var registerViewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupView()
        setupViewModel()
        setupAction()
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun setupViewModel() {
        registerViewModel = ViewModelProvider(
            this,
            AuthFactory(
                AuthRepository(
                    ApiConfig.getApiService()
                )
            )
        )[RegisterViewModel::class.java]

        registerViewModel.apply {
            isLoading.observe(this@RegisterActivity) {
                showLoading(it)
            }
            isSuccess.observe(this@RegisterActivity) {
                it.getContentIfNotHandled()?.let { success ->
                    if (success) {
                        registerSuccess()
                    }
                }
            }
            error.observe(this@RegisterActivity) { e ->
                e.getContentIfNotHandled()?.let { message ->
                    binding?.root?.let { showSnackBar(it, message) }
                }
            }
        }
    }

    private fun setupAction() {
        binding?.apply {
            etName.setValidationCallback(object : MyEditText.InputValidation {
                override val errorMessage: String
                    get() = getString(R.string.message_validation_name)
            })
            etEmail.setValidationCallback(object : MyEditTextEmail.InputValidation {
                override val errorMessage: String
                    get() = getString(R.string.message_validation_email)
            })
            etPassword.setValidationCallback(object : MyEditTextPassword.InputValidation {
                override val errorMessage: String
                    get() = getString(R.string.message_validation_password)
            })

            btnRegister.setOnClickListener {
                signUpAccount()
                hideKeyboard(this@RegisterActivity)
            }
            btnToLogin.setOnClickListener {
                showLoginPage()
            }
        }
    }

    private fun signUpAccount() {
        binding?.apply {
            when {
                !etName.validateInput() -> showSnackBar(
                    root,
                    getString(R.string.message_validation_name)
                )
                !etEmail.validateInput() -> showSnackBar(
                    root,
                    getString(R.string.message_validation_email)
                )
                !etPassword.validateInput() -> showSnackBar(
                    root,
                    getString(R.string.message_validation_password)
                )
                else -> {
                    registerViewModel.register(
                        etName.text.toString(),
                        etEmail.text.toString(),
                        etPassword.text.toString()
                    )
                }
            }
        }
    }

    private fun showLoginPage() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun registerSuccess() {
        binding?.root?.let { showSnackBar(it, getString(R.string.register_success)) }
        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showLoading(isLoading: Boolean) {
        binding?.progressBar?.visibility = visibility(isLoading)
    }
}