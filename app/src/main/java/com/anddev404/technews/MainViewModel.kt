package com.anddev404.technews

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.anddev404.repository.Repository
import com.anddev404.repository.model.News

class MainViewModel(val repository: Repository) : ViewModel() {

    private val _news =
        MutableLiveData<News>()

    fun getNews(): LiveData<News> {
        return _news
    }

    fun setNews(news: News) {
        _news.value = news
    }
}