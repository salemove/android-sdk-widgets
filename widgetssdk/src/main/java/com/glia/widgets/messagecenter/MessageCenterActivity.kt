package com.glia.widgets.messagecenter

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.glia.widgets.GliaWidgets.CHAT_TYPE
import com.glia.widgets.base.FadeTransitionActivity
import com.glia.widgets.chat.ChatActivity
import com.glia.widgets.chat.ChatType
import com.glia.widgets.core.configuration.GliaSdkConfiguration
import com.glia.widgets.databinding.MessageCenterActivityBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.createTempPhotoFile
import com.glia.widgets.helper.fileProviderAuthority
import com.glia.widgets.helper.fixCapturedPhotoRotation
import com.glia.widgets.helper.mapUriToFileAttachment
import java.io.IOException

/**
 * This activity is used for displaying the welcome screen for secure conversations.
 *
 * Main features:
 * - Returns an error if the visitor is not authenticated or the specified queue does not support secure conversations.
 * - Allows sending asynchronous messages.
 * - Offers the option to access chat history.
 *
 * Before this activity is launched, make sure that Glia Widgets SDK is set up correctly.
 *
 * Required data that should be passed together with the Activity intent:
 * - {@link GliaWidgets#QUEUE_ID}: ID of the queue you would like to use for your engagements.
 * For a full list of optional parameters, see the constants defined in {@link GliaWidgets}.
 *
 * Code example:
 * ```
 * Intent intent = new Intent(requireContext(), MessageCenterActivity.class);
 * intent.putExtra(GliaWidgets.QUEUE_ID, "MESSAGING_QUEUE_ID");
 * startActivity(intent);
 * ```
 */
class MessageCenterActivity :
    FadeTransitionActivity(),
    MessageCenterView.OnFinishListener,
    MessageCenterView.OnNavigateToMessagingListener,
    MessageCenterView.OnAttachFileListener {

    private lateinit var binding: MessageCenterActivityBinding
    private val messageCenterView get() = binding.messageCenterView
    private var configuration: GliaSdkConfiguration? = null

    private val controller: MessageCenterContract.Controller by lazy {
        Dependencies.getControllerFactory().getMessageCenterController(configuration?.queueId)
    }

    private val getContent = registerForActivityResult(GetContent()) { uri: Uri? ->
        uri?.also {
            controller.onAttachmentReceived(
                mapUriToFileAttachment(contentResolver, it) ?: return@also
            )
        }
    }

    private val getImage = registerForActivityResult(TakePicture()) { captured ->
        if (captured) {
            // Handle the returned Uri
            controller.photoCaptureFileUri?.also {
                fixCapturedPhotoRotation(it, this)
                controller.onAttachmentReceived(
                    mapUriToFileAttachment(contentResolver, it) ?: return@also
                )
            }
        }
    }

    private val getPermission =
        registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
            // Handle the returned Uri
            if (isGranted) {
                takePhoto()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.i(TAG, "Create Message Center screen")
        binding = MessageCenterActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configuration = createConfiguration(intent)

        messageCenterView.onFinishListener = this
        messageCenterView.onNavigateToMessagingListener = this
        messageCenterView.onAttachFileListener = this

        messageCenterView.setController(controller)
        messageCenterView.setConfiguration(configuration)
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                messageCenterView.onSystemBack()
                finishAndRemoveTask()
            }
        })
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
        getContent.launch(type)
    }

    override fun takePhoto() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            getPermission.launch(Manifest.permission.CAMERA)
            return
        }

        try {
            val photoFile = createTempPhotoFile(this)
            val uri = FileProvider.getUriForFile(
                this,
                fileProviderAuthority,
                photoFile
            )
            controller.photoCaptureFileUri = uri
            getImage.launch(uri)
        } catch (exception: IOException) {
            Logger.e(TAG, "Create photo file failed: " + exception.message)
        }
    }

    override fun navigateToMessaging() {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtras(getIntent())
        intent.putExtra(CHAT_TYPE, ChatType.SECURE_MESSAGING as Parcelable)
        startActivity(intent)
        finish()
    }

    private fun createConfiguration(intent: Intent): GliaSdkConfiguration {
        return GliaSdkConfiguration.Builder()
            .intent(intent)
            .build()
    }
}
