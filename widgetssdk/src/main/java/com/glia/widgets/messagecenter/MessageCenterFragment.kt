package com.glia.widgets.messagecenter

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import com.glia.widgets.base.GliaFragment
import com.glia.widgets.base.GliaFragmentContract
import com.glia.widgets.chat.Intention
import com.glia.widgets.databinding.MessageCenterFragmentBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.FragmentArgumentKeys
import com.glia.widgets.helper.getEnumArgument
import com.glia.widgets.internal.fileupload.PickVisualMediaMultipleMimeTypes
import com.glia.widgets.launcher.ActivityLauncher

/**
 * Fragment for displaying the welcome screen for secure messaging.
 *
 * Features:
 * - Returns an error if the visitor is not authenticated or the specified queue does not support secure messaging.
 * - Allows sending asynchronous messages.
 * - Offers the option to access chat history.
 *
 * This Fragment is hosted by [MessageCenterActivity] which handles Intent-based launches for backwards compatibility.
 *
 * @see MessageCenterActivity
 * @see MessageCenterView
 */
internal class MessageCenterFragment : GliaFragment(),
    MessageCenterView.OnFinishListener,
    MessageCenterView.OnNavigateToMessagingListener,
    MessageCenterView.OnAttachFileListener {

    private val activityLauncher: ActivityLauncher by lazy { Dependencies.activityLauncher }

    private var _binding: MessageCenterFragmentBinding? = null
    private val binding get() = _binding!!

    private val messageCenterView: MessageCenterView
        get() = binding.messageCenterView

    private var host: GliaFragmentContract.Host? = null

    private val controller: MessageCenterContract.Controller by lazy {
        Dependencies.controllerFactory.messageCenterController
    }

    private val pickContentMimeTypes = PickVisualMediaMultipleMimeTypes()
    private val getMediaContent = registerForActivityResult(pickContentMimeTypes) { uri: Uri? ->
        uri?.also(controller::onContentChosen)
    }

    private val getContent = registerForActivityResult(OpenDocument()) { uri: Uri? ->
        uri?.also(controller::onContentChosen)
    }

    private val getImage = registerForActivityResult(TakePicture()) { captured ->
        controller.onImageCaptured(captured)
    }

    private val getPermission = registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            controller.onTakePhotoClicked()
        }
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            messageCenterView.onSystemBack()
            host?.finish()
        }
    }

    override val gliaView: View
        get() = messageCenterView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MessageCenterFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        host = activity as? GliaFragmentContract.Host

        messageCenterView.onFinishListener = this
        messageCenterView.onNavigateToMessagingListener = this
        messageCenterView.onAttachFileListener = this

        messageCenterView.setController(controller)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )

        messageCenterView.initialize()
    }

    override fun onResume() {
        super.onResume()
        messageCenterView.onResume()
    }

    override fun onPause() {
        super.onPause()
        messageCenterView.onPause()
    }

    override fun onDestroyView() {
        controller.onDestroy()
        _binding = null
        super.onDestroyView()
    }

    override fun onDetach() {
        super.onDetach()
        host = null
    }

    override fun selectMediaAttachmentFile(types: List<String>) {
        pickContentMimeTypes.mimeTypes = types
        getMediaContent.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
    }

    override fun selectAttachmentFile(types: List<String>) {
        getContent.launch(types.toTypedArray())
    }

    override fun takePhoto(uri: Uri) {
        getImage.launch(uri)
    }

    override fun requestCameraPermission() {
        getPermission.launch(Manifest.permission.CAMERA)
    }

    override fun navigateToMessaging() {
        requireContext().let { context ->
            activityLauncher.launchChat(context, Intention.SC_CHAT)
            host?.finish()
        }
    }

    override fun returnToLiveChat() {
        requireContext().let { context ->
            activityLauncher.launchChat(context, Intention.RETURN_TO_CHAT)
            host?.finish()
        }
    }

    override fun finish() {
        host?.finish()
    }
}
