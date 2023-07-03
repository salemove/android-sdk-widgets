package com.glia.exampleapp.mvi.state

import com.glia.exampleapp.mvi.model.ui.Post

sealed interface PostState {
    object Idle : PostState
    object Loading : PostState
    data class Success(val post: Post) : PostState
    data class Error(val throwable: Throwable) : PostState
}
