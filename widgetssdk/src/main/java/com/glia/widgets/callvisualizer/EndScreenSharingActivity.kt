package com.glia.widgets.callvisualizer

import android.os.Bundle
import com.glia.widgets.base.FadeTransitionActivity
import com.glia.widgets.databinding.EndScreenSharingActivityBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG

/**
 * Glia internal class.
 *
 * It will be automatically added to the integrator's manifest file by the manifest merger during compilation.
 *
 * This activity is used only to display screen-sharing controls during
 * ongoing Call Visualizer engagements without video.
 */
internal class EndScreenSharingActivity : FadeTransitionActivity(), EndScreenSharingView.OnFinishListener {

    private lateinit var binding: EndScreenSharingActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.i(TAG, "Create End Screen Sharing screen")
        binding = EndScreenSharingActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val screenSharingView = binding.screenSharingScreenView
        screenSharingView.onFinishListener = this

        val controller = Dependencies.controllerFactory.endScreenSharingController
        controller.onActivityCreate()
        screenSharingView.setController(controller)
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.i(TAG, "Destroy End Screen Sharing screen")
    }
}
