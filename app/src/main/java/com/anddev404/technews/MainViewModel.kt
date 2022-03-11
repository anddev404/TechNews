package com.anddev404.technews

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.anddev404.repository.Repository
import com.anddev404.repository.model.News
import com.anddev404.repository.remote.ApiSource
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainViewModel(val repository: Repository) : ViewModel() {

    private val _news =
        MutableLiveData<News>()

    fun getNews(): LiveData<News> {
        return _news
    }

    fun setNews(news: News) {
        _news.value = news
    }

    fun downloadNews() {

        var api = repository.getApi(ApiSource.TECH_NEWS)

        GlobalScope.launch {

            var newsList = api.getNewsOrEmptyList()
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    setNews(newsList)
                }, 0
            )
        }
    }
}