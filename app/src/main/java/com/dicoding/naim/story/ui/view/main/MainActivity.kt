package com.dicoding.naim.story.ui.view.main

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.WindowInsets
import android.view.WindowManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.naim.story.R
import com.dicoding.naim.story.adapter.LoadingStateAdapter
import com.dicoding.naim.story.adapter.StoryListAdapter
import com.dicoding.naim.story.database.SessionPreference
import com.dicoding.naim.story.database.StoryDatabase
import com.dicoding.naim.story.database.StoryRepository
import com.dicoding.naim.story.databinding.ActivityMainBinding
import com.dicoding.naim.story.helper.visibility
import com.dicoding.naim.story.network.ApiConfig
import com.dicoding.naim.story.ui.view.SessionFactory
import com.dicoding.naim.story.ui.view.SessionViewModel
import com.dicoding.naim.story.ui.view.StoryFactory
import com.dicoding.naim.story.ui.view.login.LoginActivity
import com.dicoding.naim.story.ui.view.story.StoryActivity
import com.dicoding.naim.story.ui.view.story.StoryMapsActivity

internal val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class MainActivity : AppCompatActivity() {

    private var token = ""

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding

    private lateinit var mainViewModel: MainViewModel
    private lateinit var sessionViewModel: SessionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
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
    }

    private fun setupViewModel() {
        sessionViewModel = ViewModelProvider(
            this,
            SessionFactory(
                SessionPreference.getInstance(dataStore)
            )
        )[SessionViewModel::class.java]

        sessionViewModel.apply {
            getToken().observe(this@MainActivity) {
                token = it
                if (it.isEmpty() || it == "") {
                    showLoginPage()
                } else {
                    showStory()
                }
            }
        }
    }

    private fun setupAction() {
        binding?.apply {
            fabAddStory.setOnClickListener {
                showAddStoryPage()
            }
            fabStoryMap.setOnClickListener {
                showStoryMaps()
            }
        }
    }

    private fun showStory() {
        showLoading(true)

        mainViewModel = ViewModelProvider(
            this,
            StoryFactory(
                StoryRepository(
                    StoryDatabase.getDatabase(this@MainActivity),
                    ApiConfig.getApiService(),
                    getString(R.string.auth, token)
                )
            )
        )[MainViewModel::class.java]

        val adapter = StoryListAdapter()
        binding?.apply {
            rvStories.layoutManager = if (resources.configuration.orientation
                == Configuration.ORIENTATION_PORTRAIT
            ) {
                LinearLayoutManager(this@MainActivity)
            } else {
                GridLayoutManager(this@MainActivity, 2)
            }
            rvStories.adapter = adapter.withLoadStateFooter(
                footer = LoadingStateAdapter {
                    adapter.retry()
                }
            )
        }

        mainViewModel.stories.observe(this) {
            adapter.submitData(lifecycle, it)
        }

        showLoading(false)
    }

    private fun showAddStoryPage() {
        val intent = Intent(this@MainActivity, StoryActivity::class.java).apply {
            putExtra(StoryActivity.EXTRA_TOKEN, token)
        }
        startActivity(intent)
    }

    private fun showStoryMaps() {
        val intent = Intent(this@MainActivity, StoryMapsActivity::class.java).apply {
            putExtra(StoryActivity.EXTRA_TOKEN, token)
        }
        startActivity(intent)
    }

    private fun showLoading(isLoading: Boolean) {
        binding?.pbStory?.visibility = visibility(isLoading)
    }

    private fun showLoginPage() {
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_logout -> {
                sessionViewModel.saveToken("")
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
            R.id.action_languages -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
            }
        }
        return true
    }
}