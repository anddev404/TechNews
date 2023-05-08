package com.anddev404.technews.utils

import com.anddev404.repository.model.SingularNews
import com.anddev404.tech_news_views.newsListFragment.model.NewsItem

class ModelConverter {

    companion object {

        /**
         * Ze względu na to, że aplikacja składa się z oddzielnie kompilowanych modułów,
         * takich jak repozytorium czy widoki, każdy moduł posiada swoją reprezentację obiektów
         * zawierających listę newsów. Z tego powodu niemożliwe jest,
         * aby repozytorium i widok posiadały wspólny model danych.
         * Dlatego lista newsów pobrana z repozytorium (typ List<SingularNews>)
         * musi zostać przekonwertowana na listę newsów (typ ArrayList<NewsItem>),
         * które są reprezentowane w widoku.
         */
        fun singularNewsListToNewsItemList(news: List<SingularNews>): ArrayList<NewsItem> {

            val newsItemList = arrayListOf<NewsItem>()

            for (i in news) {
                newsItemList.add(NewsItem(i.header, i.imageUrl, i.link))
            }
            return newsItemList
        }
    }
}