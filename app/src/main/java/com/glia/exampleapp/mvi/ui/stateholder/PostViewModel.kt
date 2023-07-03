package com.glia.exampleapp.mvi.ui.stateholder

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glia.exampleapp.mvi.data.repository.PostRepository
import com.glia.exampleapp.mvi.intent.PostIntent
import com.glia.exampleapp.mvi.state.PostState
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PostViewModel(private val repository: PostRepository) : ViewModel() {
    private val _state: MutableStateFlow<PostState> = MutableStateFlow(PostState.Idle)
    val state: StateFlow<PostState> = _state.asStateFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        _state.value = PostState.Error(exception)
    }

    fun onNewIntent(postIntent: PostIntent) {
        when (postIntent) {
            PostIntent.FetchPosts -> fetchPost()
        }
    }

    @VisibleForTesting
    fun fetchPost() {
        _state.value = PostState.Loading

        viewModelScope.launch(exceptionHandler) {
            _state.value = PostState.Success(repository.fetchPost())
        }
    }
}
