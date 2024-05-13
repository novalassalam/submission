package com.dicoding.asclepius.data.response

import com.google.gson.annotations.SerializedName

data class NewsResponse(
    @field:SerializedName("articles")
    val articles: MutableList<ArticlesItem>,

    @field:SerializedName("status")
    val status: String,

    @field:SerializedName("totalResults")
    val totalResults: Int
)

data class ArticlesItem(
    @field:SerializedName("source")
    val source: SourceItem,

    @field:SerializedName("author")
    val author: String,

    @field:SerializedName("title")
    val title: String,

    @field:SerializedName("description")
    val description: String,

    @field:SerializedName("url")
    val url: String,

    @field:SerializedName("urlToImage")
    val urlToImage: String,

    @field:SerializedName("publishedAt")
    val publishedAt: String,
)

data class SourceItem(
    @field:SerializedName("name")
    val name: String,
)