package com.dicoding.naim.story.database

import androidx.lifecycle.LiveData
import androidx.paging.*
import com.dicoding.naim.story.network.ApiService
import com.dicoding.naim.story.network.ListStoryItem
import okhttp3.MultipartBody
import okhttp3.RequestBody

class StoryRepository(
    private val storyDatabase: StoryDatabase,
    private val apiService: ApiService,
    private val auth: String
) {
    fun getStoriesPaged(): LiveData<PagingData<ListStoryItem>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            remoteMediator = StoryRemoteMediator(storyDatabase, apiService, auth),
            pagingSourceFactory = {
                storyDatabase.storyDao().getAllStory()
            }
        ).liveData
    }

    suspend fun getStoriesWithLocation() =
        apiService.getAllStories(auth, 1).listStory

    suspend fun addNewStory(
        multipart: MultipartBody.Part,
        params: HashMap<String, RequestBody>,
    ) = !apiService.addStory(multipart, params, auth).error
}