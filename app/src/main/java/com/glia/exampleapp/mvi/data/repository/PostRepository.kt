package com.glia.exampleapp.mvi.data.repository

import com.glia.exampleapp.mvi.domain.FetchPostUseCase
import com.glia.exampleapp.mvi.model.ui.Post
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PostRepository(private val fetchPostUseCase: FetchPostUseCase, private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) {

    suspend fun fetchPost(): Post = withContext(ioDispatcher) {
        fetchPostUseCase()
    }

}
