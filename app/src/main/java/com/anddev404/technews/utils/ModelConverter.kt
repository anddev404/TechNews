package com.anddev404.technews.utils

import com.anddev404.repository.model.SingularNews
import com.anddev404.tech_news_views.placeholder.NewsItem

class ModelConverter {

    companion object {
        fun SingularNewsListToNewsItemList(news: List<SingularNews>): ArrayList<NewsItem> {

            var newsItemList = arrayListOf<NewsItem>()

            for (i in news) {
                newsItemList.add(NewsItem(i.header, i.imageUrl))
            }
            return newsItemList
        }
    }
}