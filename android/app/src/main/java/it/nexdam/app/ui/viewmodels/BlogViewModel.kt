package it.nexdam.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.nexdam.app.data.BlogApi
import it.nexdam.app.data.models.BlogPost
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

class BlogViewModel : ViewModel() {
    private val _listState = MutableStateFlow<BlogListState>(BlogListState.Loading)
    val listState: StateFlow<BlogListState> = _listState.asStateFlow()

    private val _detailState = MutableStateFlow<BlogDetailState?>(null)
    val detailState: StateFlow<BlogDetailState?> = _detailState.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    init {
        loadPosts()
    }

    fun loadPosts(category: String? = _selectedCategory.value) {
        _selectedCategory.value = category
        _listState.value = BlogListState.Loading
        viewModelScope.launch {
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
        viewModelScope.launch {
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
}
