package com.glia.widgets.callvisualizer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.glia.widgets.databinding.EndScreenSharingActivityBinding
import com.glia.widgets.di.Dependencies

class EndScreenSharingActivity : AppCompatActivity(), EndScreenSharingView.OnFinishListener {

    private lateinit var binding: EndScreenSharingActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EndScreenSharingActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val screenSharingView = binding.screenSharingScreenView
        screenSharingView.onFinishListener = this

        val controller = Dependencies.getControllerFactory().endScreenSharingController
        screenSharingView.setController(controller)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.screenSharingScreenView.onDestroy()
    }
}
