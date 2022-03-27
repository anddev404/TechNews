package com.anddev404.technews

import android.content.Context
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.anddev404.repository.Repository
import com.anddev404.repository.model.News
import com.anddev404.repository.remote.ApiSource
import com.anddev404.repository.remote.image_loaders.ImageLoaderSource
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainViewModel(private val repository: Repository) : ViewModel() {

    private val _news =
        MutableLiveData<News>()

    fun getNews(): LiveData<News> {
        return _news
    }

    fun getNewsSize(): Int {
        return _news.value?.news?.size ?: 0
    }

    fun downloadNews() {

        val api = repository.getApi(ApiSource.TECH_NEWS)

        GlobalScope.launch {

            val newsList = api.getNewsOrEmptyList()
            _news.postValue(newsList)
        }
    }

    fun setImage(url: String, imageView: ImageView, context: Context) {
        repository.getImageLoader(ImageLoaderSource.PICASSO_LIBRARY)
            .loadImage(imageView, url, context)
    }
}