package com.glia.exampleapp.mvi.data.datasource

import com.glia.exampleapp.mvi.model.response.PostResponse
import kotlinx.coroutines.delay
import kotlin.random.Random

class PostDataSource {

    suspend fun fetchPost(): PostResponse {
        delay(1_000)
        if (Random.nextBoolean())
            return PostResponse()
        else
            throw RuntimeException("Post is not found")
    }

}
