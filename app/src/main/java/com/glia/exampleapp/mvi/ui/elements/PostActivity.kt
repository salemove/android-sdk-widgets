package com.glia.exampleapp.mvi.ui.elements

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.glia.exampleapp.databinding.ActivityPostBinding
import com.glia.exampleapp.mvi.common.SimpleDi
import com.glia.exampleapp.mvi.intent.PostIntent
import com.glia.exampleapp.mvi.model.ui.Post
import com.glia.exampleapp.mvi.state.PostState
import com.glia.exampleapp.mvi.ui.stateholder.PostViewModel
import com.glia.exampleapp.mvi.ui.stateholder.PostViewModelFactory
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class PostActivity : AppCompatActivity() {
    private var binding: ActivityPostBinding by Delegates.notNull()

    private val viewModel: PostViewModel by viewModels {
        PostViewModelFactory(SimpleDi.Repositories.postRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        subscribeToState()
        binding.btnFetchPost.setOnClickListener {
            viewModel.onNewIntent(PostIntent.FetchPosts)
        }
    }

    private fun subscribeToState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect {
                    handleState(it)
                }
            }
        }
    }

    private fun handleState(state: PostState): Unit = when (state) {
        is PostState.Error -> showError(state.throwable.message)
        PostState.Idle -> showIdle()
        PostState.Loading -> showLoading()
        is PostState.Success -> showPost(state.post)
    }

    private fun showIdle() {
        binding.textGroup.isVisible = false
        binding.progressBar.isVisible = false
        binding.btnFetchPost.isEnabled = true
    }

    private fun showLoading() {
        binding.textGroup.isVisible = false
        binding.progressBar.isVisible = true
        binding.btnFetchPost.isEnabled = false
    }

    private fun showPost(post: Post) {
        binding.progressBar.isVisible = false
        binding.textGroup.isVisible = true
        binding.btnFetchPost.isEnabled = true
        binding.tvTitle.text = post.title
        binding.tvBody.text = post.body
    }

    private fun showError(message: String?) {
        showIdle()
        Toast.makeText(this, message ?: return, Toast.LENGTH_SHORT).show()
    }
}
