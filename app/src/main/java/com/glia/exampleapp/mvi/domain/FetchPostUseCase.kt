package com.glia.exampleapp.mvi.domain

import com.glia.exampleapp.mvi.data.datasource.PostDataSource
import com.glia.exampleapp.mvi.model.ui.Post

class FetchPostUseCase(private val dataSource: PostDataSource) {
    suspend operator fun invoke(): Post = dataSource.fetchPost().let {
        Post(it.id, it.body, it.title)
    }
}
