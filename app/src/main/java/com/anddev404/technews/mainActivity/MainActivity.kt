package com.anddev404.technews.mainActivity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
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


/**
 * To jest główna klasa aplikacji, która odpowiada za wyświetlanie użytkownikowi newsów pobranych z internetu.
 */
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

        val viewModelFactory = MainViewModelFactory(Repository(), application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

        initializeNewsFragment()
        initializeErrorFragment()
        initializeDetailsFragment()
        initializeProgressFragment()

        setCallbackForErrorFragment()
        setCallbackForNewsFragment()

        setObservers()

        setupActionBar()
        changeColors(this, resources.getColor(R.color.primary_color, theme))
    }

    override fun onPause() {
        super.onPause()
        savePositionOfList()
    }

    /**
     * Aby przywrócić ostatnie miejsce, w którym użytkownik przeglądał listę newsów
     * po ponownym utworzeniu aktywności (np. po zmianie orientacji ekranu),
     * ta funkcja zapisuje pozycję pierwszego widocznego elementu listy.
     */
    fun savePositionOfList() {
        if (viewModel.actualFragment.value == FragmentsEnum.SHOW_LIST) {

            val position = newsFragment.getFirstVisibleItemPosition()
            viewModel.listPosition = position
        }
    }

    /**
     * Jeśli aktualnie wyświetlany jest fragment NewsDetailsFragment (z wyświetlonymi detalami newsa),
     * przycisk "cofnięcia" (wstecz) powinien przekierować użytkownika z powrotem do listy newsów.
     * W przeciwnym wypadku przycisk wykonuje domyślną akcję.
     */
    override fun onBackPressed() {

        if (detailFragment.isVisible) {
            if (!detailFragment.goBack()) viewModel.showList()
        } else {
            super.onBackPressed()
        }
    }

    /**
     * Ta metoda jest wywoływana po kliknięciu elementów Action Bar/App Bar
     * (strzałka wstecz oraz przycisk menu).
     * Jeśli wybrana zostanie strzałka wstecz, metoda wywołuje onBackPressed().
     * Jeśli natomiast wybrany zostanie element menu odpowiadający za kontakt, metoda wywołuje contactUsIntent().
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
            return true
        }
        if (id == R.id.contact_action_bar || id == R.id.email_action_bar) {
            contactUsIntent()
            return true
        }
        if (id == R.id.policy_privacy_action_bar) {
            openPrivacyPolicy()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun setupActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(com.anddev404.technews.R.menu.action_bar, menu)
        return true
    }

    fun contactUsIntent() {

        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.type = "text/plain"
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("anddev404@gmail.com"))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Tech News")
        emailIntent.putExtra(Intent.EXTRA_TEXT, "")

        startActivity(Intent.createChooser(emailIntent, "Send mail..."))
    }

    fun openPrivacyPolicy() {
        val url = "https://anddev404.github.io/TechNews/privacy_policy.html"

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    //endregion

    //region fragment initializations

    private fun initializeNewsFragment() {

        newsFragment = NewsListFragment.newInstance(1)
    }

    private fun initializeErrorFragment(text: String = "Error", textButton: String = "try again") {
        errorFragment = ShowErrorFragment.newInstance(
            Error(text, textButton)
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

    /**
     * Ustawia callbacki dla fragmentu wyświetlającego listę newsów.
     * Metoda setImage pobiera i wyświetla obrazek powiązany z artykułem z internetu,
     * metoda tapItem wyświetla szczegóły artykułu w nowym fragmencie,
     * a metoda updateList, podczas przewijania listy do końca, pobiera kolejne newsy z API.
     */
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

    /**
     * Ustawia callbacki dla fragmentu wyświetlającego błąd.
     *
     * Metoda <b>clickedErrorButton</b> wywołuje się w momencie kliknięcia przycisku,
     * który służy do ponowienia operacji, która wcześniej zakończyła się błędem.
     */
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

        // Obserwator 'news' obserwuje listę newsów, które są wyświetlane,
        // gdy wartość zmiennej 'actualFragment' jest ustawiona na 'SHOW_LIST'.
        viewModel.news.observe(this) {

            if (viewModel.actualFragment.value == FragmentsEnum.SHOW_LIST) showNewsList(
                it,
                viewModel.listPosition
            )
        }

        // bserwator 'newsDetails' zawiera URL newsa, którego szczegóły są wyświetlane w WebView,
        // gdy wartość zmiennej 'actualFragment' jest ustawiona na 'SHOW_NEWS_DETAILS'.
        viewModel.newsDetails.observe(this) {

            if (viewModel.actualFragment.value == FragmentsEnum.SHOW_NEWS_DETAILS) {

                if (newsFragment.isVisible) {
                    viewModel.listPosition = newsFragment.getFirstVisibleItemPosition()
                }
                showNewsDetailsFragment(it)
            }
        }

        // Wyświetlenie odpowiedniego tekstu we fragmencie ErrorFragment
        // w przypadku wystąpienia problemów z pobieraniem danych.
        viewModel.error.observe(this) {
            errorFragment.setError(Error(it.message()))
        }

        // Aktualizuje tekst w pasku akcji (Action Bar) na podstawie przekazanego parametru.
        viewModel.actionBarTitle.observe(this) { title = getTitle(it) }
    }

    /**
     * Funkcja "getTitle" pozwala na ustawienie tytułu dla ActionBara
     * w zależności od aktualnie wyświetlanego fragmentu w aplikacji.
     * Przyjmuje jako parametr obiekt typu "FragmentsEnum",
     * który określa, jaki fragment jest aktualnie wyświetlany w aplikacji.
     */
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