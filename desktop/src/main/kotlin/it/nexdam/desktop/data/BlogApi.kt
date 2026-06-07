package it.nexdam.desktop.data

import it.nexdam.desktop.data.models.BlogDetailResponse
import it.nexdam.desktop.data.models.BlogListResponse
import it.nexdam.desktop.data.models.BlogPost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

private val json = Json { ignoreUnknownKeys = true }

object BlogApi {
    private const val BASE = "https://www.nexdam.it/api/blog"

    suspend fun fetchPosts(category: String? = null): List<BlogPost> = withContext(Dispatchers.IO) {
        val urlStr = if (category.isNullOrBlank()) {
            BASE
        } else {
            "$BASE?category=${URLEncoder.encode(category, "UTF-8")}"
        }
        val raw = get(urlStr)
        json.decodeFromString(BlogListResponse.serializer(), raw).posts
    }

    suspend fun fetchPost(slug: String): BlogPost? = withContext(Dispatchers.IO) {
        val urlStr = "$BASE?slug=${URLEncoder.encode(slug, "UTF-8")}"
        val raw = get(urlStr)
        json.decodeFromString(BlogDetailResponse.serializer(), raw).post
    }

    private fun get(urlStr: String): String {
        val connection = URL(urlStr).openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")
            connection.connectTimeout = 15_000
            connection.readTimeout = 15_000
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
}
