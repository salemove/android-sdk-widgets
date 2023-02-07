package com.glia.widgets.callvisualizer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.glia.widgets.databinding.ScreenSharingScreenActivityBinding
import com.glia.widgets.di.Dependencies

class ScreenSharingScreenActivity : AppCompatActivity(), ScreenSharingScreenView.OnFinishListener {

    private lateinit var binding: ScreenSharingScreenActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ScreenSharingScreenActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val screenSharingView = binding.screenSharingScreenView
        screenSharingView.onFinishListener = this

        val controller = Dependencies.getControllerFactory().screenSharingViewController
        screenSharingView.setController(controller)
    }
}
