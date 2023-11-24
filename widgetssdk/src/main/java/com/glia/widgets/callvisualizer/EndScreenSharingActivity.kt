package com.glia.widgets.callvisualizer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.glia.widgets.databinding.EndScreenSharingActivityBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG

class EndScreenSharingActivity : AppCompatActivity(), EndScreenSharingView.OnFinishListener {

    private lateinit var binding: EndScreenSharingActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.i(TAG, "Create End Screen Sharing screen")
        binding = EndScreenSharingActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val screenSharingView = binding.screenSharingScreenView
        screenSharingView.onFinishListener = this

        val controller = Dependencies.getControllerFactory().endScreenSharingController
        controller.onActivityCreate()
        screenSharingView.setController(controller)
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.i(TAG, "Destroy End Screen Sharing screen")
        binding.screenSharingScreenView.onDestroy()
    }
}
