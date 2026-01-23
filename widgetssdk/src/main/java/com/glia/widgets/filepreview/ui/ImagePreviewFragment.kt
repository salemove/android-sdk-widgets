package com.glia.widgets.filepreview.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.glia.widgets.R
import com.glia.widgets.base.GliaFragment
import com.glia.widgets.base.GliaFragmentContract
import com.glia.widgets.databinding.ImagePreviewFragmentBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.FragmentArgumentKeys
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.SimpleWindowInsetsAndAnimationHandler
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.setLocaleContentDescription
import com.glia.widgets.helper.showToast
import com.glia.widgets.launcher.ActivityLauncher

/**
 * Fragment for previewing images shared in chat in full-screen.
 *
 * This Fragment is hosted by [ImagePreviewActivity] which handles Intent-based launches for backwards compatibility.
 *
 * @see ImagePreviewActivity
 * @see ImagePreviewView
 */
internal class ImagePreviewFragment : GliaFragment(), ImagePreviewContract.View, MenuProvider {
    private val activityLauncher: ActivityLauncher by lazy { Dependencies.activityLauncher }
    private val localeProvider = Dependencies.localeProvider

    private var _binding: ImagePreviewFragmentBinding? = null
    private val binding get() = _binding!!

    private var host: GliaFragmentContract.Host? = null

    private val hasExternalStoragePermission: Boolean
        get() = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

    private var showDownloadIcon = false
    private var showShareIcon = false

    private var imagePreviewController: ImagePreviewContract.Controller? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            imagePreviewController?.onDownloadPressed()
        }
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            requireActivity().finishAfterTransition()
        }
    }

    override val gliaView: View
        get() = binding.previewView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setController(Dependencies.controllerFactory.imagePreviewController)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Logger.i(TAG, "Create Image Preview Fragment")
        requireActivity().enableEdgeToEdge()
        _binding = ImagePreviewFragmentBinding.inflate(inflater, container, false)
        SimpleWindowInsetsAndAnimationHandler(binding.root, binding.toolbar)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        host = activity as? GliaFragmentContract.Host

        requireActivity().setTitle(localeProvider.getString(R.string.android_preview_title))

        applyInsets()
        onImageDataReceived()

        (requireActivity() as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        binding.toolbar.setLocaleContentDescription(R.string.android_preview_title)
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        requireActivity().addMenuProvider(this, viewLifecycleOwner)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )
    }

    private fun onImageDataReceived() {
        val args = arguments ?: return

        if (args.containsKey(FragmentArgumentKeys.IMAGE_PREVIEW_LOCAL_IMAGE_URI)) {
            val uri = args.getParcelable<Uri>(FragmentArgumentKeys.IMAGE_PREVIEW_LOCAL_IMAGE_URI)
            uri?.let { imagePreviewController?.onLocalImageReceived(it) }
        } else {
            val bitmapId = args.getString(FragmentArgumentKeys.IMAGE_PREVIEW_IMAGE_ID).orEmpty()
            val bitmapName = args.getString(FragmentArgumentKeys.IMAGE_PREVIEW_IMAGE_NAME).orEmpty()
            imagePreviewController?.onImageDataReceived(bitmapId, bitmapName)
            imagePreviewController?.onImageRequested()
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_file_preview, menu)
    }

    override fun onPrepareMenu(menu: Menu) {
        menu.findItem(R.id.save_item).also {
            it.title = localeProvider.getString(R.string.android_preview_menu_save)
            it.isVisible = showDownloadIcon
        }
        menu.findItem(R.id.share_item).also {
            it.title = localeProvider.getString(R.string.android_preview_menu_share)
            it.isVisible = showShareIcon
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.save_item -> {
                onDownloadClicked()
                true
            }
            R.id.share_item -> {
                onShareClicked()
                true
            }
            android.R.id.home -> {
                requireActivity().finishAfterTransition()
                true
            }
            else -> false
        }
    }

    private fun applyInsets() {
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v: View, insets: WindowInsetsCompat ->
            insets.getInsets(WindowInsetsCompat.Type.systemBars()).run {
                v.setPaddingRelative(left, top, right, bottom)
                WindowInsetsCompat.CONSUMED
            }
        }
    }

    override fun onDestroyView() {
        imagePreviewController?.onDestroy()
        _binding = null
        Logger.i(TAG, "Destroy Image Preview Fragment")
        super.onDestroyView()
    }

    override fun onDetach() {
        super.onDetach()
        host = null
    }

    private fun onShareClicked() {
        imagePreviewController?.onSharePressed()
    }

    private fun onDownloadClicked() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> imagePreviewController?.onDownloadPressed()
            hasExternalStoragePermission -> imagePreviewController?.onDownloadPressed()
            else -> requestStoragePermission()
        }
    }

    private fun requestStoragePermission() {
        requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun setImageBitmap(loadedImage: Bitmap) {
        binding.previewView.setImageBitmap(loadedImage)
    }

    private fun setImageUri(uri: Uri) {
        binding.previewView.setImageURI(uri)
    }

    override fun setController(controller: ImagePreviewContract.Controller) {
        imagePreviewController = controller
        controller.setView(this)
    }

    override fun onStateUpdated(state: State) {
        showDownloadIcon = state.isShowDownloadButton
        showShareIcon = state.isShowShareButton
        requireActivity().invalidateOptionsMenu()
        state.loadedImage?.let { setImageBitmap(it) }
        state.localImageUri?.let { setImageUri(it) }
    }

    override fun shareImageFile(fileName: String) {
        requireActivity().let { activity ->
            activityLauncher.launchShareImage(activity, fileName)
        }
    }

    override fun shareImageFile(uri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        shareIntent.type = "image/jpeg"
        startActivity(shareIntent)
    }

    override fun showOnImageSaveSuccess() {
        requireContext().showToast(localeProvider.getString(R.string.android_preview_save_success))
    }

    override fun showOnImageSaveFailed() {
        requireContext().showToast(localeProvider.getString(R.string.android_preview_save_error))
    }

    override fun showOnImageLoadingFailed() {
        requireContext().showToast(localeProvider.getString(R.string.android_image_preview_fetch_error))
    }

    companion object {
        /**
         * Create a new instance of ImagePreviewFragment with remote attachment.
         *
         * @param imageId The attachment file ID
         * @param imageName The attachment file name
         * @return A new ImagePreviewFragment instance
         */
        fun newInstance(imageId: String, imageName: String): ImagePreviewFragment {
            return ImagePreviewFragment().apply {
                arguments = Bundle().apply {
                    putString(FragmentArgumentKeys.IMAGE_PREVIEW_IMAGE_ID, imageId)
                    putString(FragmentArgumentKeys.IMAGE_PREVIEW_IMAGE_NAME, imageName)
                }
            }
        }

        /**
         * Create a new instance of ImagePreviewFragment with local attachment.
         *
         * @param localImageUri The local image URI
         * @return A new ImagePreviewFragment instance
         */
        fun newInstanceLocal(localImageUri: Uri): ImagePreviewFragment {
            return ImagePreviewFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(FragmentArgumentKeys.IMAGE_PREVIEW_LOCAL_IMAGE_URI, localImageUri)
                }
            }
        }
    }
}
