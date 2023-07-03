package com.glia.exampleapp.mvi.ui.stateholder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.glia.exampleapp.mvi.data.repository.PostRepository

/**
 * Ideally, this should be done by a DI framework
 */
class PostViewModelFactory(private val repository: PostRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(PostViewModel::class.java) -> PostViewModel(repository) as T
        else -> throw IllegalArgumentException("Unknown ViewModel class")
    }
}
