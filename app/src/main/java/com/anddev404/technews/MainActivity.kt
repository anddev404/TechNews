package com.anddev404.technews

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.anddev404.repository.Repository
import com.anddev404.tech_news_views.NewsListFragment
import com.anddev404.tech_news_views.OnNewsListFragmentListener
import com.anddev404.tech_news_views.placeholder.NewsItem
import com.anddev404.technews.utils.ModelConverter

class MainActivity : AppCompatActivity() {

    lateinit var viewModel: MainViewModel;
    lateinit var newsFragment: NewsListFragment

    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main);

        viewModel =
            MainViewModel(Repository())//TODO add ViewModelFactory// setViewmModel()

        initializeNewsFragment()
        setCallbackForNewsFragment()

        setNewsObserver()

        viewModel.downloadNews()

    }

    fun initializeNewsFragment() {
        var fragment = supportFragmentManager.findFragmentById(R.id.news_fragment)
        newsFragment = fragment as NewsListFragment
    }

    fun setCallbackForNewsFragment() {

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

    fun setNewsObserver() {
        viewModel.getNews().observe(this, Observer {

            newsFragment.addItems(ModelConverter.SingularNewsListToNewsItemList(it.news))
        })
    }
}