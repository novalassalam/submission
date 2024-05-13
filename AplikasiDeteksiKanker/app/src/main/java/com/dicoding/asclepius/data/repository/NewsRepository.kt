package com.dicoding.asclepius.data.repository

import com.dicoding.asclepius.data.retrofit.ApiInstance

class NewsRepository() {
    suspend fun getNews(
        query: String,
        language: String,
        pageSize: Int,
        page: Int,
    ) = ApiInstance.api.getNews(
            query = query,
            language = language,
            pageSize = pageSize,
            page = page
        )
}