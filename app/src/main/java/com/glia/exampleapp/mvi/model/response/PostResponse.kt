package com.glia.exampleapp.mvi.model.response

import java.util.UUID
import kotlin.random.Random
import kotlin.random.nextUInt

data class PostResponse(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Title N ${Random.nextUInt(100u)}",
    val body: String = "Body N ${Random.nextUInt(100u)}"
)
