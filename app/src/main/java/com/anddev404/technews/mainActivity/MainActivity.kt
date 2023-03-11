package com.anddev404.technews.mainActivity

import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.anddev404.repository.Repository
import com.anddev404.tech_news_views.newsListFragment.NewsListFragment
import com.anddev404.tech_news_views.newsListFragment.OnNewsListFragmentListener
import com.anddev404.tech_news_views.newsListFragment.model.NewsItem
import com.anddev404.tech_news_views.showErrorFragment.Error
import com.anddev404.tech_news_views.showErrorFragment.OnShowErrorFragmentListener
import com.anddev404.tech_news_views.showErrorFragment.ShowErrorFragment
import com.anddev404.tech_news_views.showNewsDetailsFragment.NewsDetailsFragment
import com.anddev404.tech_news_views.showProgressFragment.ShowProgressFragment
import com.anddev404.technews.R
import com.anddev404.technews.utils.AndroidBars.Companion.changeColors

class MainActivity : AppCompatActivity() {

    //region variables
    private lateinit var viewModel: MainViewModel

    private lateinit var newsFragment: NewsListFragment
    private lateinit var errorFragment: ShowErrorFragment
    private lateinit var progressFragment: ShowProgressFragment
    private lateinit var detailFragment: NewsDetailsFragment
    //endregion

    //region activity methods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(null)
        setContentView(R.layout.activity_main)

        val viewModelFactory = MainViewModelFactory(Repository())
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

        initializeNewsFragment()
        initializeErrorFragment()
        initializeDetailsFragment()
        initializeProgressFragment()

        setCallbackForErrorFragment()
        setCallbackForNewsFragment()

        setObservers()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        changeColors(this, resources.getColor(R.color.primary_color,theme))
    }

    override fun onPause() {
        super.onPause()

        if (viewModel.actualFragment.value == FragmentsEnum.SHOW_LIST) {

            val position = newsFragment.getFirstVisibleItemPosition()
            viewModel.listPosition = position
        }
    }

    override fun onBackPressed() {

        if (detailFragment.isVisible) {
            if (!detailFragment.goBack()) viewModel.showList()
        } else {
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    //endregion

    //region fragment initializations

    private fun initializeNewsFragment() {

        newsFragment = NewsListFragment.newInstance(1)
    }

    private fun initializeErrorFragment() {
        errorFragment = ShowErrorFragment.newInstance(
            Error(
                "Error",
                "try again",
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
                viewModel.setUrl(newsItem.siteUrl)
                viewModel.showNewsDetails()

            }

            override fun updateList() {
                // TODO("Not yet implemented")
            }
        })
    }

    private fun setCallbackForErrorFragment() {

        errorFragment.setOnShowErrorFragmentListener(object : OnShowErrorFragmentListener {
            override fun clickedErrorButton(error: Error) {
                viewModel.loadList()
            }
        })
    }

    //endregion

    //region live data observers

    private fun setObservers() {

        viewModel.actualFragment.observe(this) {

            when (it) {
                FragmentsEnum.LOAD_LIST -> {
                    showLoadFragment()
                }
                FragmentsEnum.ERROR -> {
                    showErrorFragment()
                }
                else -> {}
            }
        }

        viewModel.news.observe(this) {

            if (viewModel.actualFragment.value == FragmentsEnum.SHOW_LIST) showNewsList(
                it,
                viewModel.listPosition
            )
        }

        viewModel.newsDetails.observe(this) {

            if (viewModel.actualFragment.value == FragmentsEnum.SHOW_NEWS_DETAILS) {

                if (newsFragment.isVisible) {
                    val position = newsFragment.getFirstVisibleItemPosition()

                    viewModel.listPosition = position
                }
                showNewsDetailsFragment(it)
            }
        }
        viewModel.actionBarTitle.observe(this) { title = getTitle(it) }
    }

    private fun getTitle(fragment: FragmentsEnum): String {
        return when (fragment) {
            FragmentsEnum.LOAD_LIST -> {
                resources.getString(R.string.load_fragment_title)
            }
            FragmentsEnum.SHOW_LIST -> {
                resources.getString(R.string.show_list_fragment_title)
            }
            FragmentsEnum.ERROR -> {
                resources.getString(R.string.error_fragment_title)
            }
            FragmentsEnum.SHOW_NEWS_DETAILS -> {
                resources.getString(R.string.show_details_fragment_title)
            }
        }
    }

    //endregion

    //region show fragments

    private fun showLoadFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.news_fragment, progressFragment).commit()
    }

    private fun showNewsList(list: ArrayList<NewsItem>, position: Int) {

        newsFragment.setBundle(1, list.toTypedArray(), position)

        supportFragmentManager.beginTransaction()
            .replace(R.id.news_fragment, newsFragment).commit()
    }

    private fun showErrorFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.news_fragment, errorFragment).commit()
    }

    private fun showNewsDetailsFragment(url: String) {

        detailFragment.setBundle(url)

        supportFragmentManager.beginTransaction()
            .replace(R.id.news_fragment, detailFragment).commit()
    }

    //endregion
}