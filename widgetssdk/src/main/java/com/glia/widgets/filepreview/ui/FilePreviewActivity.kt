package com.glia.widgets.filepreview.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.glia.androidsdk.chat.AttachmentFile
import com.glia.widgets.R
import com.glia.widgets.databinding.FilePreviewActivityBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.fileProviderAuthority
import com.glia.widgets.helper.showToast
import java.io.File

internal class FilePreviewActivity : AppCompatActivity(), FilePreviewContract.View {
    private val binding: FilePreviewActivityBinding by lazy {
        FilePreviewActivityBinding.inflate(
            layoutInflater
        )
    }

    private val hasExternalStoragePermission: Boolean
        get() = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

    private var showDownloadIcon = false
    private var showShareIcon = false

    private var filePreviewController: FilePreviewController? = null

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)

        setController(Dependencies.getControllerFactory().imagePreviewController)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.glia_preview_activity_toolbar_title)

        applyInsets()
        onImageDataReceived(intent)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun onImageDataReceived(intent: Intent) {
        val bitmapId = intent.getStringExtra(IMAGE_ID_KEY).orEmpty()
        val bitmapName = intent.getStringExtra(IMAGE_ID_NAME).orEmpty()
        filePreviewController?.onImageDataReceived(bitmapId, bitmapName)
        filePreviewController?.onImageRequested()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_file_preview, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.save_item).isVisible = showDownloadIcon
        menu.findItem(R.id.share_item).isVisible = showShareIcon
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
        onBackPressed()
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
        filePreviewController?.onDestroy()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == WRITE_PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            filePreviewController?.onDownloadPressed()
        }
    }

    private fun onShareClicked() {
        filePreviewController?.onSharePressed()
    }

    private fun onDownloadClicked() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> filePreviewController?.onDownloadPressed()
            hasExternalStoragePermission -> filePreviewController?.onDownloadPressed()
            else -> requestStoragePermission()
        }
    }

    private fun requestStoragePermission() {
        val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        requestPermissions(permissions, WRITE_PERMISSION_REQUEST_CODE)
    }

    private fun setImageBitmap(loadedImage: Bitmap) {
        binding.filePreviewView.setImageBitmap(loadedImage)
    }

    override fun setController(controller: FilePreviewController) {
        filePreviewController = controller
        controller.setView(this)
    }

    override fun onStateUpdated(state: State) {
        showDownloadIcon = state.isShowDownloadButton
        showShareIcon = state.isShowShareButton
        invalidateOptionsMenu()
        val loadedImage = state.loadedImage
        loadedImage?.let { setImageBitmap(it) }
    }

    override fun shareImageFile(fileName: String) {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .toString(), fileName
        )
        val contentUri = FileProvider.getUriForFile(this, this.fileProviderAuthority, file)
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
        shareIntent.type = "image/jpeg"
        startActivity(shareIntent)
    }

    override fun showOnImageSaveSuccess() {
        showToast(R.string.glia_preview_activity_image_save_success_msg)
    }

    override fun showOnImageSaveFailed() {
        showToast(R.string.glia_preview_activity_image_save_fail_msg)
    }

    override fun showOnImageLoadingFailed() {
        showToast(R.string.glia_preview_activity_preview_failed_msg)
    }

    override fun engagementEnded() {
        finishAfterTransition()
    }

    companion object {
        private const val WRITE_PERMISSION_REQUEST_CODE = 110011
        private const val IMAGE_ID_KEY = "image_id"
        private const val IMAGE_ID_NAME = "image_name"

        fun intent(context: Context, attachment: AttachmentFile): Intent {
            return Intent(context, FilePreviewActivity::class.java)
                .putExtra(IMAGE_ID_KEY, attachment.id)
                .putExtra(IMAGE_ID_NAME, attachment.name)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
    }
}
