package com.dicoding.asclepius.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dicoding.asclepius.data.repository.NewsRepository
import com.dicoding.asclepius.data.response.NewsResponse
import com.dicoding.asclepius.util.Resource
import com.dicoding.asclepius.util.internetConnection
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException


class NewsViewModel(app: Application, val newsRepository: NewsRepository) : AndroidViewModel(app) {
    private val appContext = app

    val news: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var newsPages = 1
    var newsResponse: NewsResponse? = null

    init {
        getNews("id")
    }

    fun getNews(language: String) {
        viewModelScope.launch {
            newsInternet(language)
        }
    }

    private fun handleNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        return if (response.isSuccessful) {
            val resultResponse = response.body()
            if (resultResponse != null) {
                newsPages++
                if (newsResponse == null) {
                    newsResponse = resultResponse
                } else {
                    val oldArticles = newsResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                Resource.Success(newsResponse ?: resultResponse)
            } else {
                Resource.Error("Response body is null")
            }
        } else {
            Resource.Error("Response unsuccessful: ${response.message()}")
        }
    }

    private suspend fun newsInternet(language: String) {
        news.postValue(Resource.Loading())

        try {
            if (internetConnection(appContext)) {
                val response = newsRepository.getNews("kanker", language, 5, newsPages)
                news.postValue(handleNewsResponse(response))
            } else {
                news.postValue(Resource.Error("No Internet Connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> news.postValue(Resource.Error("Network Failure"))
                else -> news.postValue(Resource.Error("Conversion Error: ${t.message}"))
            }
        }
    }
}
