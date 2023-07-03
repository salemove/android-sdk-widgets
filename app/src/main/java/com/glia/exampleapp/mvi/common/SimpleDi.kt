package com.glia.exampleapp.mvi.common

import com.glia.exampleapp.mvi.data.datasource.PostDataSource
import com.glia.exampleapp.mvi.data.repository.PostRepository
import com.glia.exampleapp.mvi.domain.FetchPostUseCase

object SimpleDi {
    object DataSources {
        val postDataSource: PostDataSource
            get() = PostDataSource()
    }

    object UseCases {
        val fetchPostUseCase: FetchPostUseCase
            get() = FetchPostUseCase(DataSources.postDataSource)
    }

    object Repositories {
        val postRepository: PostRepository
            get() = PostRepository(UseCases.fetchPostUseCase)
    }
}
