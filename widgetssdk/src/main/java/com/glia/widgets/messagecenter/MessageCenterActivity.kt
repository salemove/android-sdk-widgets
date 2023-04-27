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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.glia.widgets.GliaWidgets.CHAT_TYPE
import com.glia.widgets.chat.ChatActivity
import com.glia.widgets.chat.ChatType
import com.glia.widgets.core.configuration.GliaSdkConfiguration
import com.glia.widgets.databinding.MessageCenterActivityBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.Utils
import com.glia.widgets.helper.fileProviderAuthority
import com.glia.widgets.helper.fixCapturedPhotoRotation
import java.io.IOException

class MessageCenterActivity : AppCompatActivity(),
        MessageCenterView.OnFinishListener, MessageCenterView.OnNavigateToMessagingListener,
        MessageCenterView.OnAttachFileListener {

    private lateinit var binding: MessageCenterActivityBinding
    private val messageCenterView  get() = binding.messageCenterView
    private var configuration: GliaSdkConfiguration? = null

    private val controller: MessageCenterContract.Controller by lazy {
        Dependencies.getControllerFactory().getMessageCenterController(configuration?.queueId)
    }

    private val getContent = registerForActivityResult(GetContent()) { uri: Uri? ->
        uri?.also {
            controller.onAttachmentReceived(Utils.mapUriToFileAttachment(contentResolver, it))
        }
    }

    private val getImage = registerForActivityResult(TakePicture()) {
        // Handle the returned Uri
        controller.photoCaptureFileUri?.also {
            fixCapturedPhotoRotation(it, this)
            controller.onAttachmentReceived(Utils.mapUriToFileAttachment(contentResolver, it))
        }
    }

    private val getPermission = registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
        // Handle the returned Uri
        if (isGranted) {
            takePhoto()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            val photoFile = Utils.createTempPhotoFile(this)
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

    private fun createConfiguration(intent: Intent): GliaSdkConfiguration? {
        return GliaSdkConfiguration.Builder()
            .intent(intent)
            .build()
    }
}
