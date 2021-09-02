package com.glia.widgets.filepreview.ui;

import com.glia.widgets.base.BaseController;
import com.glia.widgets.base.BaseView;

public interface FilePreviewContract {
    interface Controller extends BaseController {
        void onSharePressed();

        void onDownloadPressed();

        void onImageRequested();

        void onImageDataReceived(String bitmapId, String bitmapName);
    }

    interface View extends BaseView<FilePreviewController> {
        void onStateUpdated(State state);

        void shareImageFile(String fileName);

        void showOnImageSaveSuccess();

        void showOnImageSaveFailed();

        void showOnImageLoadingFailed();

        void engagementEnded();
    }
}
