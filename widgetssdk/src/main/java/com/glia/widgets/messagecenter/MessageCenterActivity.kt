package com.glia.widgets.messagecenter

import android.Manifest
import android.net.Uri
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import com.glia.widgets.base.FadeTransitionActivity
import com.glia.widgets.chat.Intention
import com.glia.widgets.databinding.MessageCenterActivityBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG

/**
 * This activity is used for displaying the welcome screen for secure messaging.
 *
 * Main features:
 * - Returns an error if the visitor is not authenticated or the specified queue does not support secure messaging.
 * - Allows sending asynchronous messages.
 * - Offers the option to access chat history.
 *
 * Before this activity is launched, make sure that Glia Widgets SDK is set up correctly.
 */
internal class MessageCenterActivity : FadeTransitionActivity(),
    MessageCenterView.OnFinishListener,
    MessageCenterView.OnNavigateToMessagingListener,
    MessageCenterView.OnAttachFileListener {

    private lateinit var binding: MessageCenterActivityBinding
    private val messageCenterView get() = binding.messageCenterView

    private val controller: MessageCenterContract.Controller by lazy {
        Dependencies.controllerFactory.messageCenterController
    }

    private val getContent = registerForActivityResult(OpenDocument()) { uri: Uri? ->
        uri?.also(controller::onContentChosen)
    }

    private val getImage = registerForActivityResult(TakePicture()) { captured ->
        controller.onImageCaptured(captured)
    }

    private val getPermission = registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
        // Handle the returned Uri
        if (isGranted) {
            controller.onTakePhotoClicked()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.i(TAG, "Create Message Center screen")
        binding = MessageCenterActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        messageCenterView.onFinishListener = this
        messageCenterView.onNavigateToMessagingListener = this
        messageCenterView.onAttachFileListener = this

        messageCenterView.setController(controller)
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                messageCenterView.onSystemBack()
                finishAndRemoveTask()
            }
        })

        messageCenterView.initialize()
    }

    override fun onResume() {
        super.onResume()

        messageCenterView.onResume()
    }

    override fun onPause() {
        messageCenterView.onPause()

        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.i(TAG, "Destroy Message Center screen")
    }

    override fun selectAttachmentFile(type: String) {
        getContent.launch(arrayOf(type))
    }

    override fun takePhoto(uri: Uri) {
        getImage.launch(uri)
    }

    override fun requestCameraPermission() {
        getPermission.launch(Manifest.permission.CAMERA)
    }

    override fun navigateToMessaging() {
        Dependencies.activityLauncher.launchChat(this, Intention.SC_CHAT)
        finish()
    }

    override fun returnToLiveChat() {
        Dependencies.activityLauncher.launchChat(this, Intention.RETURN_TO_CHAT)
        finish()
    }

}
