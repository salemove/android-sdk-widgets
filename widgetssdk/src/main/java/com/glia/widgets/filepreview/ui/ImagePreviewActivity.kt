package com.glia.widgets.filepreview.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.glia.widgets.R
import com.glia.widgets.databinding.ImagePreviewActivityBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.ExtraKeys
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.SimpleWindowInsetsAndAnimationHandler
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.getParcelable
import com.glia.widgets.helper.setLocaleContentDescription
import com.glia.widgets.helper.showToast
import com.glia.widgets.launcher.ActivityLauncher

private const val WRITE_PERMISSION_REQUEST_CODE = 110011

/**
 * Glia internal class.
 *
 * It will be automatically added to the integrator's manifest file by the manifest merger during compilation.
 *
 * This activity is used to preview images shared in chat in full-screen.
 */
internal class ImagePreviewActivity : AppCompatActivity(), ImagePreviewContract.View {
    private val activityLauncher: ActivityLauncher by lazy { Dependencies.activityLauncher }
    private val localeProvider = Dependencies.localeProvider

    private val binding: ImagePreviewActivityBinding by lazy { ImagePreviewActivityBinding.inflate(layoutInflater) }

    private val hasExternalStoragePermission: Boolean
        get() = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private var showDownloadIcon = false
    private var showShareIcon = false

    private var imagePreviewController: ImagePreviewContract.Controller? = null

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)

        setController(Dependencies.controllerFactory.imagePreviewController)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        SimpleWindowInsetsAndAnimationHandler(binding.root, binding.toolbar)
        Logger.i(TAG, "Create Image Preview screen")
        setContentView(binding.root)
        title = localeProvider.getString(R.string.android_preview_title)

        applyInsets()
        onImageDataReceived(intent)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setLocaleContentDescription(R.string.android_preview_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun onImageDataReceived(intent: Intent) {
        if (intent.hasExtra(ExtraKeys.IMAGE_PREVIEW_LOCAL_IMAGE_URI)) {
            imagePreviewController?.onLocalImageReceived(intent.getParcelable<Uri>(ExtraKeys.IMAGE_PREVIEW_LOCAL_IMAGE_URI) ?: return)
        } else {
            val bitmapId = intent.getStringExtra(ExtraKeys.IMAGE_PREVIEW_IMAGE_ID).orEmpty()
            val bitmapName = intent.getStringExtra(ExtraKeys.IMAGE_PREVIEW_IMAGE_NAME).orEmpty()
            imagePreviewController?.onImageDataReceived(bitmapId, bitmapName)
            imagePreviewController?.onImageRequested()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_file_preview, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.save_item).also {
            it.title = localeProvider.getString(R.string.android_preview_menu_save)
            it.isVisible = showDownloadIcon
        }
        menu.findItem(R.id.share_item).also {
            it.title = localeProvider.getString(R.string.android_preview_menu_share)
            it.isVisible = showShareIcon
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save_item -> onDownloadClicked()
            R.id.share_item -> onShareClicked()
            else -> return false
        }

        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun applyInsets() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v: View, insets: WindowInsetsCompat ->
            insets.getInsets(WindowInsetsCompat.Type.systemBars()).run {
                v.setPaddingRelative(left, top, right, bottom)
                WindowInsetsCompat.CONSUMED
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.i(TAG, "Destroy Image Preview screen")
        imagePreviewController?.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == WRITE_PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            imagePreviewController?.onDownloadPressed()
        }
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
        val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        requestPermissions(permissions, WRITE_PERMISSION_REQUEST_CODE)
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
        invalidateOptionsMenu()
        state.loadedImage?.let { setImageBitmap(it) }
        state.localImageUri?.let { setImageUri(it) }
    }

    override fun shareImageFile(fileName: String) {
        activityLauncher.launchShareImage(this, fileName)
    }

    override fun shareImageFile(uri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        shareIntent.type = "image/jpeg"
        startActivity(shareIntent)
    }

    override fun showOnImageSaveSuccess() {
        showToast(localeProvider.getString(R.string.android_preview_save_success))
    }

    override fun showOnImageSaveFailed() {
        showToast(localeProvider.getString(R.string.android_preview_save_error))
    }

    override fun showOnImageLoadingFailed() {
        showToast(localeProvider.getString(R.string.android_image_preview_fetch_error))
    }

    override fun engagementEnded() {
        finishAfterTransition()
    }
}
