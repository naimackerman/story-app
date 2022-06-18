package com.dicoding.naim.story.ui.widget

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.os.bundleOf
import com.bumptech.glide.Glide
import com.dicoding.naim.story.R
import com.dicoding.naim.story.database.SessionPreference
import com.dicoding.naim.story.network.ApiConfig
import com.dicoding.naim.story.network.ListStoryItem
import com.dicoding.naim.story.ui.view.main.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

internal class StackRemoteViewsFactory(private val mContext: Context) :
    RemoteViewsService.RemoteViewsFactory {

    companion object {
        private const val TAG = "StackRemoteViewsFactory"
    }

    private val mWidgetItems = ArrayList<ListStoryItem>()

    override fun onCreate() {

    }

    override fun onDataSetChanged(): Unit = runBlocking {
        val pref = SessionPreference.getInstance(mContext.dataStore)
        val auth = mContext.getString(R.string.auth, pref.getToken().first())

        try {
            mWidgetItems.clear()
            mWidgetItems.addAll(
                ApiConfig.getApiService()
                    .getAllStories(auth, 0).listStory
            )
        } catch (e: Exception) {
            Log.e(TAG, "onResponse: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun onDestroy() {

    }

    override fun getCount(): Int = mWidgetItems.size

    override fun getViewAt(position: Int): RemoteViews {
        val rv = RemoteViews(mContext.packageName, R.layout.item_widget).apply {
            val image = Glide.with(mContext)
                .asBitmap()
                .load(mWidgetItems[position].photoUrl)
                .submit()
                .get()

            setImageViewBitmap(R.id.imageView, image)
        }

        val extras = bundleOf(
            BannerWidget.EXTRA_ITEM to mWidgetItems[position].name
        )
        val fillInIntent = Intent()
        fillInIntent.putExtras(extras)

        rv.setOnClickFillInIntent(R.id.imageView, fillInIntent)
        return rv
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(i: Int): Long = 0

    override fun hasStableIds(): Boolean = false

}