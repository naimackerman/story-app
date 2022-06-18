package com.dicoding.naim.story.ui.view.login

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import androidx.lifecycle.ViewModelProvider
import com.dicoding.naim.story.R
import com.dicoding.naim.story.database.AuthRepository
import com.dicoding.naim.story.database.SessionPreference
import com.dicoding.naim.story.databinding.ActivityLoginBinding
import com.dicoding.naim.story.helper.hideKeyboard
import com.dicoding.naim.story.helper.showSnackBar
import com.dicoding.naim.story.helper.visibility
import com.dicoding.naim.story.network.ApiConfig
import com.dicoding.naim.story.ui.custom.MyEditTextEmail
import com.dicoding.naim.story.ui.custom.MyEditTextPassword
import com.dicoding.naim.story.ui.view.AuthFactory
import com.dicoding.naim.story.ui.view.SessionFactory
import com.dicoding.naim.story.ui.view.SessionViewModel
import com.dicoding.naim.story.ui.view.main.MainActivity
import com.dicoding.naim.story.ui.view.main.dataStore
import com.dicoding.naim.story.ui.view.register.RegisterActivity

class LoginActivity : AppCompatActivity() {

    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var sessionViewModel: SessionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
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
        loginViewModel = ViewModelProvider(
            this,
            AuthFactory(
                AuthRepository(
                    ApiConfig.getApiService()
                )
            )
        )[LoginViewModel::class.java]

        sessionViewModel = ViewModelProvider(
            this,
            SessionFactory(
                SessionPreference.getInstance(dataStore)
            )
        )[SessionViewModel::class.java]

        loginViewModel.apply {
            isLoading.observe(this@LoginActivity) {
                showLoading(it)
            }

            token.observe(this@LoginActivity) { token ->
                token.getContentIfNotHandled()?.let {
                    loginSuccess(it)
                }
            }

            error.observe(this@LoginActivity) { e ->
                e.getContentIfNotHandled()?.let { message ->
                    binding?.root?.let { showSnackBar(it, message) }
                }
            }
        }

        sessionViewModel.apply {
            getToken().observe(this@LoginActivity) {
                if (it.isNotEmpty()) {
                    showStoryPage()
                }
            }
        }
    }

    private fun setupAction() {
        binding?.apply {
            etEmail.setValidationCallback(object : MyEditTextEmail.InputValidation {
                override val errorMessage: String
                    get() = getString(R.string.message_validation_email)
            })
            etPassword.setValidationCallback(object : MyEditTextPassword.InputValidation {
                override val errorMessage: String
                    get() = getString(R.string.message_validation_password)
            })

            btnToRegister.setOnClickListener {
                showRegisterPage()
            }
            btnLogin.setOnClickListener {
                signInAccount()
                hideKeyboard(this@LoginActivity)
            }
        }
    }

    private fun signInAccount() {
        binding?.apply {
            when {
                !etEmail.validateInput() -> showSnackBar(
                    root,
                    getString(R.string.message_validation_email)
                )
                !etPassword.validateInput() -> showSnackBar(
                    root,
                    getString(R.string.message_validation_password)
                )
                else -> {
                    loginViewModel.login(
                        etEmail.text.toString(),
                        etPassword.text.toString()
                    )
                }
            }
        }
    }

    private fun showRegisterPage() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun loginSuccess(token: String) {
        sessionViewModel.saveToken(token)
        binding?.root?.let { showSnackBar(it, getString(R.string.login_success)) }
    }

    private fun showStoryPage() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showLoading(isLoading: Boolean) {
        binding?.progressBar?.visibility = visibility(isLoading)
    }
}