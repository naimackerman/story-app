package com.dicoding.naim.story.ui.view.story

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.dicoding.naim.story.R
import com.dicoding.naim.story.database.StoryDatabase
import com.dicoding.naim.story.database.StoryRepository
import com.dicoding.naim.story.databinding.ActivityStoryMapsBinding
import com.dicoding.naim.story.network.ApiConfig
import com.dicoding.naim.story.network.ListStoryItem
import com.dicoding.naim.story.ui.view.StoryFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions

class StoryMapsActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        const val EXTRA_TOKEN = "extra_token"
    }

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityStoryMapsBinding

    private lateinit var storyMapsViewModel: StoryMapsViewModel

    private var latestStory: ListStoryItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStoryMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val token = intent.getStringExtra(EXTRA_TOKEN).toString()
        Log.d("token", token)

        storyMapsViewModel = ViewModelProvider(
            this,
            StoryFactory(
                StoryRepository(
                    StoryDatabase.getDatabase(this@StoryMapsActivity),
                    ApiConfig.getApiService(),
                    getString(R.string.auth, token)
                )
            )
        )[StoryMapsViewModel::class.java]

        setMapStyle(mMap)

        mMap.setOnInfoWindowClickListener { markerStory ->
            val story = markerStory.tag as ListStoryItem

            latestStory = story
            val intent = Intent(this@StoryMapsActivity, StoryDetailActivity::class.java)
            intent.putExtra(StoryDetailActivity.EXTRA_STORY, story)
            startActivity(intent)
        }

        storyMapsViewModel.stories.observe(this@StoryMapsActivity) {
            mMap.clear()

            it.forEach { story ->
                val latLng = LatLng(story.lat, story.lon)
                val marker = mMap.addMarker(
                    MarkerOptions().position(latLng)
                        .title(getString(R.string.stories_content_description, story.name))
                        .snippet(getString(R.string.marker_snippet))
                        .icon(
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                        )
                )
                marker?.tag = story
            }

            val story = latestStory ?: it[0]
            mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(LatLng(story.lat, story.lon), 7f)
            )
        }
        getLocation()
    }

    private fun setMapStyle(map: GoogleMap) {
        map.uiSettings.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
        }
        try {
            this.let {
                val success = map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        it,
                        R.raw.map_style
                    )
                )
                if (!success) {
                    Log.e(ContentValues.TAG, "Style parsing failed.")
                }
            }
        } catch (exception: Resources.NotFoundException) {
            Log.e(ContentValues.TAG, "Can't find style. Error: ", exception)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getLocation()
            }
        }

    private fun getLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}