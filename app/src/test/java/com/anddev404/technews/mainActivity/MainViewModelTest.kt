package com.anddev404.technews.mainActivity

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.anddev404.repository.Repository
import com.anddev404.repository.model.News
import com.anddev404.repository.model.Response
import com.anddev404.repository.model.SingularNews
import com.anddev404.repository.remote.ApiInterface2
import com.anddev404.tech_news_views.newsListFragment.model.NewsItem
import com.anddev404.technews.utils.InternetInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.fail
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

internal class MainViewModelTest {

    private lateinit var viewModel: MainViewModel

    private lateinit var mockContext: Application
    private lateinit var mockInternetConnection: InternetInterface
    private lateinit var mockApi: ApiInterface2
    private lateinit var mockRepository: Repository

    private lateinit var actualFragmentLiveData: LiveData<FragmentsEnum>
    private lateinit var actualFragmentObserver: Observer<FragmentsEnum>

    private lateinit var newsLiveData: LiveData<ArrayList<NewsItem>>
    private lateinit var newsObserver: Observer<ArrayList<NewsItem>>

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @Before
    fun setup() = runTest {

        mockApi = mock(ApiInterface2::class.java)
        mockRepository = mock(Repository::class.java)
        mockContext = mock(Application::class.java)
        mockInternetConnection = mock(InternetInterface::class.java)

        viewModel = MainViewModel(mockRepository, mockContext, mockInternetConnection)

        `when`(mockInternetConnection.isOnline(mockContext)).thenReturn(true)
        `when`(mockRepository.getApiV2(any())).thenReturn(mockApi)
        `when`(mockApi.getResponse()).thenReturn(getFakeResponse())

        actualFragmentLiveData = viewModel.actualFragment
        newsLiveData = viewModel.news
    }

    @ExperimentalCoroutinesApi
    @Test
    fun getNews() = runTest {
        val testDispatcher = TestCoroutineDispatcher()
        Dispatchers.setMain(testDispatcher)

        actualFragmentObserver = Observer {}
        newsObserver = Observer {
            if (it.size != getFakeResponse().news.value?.news?.size) {
                fail("no response")
            }
        }

        actualFragmentLiveData.observeForever(actualFragmentObserver)
        newsLiveData.observeForever(newsObserver)

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun getFakeResponse(): Response {
        return Response(
            200, MutableLiveData(
                News(
                    arrayListOf(
                        SingularNews("header 1"), SingularNews("header 2")
                    )
                )
            ), MutableLiveData()
        )
    }
}

