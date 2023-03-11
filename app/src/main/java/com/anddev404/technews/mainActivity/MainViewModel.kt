package com.anddev404.technews.mainActivity

import android.app.Application
import android.content.Context
import android.widget.ImageView
import androidx.lifecycle.*
import com.anddev404.repository.Repository
import com.anddev404.repository.errors.ResponseError
import com.anddev404.repository.model.News
import com.anddev404.repository.remote.ApiSource
import com.anddev404.repository.remote.image_loaders.ImageLoaderSource
import com.anddev404.tech_news_views.newsListFragment.model.NewsItem
import com.anddev404.technews.utils.ModelConverter
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: Repository,
    private val applicationContext: Application
) : ViewModel() {

    private val _actualFragment = MutableLiveData(FragmentsEnum.LOAD_LIST)
    val actualFragment: LiveData<FragmentsEnum> = _actualFragment

    //region news list
    var listPosition = 0

    private val _newsRepository = MutableLiveData<News>()
    private var newsView: LiveData<ArrayList<NewsItem>> =
        Transformations.map(_newsRepository) {
            ModelConverter.singularNewsListToNewsItemList(it.news)
        }

    val news: MediatorLiveData<ArrayList<NewsItem>> =
        MediatorLiveData<ArrayList<NewsItem>>().apply {

            addSource(actualFragment) {
                when (it) {
                    FragmentsEnum.LOAD_LIST -> {
                        downloadNews()
                    }
                    FragmentsEnum.SHOW_LIST -> {
                        if (newsView.value != null) value = newsView.value
                    }
                    else -> {}
                }
            }
            addSource(newsView) {
                when (actualFragment.value) {
                    FragmentsEnum.SHOW_LIST -> {
                        value = it
                    }
                    else -> {}
                }
            }
        }

    val error = MutableLiveData<ResponseError>()

    fun loadList() {
        _actualFragment.value = FragmentsEnum.LOAD_LIST
    }

    fun showList() {
        _actualFragment.value = FragmentsEnum.SHOW_LIST
    }

    private fun downloadNews() {

        val api = repository.getApiV2(ApiSource.NEWS)
        viewModelScope.launch {

            val response = api.getResponse()
            if (response.responseCode == 200) {

                _newsRepository.postValue(response.news.value)
                _actualFragment.postValue(FragmentsEnum.SHOW_LIST)
            } else {

                error.postValue(response.error.value)
                _actualFragment.postValue(FragmentsEnum.ERROR)
            }
        }
    }

    fun setImage(url: String, imageView: ImageView, context: Context) {
        repository.getImageLoader(ImageLoaderSource.PICASSO_LIBRARY)
            .loadImage(imageView, url, context)
    }

    //endregion

    //region show news details

    private val _url: MutableLiveData<String> = MutableLiveData()

    fun setUrl(url: String) {
        _url.value = url
    }

    fun showNewsDetails() {
        _actualFragment.value = FragmentsEnum.SHOW_NEWS_DETAILS
    }

    val newsDetails: MediatorLiveData<String> =
        MediatorLiveData<String>().apply {

            addSource(actualFragment) {
                when (it) {
                    FragmentsEnum.SHOW_NEWS_DETAILS -> {
                        if (_url.value != null) value = _url.value
                    }
                    else -> {}
                }
            }
            addSource(_url) {
                when (actualFragment.value) {
                    FragmentsEnum.SHOW_NEWS_DETAILS -> {
                        value = it
                    }
                    else -> {}
                }
            }
        }

    //endregion

    //region actionBar

    val actionBarTitle: LiveData<FragmentsEnum> = _actualFragment

    //endregion
}