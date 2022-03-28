package com.anddev404.technews

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.anddev404.repository.Repository
import com.anddev404.tech_news_views.NewsListFragment
import com.anddev404.tech_news_views.OnNewsListFragmentListener
import com.anddev404.tech_news_views.placeholder.NewsItem
import com.anddev404.tech_news_views.showErrorFragment.Error
import com.anddev404.tech_news_views.showErrorFragment.ErrorType
import com.anddev404.tech_news_views.showErrorFragment.OnShowErrorFragmentListener
import com.anddev404.tech_news_views.showErrorFragment.ShowErrorFragment
import com.anddev404.tech_news_views.showProgressFragment.ShowProgressFragment
import com.anddev404.technews.utils.Internet
import com.anddev404.technews.utils.ModelConverter

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var newsFragment: NewsListFragment
    private lateinit var errorFragment: ShowErrorFragment
    private lateinit var progressFragment: ShowProgressFragment

    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewModelFactory = MainViewModelFactory(Repository())
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

        initializeNewsFragment()
        initializeErrorFragment()
        initializeProgressFragment()

        setCallbackForNewsFragment()
        setCallbackForErrorFragment()

        setObservers()

        getNewsIfNeeded()

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
        newsFragment = NewsListFragment.newInstance()
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

    //endregion

    //region fragment callbacks

    private fun setCallbackForNewsFragment() {

        newsFragment.setOnNewsListFragmentListener(object : OnNewsListFragmentListener {
            override fun setImage(url: String, newsItem: NewsItem, imageView: ImageView) {

                viewModel.setImage(url, imageView, applicationContext)
            }

            override fun tapItem(itemPosition: Int, newsItem: NewsItem) {
                // TODO("Not yet implemented")
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

            newsFragment.arguments = Bundle().apply {
                putParcelableArray(
                    NewsListFragment.ARG_NEWS_LIST,
                    ModelConverter.SingularNewsListToNewsItemList(it.news).toTypedArray()
                )

            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.news_fragment, newsFragment).commit()

        })
    }
    //endregion
}