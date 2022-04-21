package com.anddev404.technews

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.anddev404.repository.Repository
import com.anddev404.repository.model.News
import com.anddev404.tech_news_views.newsListFragment.NewsListFragment
import com.anddev404.tech_news_views.newsListFragment.OnNewsListFragmentListener
import com.anddev404.tech_news_views.newsListFragment.model.NewsItem
import com.anddev404.tech_news_views.showErrorFragment.Error
import com.anddev404.tech_news_views.showErrorFragment.ErrorType
import com.anddev404.tech_news_views.showErrorFragment.OnShowErrorFragmentListener
import com.anddev404.tech_news_views.showErrorFragment.ShowErrorFragment
import com.anddev404.tech_news_views.showNewsDetailsFragment.NewsDetailsFragment
import com.anddev404.tech_news_views.showProgressFragment.ShowProgressFragment
import com.anddev404.technews.utils.Internet
import com.anddev404.technews.utils.ModelConverter

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    private lateinit var newsFragment: NewsListFragment
    private lateinit var errorFragment: ShowErrorFragment
    private lateinit var progressFragment: ShowProgressFragment
    private lateinit var detailFragment: NewsDetailsFragment

    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewModelFactory = MainViewModelFactory(Repository())
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

        initializeNewsFragment()
        initializeErrorFragment()
        initializeProgressFragment()
        initializeDetailsFragment()

        setCallbackForNewsFragment()
        setCallbackForErrorFragment()

        setObservers()

        getNewsIfNeeded()
    }

    override fun onPause() {
        super.onPause()
        viewModel.listPosition = newsFragment.getFirstVisibleItemPosition()
    }

    private fun getNewsIfNeeded() {
        if (viewModel.getNewsSize() == 0) {
            if (Internet.isOnline(this)) {

                supportFragmentManager.beginTransaction()
                    .replace(R.id.news_fragment, progressFragment).commit()
                viewModel.downloadNews()
            } else {

                supportFragmentManager.beginTransaction()
                    .replace(R.id.news_fragment, errorFragment).commit()
            }
        }
    }

    //region fragment initializations

    private fun initializeNewsFragment() {
        if (viewModel.getNewsSize() > 0) {

            var news = viewModel.getNews().value?.news ?: arrayListOf()
            newsFragment =
                NewsListFragment.newInstance(
                    1,
                    ModelConverter.SingularNewsListToNewsItemList(news).toTypedArray(),
                    viewModel.listPosition
                )

        } else {
            newsFragment = NewsListFragment.newInstance(1)

        }
    }

    private fun initializeErrorFragment() {
        errorFragment = ShowErrorFragment.newInstance(
            Error(
                "Turn On The Internet",
                "Ok",
                ErrorType.INTERNET_OFF
            )
        )
    }

    private fun initializeProgressFragment() {
        progressFragment = ShowProgressFragment()
    }

    private fun initializeDetailsFragment() {
        detailFragment = NewsDetailsFragment()
    }

    //endregion

    //region fragment callbacks

    private fun setCallbackForNewsFragment() {

        newsFragment.setOnNewsListFragmentListener(object : OnNewsListFragmentListener {
            override fun setImage(url: String, newsItem: NewsItem, imageView: ImageView) {

                viewModel.setImage(url, imageView, applicationContext)
            }

            override fun tapItem(itemPosition: Int, newsItem: NewsItem) {

                if (viewModel.getNews().value is News) {

                    detailFragment.arguments = Bundle().apply {
                        putString(
                            "url", (viewModel.getNews().value as News).news.get(
                                itemPosition
                            ).link
                        )
                    }
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.news_fragment, detailFragment).commit()
                }
            }

            override fun updateList() {
                // TODO("Not yet implemented")
            }
        })
    }

    private fun setCallbackForErrorFragment() {

        errorFragment.setOnShowErrorFragmentListener(object : OnShowErrorFragmentListener {
            override fun clickedErrorButton(error: Error) {
                getNewsIfNeeded()
            }
        })
    }

    //endregion

    //region live data observers

    private fun setObservers() {
        viewModel.getNews().observe(this, Observer {

            newsFragment.arguments?.putParcelableArray(
                NewsListFragment.ARG_NEWS_LIST,
                ModelConverter.SingularNewsListToNewsItemList(it.news).toTypedArray()
            )
            supportFragmentManager.beginTransaction()
                .replace(R.id.news_fragment, newsFragment).commit()

        })
    }
    //endregion

    override fun onBackPressed() {

        if (this::detailFragment.isInitialized && detailFragment != null && detailFragment.isVisible()) {
            if (detailFragment.goBack()) {
            } else {
                newsFragment.arguments?.putParcelableArray(
                    NewsListFragment.ARG_NEWS_LIST,
                    ModelConverter.SingularNewsListToNewsItemList(
                        viewModel.getNews().value?.news ?: arrayListOf()
                    ).toTypedArray()
                )
                supportFragmentManager.beginTransaction()
                    .replace(R.id.news_fragment, newsFragment).commit()
            }
        } else {
            super.onBackPressed()
        }

    }
}