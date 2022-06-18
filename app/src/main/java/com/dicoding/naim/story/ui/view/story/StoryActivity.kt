package com.dicoding.naim.story.ui.view.story

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.dicoding.naim.story.R
import com.dicoding.naim.story.database.StoryDatabase
import com.dicoding.naim.story.database.StoryRepository
import com.dicoding.naim.story.databinding.ActivityStoryBinding
import com.dicoding.naim.story.helper.reduceFileImage
import com.dicoding.naim.story.helper.showSnackBar
import com.dicoding.naim.story.helper.uriToFile
import com.dicoding.naim.story.helper.visibility
import com.dicoding.naim.story.network.ApiConfig
import com.dicoding.naim.story.ui.custom.MyEditText
import com.dicoding.naim.story.ui.view.StoryFactory
import com.dicoding.naim.story.ui.view.camera.CameraActivity
import com.dicoding.naim.story.ui.view.main.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.File

class StoryActivity : AppCompatActivity() {

    companion object {
        const val CAMERA_X_RESULT = 200
        const val EXTRA_TOKEN = "extra_token"
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

    private var _binding: ActivityStoryBinding? = null
    private val binding get() = _binding

    private lateinit var storyViewModel: StoryViewModel

    private var tempTakenImageFile: File? = null

    private var location: Location? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    this,
                    "Tidak mendapatkan permission.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (!allPermissionsGranted()) {
            binding?.root?.let {
                showSnackBar(it, getString(R.string.permission_denied))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityStoryBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        val token = intent.getStringExtra(EXTRA_TOKEN).toString()

        storyViewModel = ViewModelProvider(
            this,
            StoryFactory(
                StoryRepository(
                    StoryDatabase.getDatabase(this@StoryActivity),
                    ApiConfig.getApiService(),
                    getString(R.string.auth, token)
                )
            )
        )[StoryViewModel::class.java]

        binding?.apply {
            etDescription.setValidationCallback(object : MyEditText.InputValidation {
                override val errorMessage: String
                    get() = getString(R.string.message_validation_description)
            })
            btnCamera.setOnClickListener { startCameraX() }
            btnGallery.setOnClickListener { startGallery() }
            btnUpload.setOnClickListener { uploadImage(storyViewModel) }
        }

        storyViewModel.apply {
            isLoading.observe(this@StoryActivity) {
                showLoading(it)
            }
            isSuccess.observe(this@StoryActivity) {
                it.getContentIfNotHandled()?.let { success ->
                    if (success) {
                        showStoryPage()
                    }
                }
            }
            error.observe(this@StoryActivity) { e ->
                e.getContentIfNotHandled()?.let { message ->
                    binding?.root?.let { it -> showSnackBar(it, message) }
                }
            }
        }
        getLocation()
    }

    private fun showStoryPage() {
        val intent = Intent(this@StoryActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showLoading(isLoading: Boolean) {
        binding?.uploadProgressBar?.visibility = visibility(isLoading)
    }

    private fun startCameraX() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    private fun uploadImage(viewModel: StoryViewModel) {
        if (tempTakenImageFile != null && binding?.etDescription?.validateInput() == true) {
            val file = reduceFileImage(tempTakenImageFile as File)

            val description = binding?.etDescription?.text.toString()
            viewModel.uploadStory(file, description, location)
        } else {
            Toast.makeText(this@StoryActivity, R.string.error_image_empty, Toast.LENGTH_SHORT)
                .show()
        }
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERA_X_RESULT) {
            val myFile = it.data?.getSerializableExtra("picture") as File
            it.data?.getBooleanExtra("isBackCamera", true) as Boolean

            tempTakenImageFile = myFile
            val result = BitmapFactory.decodeFile(myFile.path)

            binding?.ivPreview?.setImageBitmap(result)
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            val myFile = uriToFile(selectedImg, this@StoryActivity)
            tempTakenImageFile = myFile

            binding?.ivPreview?.setImageURI(selectedImg)
        }
    }

    private fun getLocation() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    binding?.tvLocation?.text =
                        getString(R.string.location, location.latitude, location.longitude)
                    this.location = location
                } else {
                    Toast.makeText(
                        this@StoryActivity,
                        "Location is not found. Try Again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
}