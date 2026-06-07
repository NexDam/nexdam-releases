package it.nexdam.app.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BlogPost(
    val id: String,
    val slug: String,
    val title: String,
    val excerpt: String? = null,
    val category: String? = null,
    @SerialName("cover_url") val coverUrl: String? = null,
    @SerialName("read_minutes") val readMinutes: Int = 0,
    @SerialName("published_at") val publishedAt: String? = null,
    val body: String? = null
)

@Serializable
data class BlogListResponse(val posts: List<BlogPost> = emptyList())

@Serializable
data class BlogDetailResponse(val post: BlogPost? = null)
