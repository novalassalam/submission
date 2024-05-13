package com.dicoding.asclepius.data.retrofit

import com.dicoding.asclepius.BuildConfig
import com.dicoding.asclepius.data.response.NewsResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("everything")
    suspend fun getNews(
        @Query("q") query: String = "kanker",
        @Query("apiKey") apiKey: String = BuildConfig.NEWS_API_KEY,
        @Query("language") language: String = "id",
        @Query("pageSize") pageSize: Int = 5,
        @Query("page") page: Int = 1
    ): Response<NewsResponse>
}