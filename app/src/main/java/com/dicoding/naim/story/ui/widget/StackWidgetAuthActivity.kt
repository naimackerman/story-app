package com.dicoding.naim.story.ui.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.dicoding.naim.story.R
import com.dicoding.naim.story.databinding.ActivityStackWidgetAuthBinding
import com.dicoding.naim.story.helper.showSnackBar
import com.dicoding.naim.story.helper.visibility
import com.dicoding.naim.story.ui.custom.MyEditTextEmail
import com.dicoding.naim.story.ui.custom.MyEditTextPassword
import com.dicoding.naim.story.ui.view.SessionViewModel
import com.dicoding.naim.story.ui.view.login.LoginViewModel

class StackWidgetAuthActivity : AppCompatActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var binding: ActivityStackWidgetAuthBinding

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var sessionViewModel: SessionViewModel

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        setResult(RESULT_CANCELED)

        binding = ActivityStackWidgetAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showLoading(false)

        sessionViewModel.getToken().observe(this) {
            if (it.isNotEmpty() || it !== "") {
                showWidget()
            }
        }

        loginViewModel.apply {
            isLoading.observe(this@StackWidgetAuthActivity) {
                showLoading(it)
            }

            token.observe(this@StackWidgetAuthActivity) {
                it.getContentIfNotHandled()?.let { token ->
                    Toast.makeText(
                        this@StackWidgetAuthActivity,
                        getString(R.string.login_success),
                        Toast.LENGTH_SHORT
                    ).show()
                    sessionViewModel.saveToken(token)
                }
            }

            error.observe(this@StackWidgetAuthActivity) {
                it.getContentIfNotHandled()?.let { message ->
                    showSnackBar(binding.root, message)
                }
            }
        }

        binding.apply {
            etEmail.setValidationCallback(object : MyEditTextEmail.InputValidation {
                override val errorMessage: String
                    get() = getString(R.string.message_validation_email)
            })

            etPassword.setValidationCallback(object : MyEditTextPassword.InputValidation {
                override val errorMessage: String
                    get() = getString(R.string.message_validation_password)
            })

            btnLogin.setOnClickListener {
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

        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        finish()
    }

    override fun onStop() {
        super.onStop()
        finish()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = visibility(isLoading)
    }

    private fun showWidget() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        BannerWidget.updateAppWidget(this, appWidgetManager, appWidgetId)

        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(RESULT_OK, resultValue)
        finish()
    }
}
