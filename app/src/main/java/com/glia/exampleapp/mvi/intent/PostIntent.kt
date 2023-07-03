package com.glia.exampleapp.mvi.intent

sealed interface PostIntent {
    object FetchPosts : PostIntent
}
