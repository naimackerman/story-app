package com.dicoding.naim.story.ui.view.story

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.dicoding.naim.story.databinding.ActivityStoryDetailBinding
import com.dicoding.naim.story.network.ListStoryItem

class StoryDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_STORY = "extra_story"
    }

    private var _binding: ActivityStoryDetailBinding? = null
    private val binding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityStoryDetailBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val story = intent.getParcelableExtra<ListStoryItem>(EXTRA_STORY) as ListStoryItem
        showStoryDetail(story)
    }

    private fun showStoryDetail(story: ListStoryItem) {
        binding?.apply {
            imgStoryDetail.let {
                Glide.with(applicationContext)
                    .load(story.photoUrl)
                    .into(it)
            }
            tvDetailName.text = story.name
            tvDetailDescription.text = story.description
        }
    }
}