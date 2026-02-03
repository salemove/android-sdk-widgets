package com.glia.widgets.filepreview.ui

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.BundleCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.glia.widgets.HostActivity
import com.glia.widgets.R
import com.glia.widgets.base.BaseDialogFragment
import com.glia.widgets.databinding.FragmentImagePreviewBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.SimpleWindowInsetsAndAnimationHandler
import com.glia.widgets.helper.setLocaleContentDescription
import com.glia.widgets.helper.showToast

private const val WRITE_PERMISSION_REQUEST_CODE = 110011

/**
 * DialogFragment for previewing images in full-screen.
 *
 * Displayed as a full-screen dialog over the current content.
 * Handles both remote images (loaded from cache/downloads) and local images (from Uri).
 */
internal class ImagePreviewDialogFragment :
    BaseDialogFragment<ImagePreviewUiState, ImagePreviewEffect, ImagePreviewViewModel>() {

    internal companion object {
        internal const val ARG_IMAGE_ID = "arg_image_id"
        internal const val ARG_IMAGE_NAME = "arg_image_name"
        internal const val ARG_LOCAL_IMAGE_URI = "arg_local_image_uri"
    }

    private var _binding: FragmentImagePreviewBinding? = null
    private val binding get() = _binding!!

    override val viewModel: ImagePreviewViewModel by viewModels { Dependencies.viewModelFactory }
    private val localeProvider by lazy { Dependencies.localeProvider }
    private val activityLauncher by lazy { Dependencies.activityLauncher }

    private var showDownloadIcon: Boolean = false
    private var showShareIcon: Boolean = false

    private val hasExternalStoragePermission: Boolean
        get() = context?.let {
            ContextCompat.checkSelfPermission(it, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        } ?: false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Application_Glia_Activity_Style)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.apply {
            requestFeature(Window.FEATURE_NO_TITLE)
            setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImagePreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initializeFromArguments()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun initializeFromArguments() {
        arguments?.let { args ->
            val localImageUri = args.getString(ARG_LOCAL_IMAGE_URI)
            if (localImageUri != null) {
                viewModel.processIntent(ImagePreviewIntent.InitializeWithLocalImage(Uri.parse(localImageUri)))
            } else {
                val imageId = args.getString(ARG_IMAGE_ID).orEmpty()
                val imageName = args.getString(ARG_IMAGE_NAME).orEmpty()
                viewModel.processIntent(ImagePreviewIntent.InitializeWithRemoteImage(imageId, imageName))
                viewModel.processIntent(ImagePreviewIntent.LoadImage)
            }
        }
    }

    override fun setupViews() {
        setupToolbar()
        setupMenu()
        applyInsets()
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            title = localeProvider.getString(R.string.android_preview_title)
            setLocaleContentDescription(R.string.android_preview_title)
            setNavigationOnClickListener {
                viewModel.processIntent(ImagePreviewIntent.CloseScreen)
            }
        }

        // Set up the toolbar as action bar for menu support
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_file_preview, menu)
            }

            override fun onPrepareMenu(menu: Menu) {
                menu.findItem(R.id.save_item)?.also {
                    it.title = localeProvider.getString(R.string.android_preview_menu_save)
                    it.isVisible = showDownloadIcon
                }
                menu.findItem(R.id.share_item)?.also {
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
                        viewModel.processIntent(ImagePreviewIntent.ShareImage)
                        true
                    }
                    android.R.id.home -> {
                        viewModel.processIntent(ImagePreviewIntent.CloseScreen)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun applyInsets() {
        dialog?.window?.let { window ->
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
        SimpleWindowInsetsAndAnimationHandler(binding.root, binding.toolbar)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v: View, insets: WindowInsetsCompat ->
            insets.getInsets(WindowInsetsCompat.Type.systemBars()).run {
                v.setPaddingRelative(left, top, right, bottom)
                WindowInsetsCompat.CONSUMED
            }
        }
    }

    private fun onDownloadClicked() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                viewModel.processIntent(ImagePreviewIntent.DownloadImage)
            }
            hasExternalStoragePermission -> {
                viewModel.processIntent(ImagePreviewIntent.DownloadImage)
            }
            else -> requestStoragePermission()
        }
    }

    private fun requestStoragePermission() {
        val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        requestPermissions(permissions, WRITE_PERMISSION_REQUEST_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == WRITE_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            viewModel.processIntent(ImagePreviewIntent.DownloadImage)
        }
    }

    override fun handleState(state: ImagePreviewUiState) {
        showDownloadIcon = state.isShowDownloadButton
        showShareIcon = state.isShowShareButton
        activity?.invalidateOptionsMenu()

        state.loadedBitmap?.let { bitmap ->
            binding.previewView.setImageBitmap(bitmap)
        }
        state.localImageUri?.let { uri ->
            binding.previewView.setImageURI(uri)
        }
    }

    override fun handleEffect(effect: ImagePreviewEffect) {
        when (effect) {
            ImagePreviewEffect.Dismiss -> {
                dismissAllowingStateLoss()
                (activity as? HostActivity)?.finishIfEmpty()
            }
            is ImagePreviewEffect.ShareRemoteImage -> {
                activity?.let { activityLauncher.launchShareImage(it, effect.fileName) }
            }
            is ImagePreviewEffect.ShareLocalImage -> {
                shareLocalImage(effect.uri)
            }
            ImagePreviewEffect.ShowImageSaveSuccess -> {
                context?.showToast(localeProvider.getString(R.string.android_preview_save_success))
            }
            ImagePreviewEffect.ShowImageSaveFailed -> {
                context?.showToast(localeProvider.getString(R.string.android_preview_save_error))
            }
            ImagePreviewEffect.ShowImageLoadingFailed -> {
                context?.showToast(localeProvider.getString(R.string.android_image_preview_fetch_error))
            }
        }
    }

    private fun shareLocalImage(uri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "image/jpeg"
        }
        startActivity(shareIntent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
