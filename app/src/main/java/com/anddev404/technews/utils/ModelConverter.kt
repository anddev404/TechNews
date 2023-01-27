package com.anddev404.technews.utils

import com.anddev404.repository.model.SingularNews
import com.anddev404.tech_news_views.newsListFragment.model.NewsItem

class ModelConverter {

    companion object {
        fun singularNewsListToNewsItemList(news: List<SingularNews>): ArrayList<NewsItem> {

            val newsItemList = arrayListOf<NewsItem>()

            for (i in news) {
                newsItemList.add(NewsItem(i.header, i.imageUrl, i.link))
            }
            return newsItemList
        }
    }
}