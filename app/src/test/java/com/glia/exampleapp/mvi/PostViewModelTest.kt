package com.glia.exampleapp.mvi

import com.glia.exampleapp.mvi.data.repository.PostRepository
import com.glia.exampleapp.mvi.model.ui.Post
import com.glia.exampleapp.mvi.state.PostState
import com.glia.exampleapp.mvi.ui.stateholder.PostViewModel
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PostViewModelTest {
    private val repository: PostRepository = mockk()
    lateinit var viewModel: PostViewModel
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        viewModel = PostViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearMocks(repository)
    }

    @Test
    fun `fetchPost produces Success state when fetching post succeed`() {
        coEvery { repository.fetchPost() } returns Post("", "", "")
        viewModel.fetchPost()
        viewModel.state.value.shouldBeInstanceOf<PostState.Success>()
    }

    @Test
    fun `fetchPost produces Error state when fetching post failed`() {
        coEvery { repository.fetchPost() } throws RuntimeException()
        viewModel.fetchPost()
        viewModel.state.value.shouldBeInstanceOf<PostState.Error>()
    }

    @Test
    fun `fetchPost produces Loading state before fetching the post`() = runTest {
        coEvery { repository.fetchPost() } coAnswers {
            delay(1000)
            Post("", "", "")
        }
        viewModel.fetchPost()
        viewModel.state.value.shouldBeInstanceOf<PostState.Loading>()

    }
}
