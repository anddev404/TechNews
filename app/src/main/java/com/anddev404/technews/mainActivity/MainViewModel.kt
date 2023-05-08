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
import com.anddev404.technews.utils.Internet
import com.anddev404.technews.utils.ModelConverter
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: Repository,
    private val applicationContext: Application
) : ViewModel() {

    /**
     * Przechowuje informacje o aktualnym fragmencie wyświetlanym w interfejsie użytkownika.
     * Aktualny fragment jest reprezentowany przez klasę enum FragmentsEnum,
     * która definiuje cztery możliwe wartości: LOAD_LIST, ERROR, SHOW_LIST, SHOW_NEWS_DETAILS.
     */
    private val _actualFragment = MutableLiveData(FragmentsEnum.LOAD_LIST)

    /**
     * Publiczne pole LiveData, które umożliwia
     * odczytanie aktualnego fragmentu wyświetlanego w interfejsie użytkownika.
     */
    val actualFragment: LiveData<FragmentsEnum> = _actualFragment

    //region news list
    /**
     * Zmienna typu Int, która przechowuje pozycję na liście newsów,
     * na której użytkownik ostatnio zakończył przeglądanie.
     */
    var listPosition = 0

    /**
     * Zawiera odpowiedź z repozytorium, która obejmuje listę newsów
     * pobranych z internetu przy użyciu modułu technews-repository.
     */
    private val _newsRepository = MutableLiveData<News>()

    /**
     * Ze względu na to, że aplikacja składa się z oddzielnie kompilowanych modułów,
     * takich jak repozytorium czy widoki, każdy moduł posiada swoją reprezentację obiektów
     * zawierających listę newsów. Z tego powodu niemożliwe jest,
     * aby repozytorium i widok posiadały wspólny model danych.
     * Dlatego lista newsów pobrana z repozytorium musi zostać przekonwertowana
     * na listę newsów, która następnie jest przekazywana do widoku za pomocą obiektu typu LiveData.
     */
    private var newsView: LiveData<ArrayList<NewsItem>> =
        Transformations.map(_newsRepository) {
            ModelConverter.singularNewsListToNewsItemList(it.news)
        }

    /**
     * Sprawdza, czy dwa wymagane warunki zostały spełnione przed wyświetleniem listy newsów.
     * Jeśli wartość zmiennej "actualFragment" jest ustawiona na "SHOW_LIST",
     * a zmienna "newsView" zawiera listę newsów,
     * to widok zostaje poinformowany o konieczności wyświetlenia listy newsów.
     * Jeśli lista ładowania jest aktualnie wyświetlana (actualFragment ustawiony na "LOAD_LIST"),
     * rozpoczyna się proces pobierania newsów z internetu.
     */
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

    /**
     * Odpowiedź serwera w przypadku wystąpienia błędu podczas próby pobierania newsów z internetu.
     */
    private val _error = MutableLiveData<ResponseError>()
    val error: LiveData<ResponseError> = _error

    fun loadList() {
        _actualFragment.value = FragmentsEnum.LOAD_LIST
    }

    fun showList() {
        _actualFragment.value = FragmentsEnum.SHOW_LIST
    }

    /**
     * Funkcja ma na celu pobranie newsów z internetu i zaktualizowanie listy, która je przechowuje.

     * Najpierw funkcja sprawdza, czy urządzenie ma połączenie z internetem.
     * Jeśli tak nie jest, funkcja ustawia odpowiedni kod błędu
     * i aktualizuje wartość "_actualFragment" na "FragmentsEnum.ERROR",
     * co oznacza, że aplikacja powinna wyświetlić ekran z informacją o błędzie.
     *
     * Następnie funkcja uzyskuje dostęp do odpowiedniego API za pomocą obiektu "repository",
     * a następnie wykonuje żądanie HTTP, aby pobrać listę newsów.
     *
     * Następnie funkcja uzyskuje dostęp do odpowiedniego API za pomocą obiektu "repository"
     * i wykonuje żądanie HTTP, aby pobrać listę newsów.
     * Jeśli odpowiedź serwera ma kod 200, co oznacza sukces,
     * funkcja ustawia listę newsów w "_newsRepository" na wartość zwróconą przez serwer
     * i ustawia wartość "_actualFragment" na "FragmentsEnum.SHOW_LIST", co oznacza,
     * że aplikacja powinna wyświetlić ekran z listą newsów.
     *
     * Jeśli odpowiedź serwera ma inny kod niż 200,
     * funkcja ustawia odpowiedni kod błędu w "_error" i aktualizuje wartość
     * "_actualFragment" na "FragmentsEnum.ERROR",
     * co oznacza, że aplikacja powinna wyświetlić ekran z informacją o błędzie.
     */
    private fun downloadNews() {

        if (!Internet.isOnline(applicationContext)) {
            _error.postValue(ResponseError.NO_INTERNET_CONNECTION)
            _actualFragment.postValue(FragmentsEnum.ERROR)
            return
        }
        val api = repository.getApiV2(ApiSource.NEWS)
        viewModelScope.launch {

            val response = api.getResponse()
            if (response.responseCode == 200) {

                _newsRepository.postValue(response.news.value)
                _actualFragment.postValue(FragmentsEnum.SHOW_LIST)
            } else {

                _error.postValue(response.error.value)
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

    /**
     * Ta funkcja przechowuje adres URL wybranego przez użytkownika newsa, którego szczegóły chce zobaczyć.
     */
    private val _url: MutableLiveData<String> = MutableLiveData()

    fun setUrl(url: String) {
        _url.value = url
    }

    fun showNewsDetails() {
        _actualFragment.value = FragmentsEnum.SHOW_NEWS_DETAILS
    }

    /**
     * newsDetails typu MediatorLiveData sprawdza, czy zostały spełnione dwa wymagane warunki
     * do wyświetlenia szczegółów danego newsa.
     * Jeśli zmienna "actualFragment" jest ustawiona na "SHOW_NEWS_DETAILS"
     * i zmienna "_url" zawiera poprawny adres URL,
     * funkcja "newsDetails" powiadamia widok o konieczności wyświetlenia szczegółów danego newsa.
     */
    val newsDetails =
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