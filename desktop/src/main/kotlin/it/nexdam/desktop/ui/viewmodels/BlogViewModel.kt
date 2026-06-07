package it.nexdam.desktop.ui.viewmodels

import it.nexdam.desktop.data.BlogApi
import it.nexdam.desktop.data.models.BlogPost
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class BlogListState {
    object Loading : BlogListState()
    data class Success(val posts: List<BlogPost>) : BlogListState()
    data class Error(val message: String) : BlogListState()
}

sealed class BlogDetailState {
    object Loading : BlogDetailState()
    data class Success(val post: BlogPost) : BlogDetailState()
    data class Error(val message: String) : BlogDetailState()
}

class BlogViewModel {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _listState = MutableStateFlow<BlogListState>(BlogListState.Loading)
    val listState: StateFlow<BlogListState> = _listState

    private val _detailState = MutableStateFlow<BlogDetailState?>(null)
    val detailState: StateFlow<BlogDetailState?> = _detailState

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory

    init {
        loadPosts()
    }

    fun loadPosts(category: String? = _selectedCategory.value) {
        _selectedCategory.value = category
        _listState.value = BlogListState.Loading
        scope.launch {
            try {
                val posts = BlogApi.fetchPosts(category)
                _listState.value = BlogListState.Success(posts)
            } catch (e: Exception) {
                _listState.value = BlogListState.Error(e.message ?: "Impossibile caricare gli articoli")
            }
        }
    }

    fun openPost(slug: String) {
        _detailState.value = BlogDetailState.Loading
        scope.launch {
            try {
                val post = BlogApi.fetchPost(slug)
                _detailState.value = if (post != null) BlogDetailState.Success(post)
                else BlogDetailState.Error("Articolo non trovato")
            } catch (e: Exception) {
                _detailState.value = BlogDetailState.Error(e.message ?: "Impossibile caricare l'articolo")
            }
        }
    }

    fun closePost() {
        _detailState.value = null
    }

    fun reset() {
        _detailState.value = null
        _selectedCategory.value = null
    }
}
