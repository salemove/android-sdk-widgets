package com.glia.widgets.filepreview.ui;

import com.glia.widgets.core.engagement.domain.GliaOnEngagementEndUseCase;
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromCacheUseCase;
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromDownloadsUseCase;
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromNetworkUseCase;
import com.glia.widgets.filepreview.domain.usecase.PutImageFileToDownloadsUseCase;
import com.glia.widgets.helper.Logger;

import io.reactivex.disposables.Disposable;

public class FilePreviewController implements FilePreviewContract.Controller, GliaOnEngagementEndUseCase.Listener {
    private static final String TAG = FilePreviewController.class.getSimpleName();

    private FilePreviewContract.View view;
    private State state = new State();

    private final GetImageFileFromDownloadsUseCase getImageFileFromDownloadsUseCase;
    private final GetImageFileFromCacheUseCase getImageFileFromCacheUseCase;
    private final GetImageFileFromNetworkUseCase getImageFileFromNetworkUseCase;
    private final PutImageFileToDownloadsUseCase putImageFileToDownloadsUseCase;
    private final GliaOnEngagementEndUseCase onEngagementEndUseCase;

    private Disposable disposable;

    public FilePreviewController(
            GetImageFileFromDownloadsUseCase getImageFileFromDownloadsUseCase,
            GetImageFileFromCacheUseCase getImageFileFromCacheUseCase,
            GetImageFileFromNetworkUseCase getImageFileFromNetworkUseCase,
            PutImageFileToDownloadsUseCase putImageFileToDownloadsUseCase,
            GliaOnEngagementEndUseCase onEngagementEndUseCase
    ) {
        this.getImageFileFromDownloadsUseCase = getImageFileFromDownloadsUseCase;
        this.getImageFileFromCacheUseCase = getImageFileFromCacheUseCase;
        this.getImageFileFromNetworkUseCase = getImageFileFromNetworkUseCase;
        this.putImageFileToDownloadsUseCase = putImageFileToDownloadsUseCase;
        this.onEngagementEndUseCase = onEngagementEndUseCase;
    }

    public void setView(FilePreviewContract.View view) {
        this.view = view;
        this.state.reset();
        onEngagementEndUseCase.execute(this);
    }

    private synchronized void setState(State state) {
        this.state = state;
        if (view != null) view.onStateUpdated(state);
    }

    @Override
    public void onImageRequested() {
        Logger.d(TAG, "onImageRequested - loadFrom downloads: " + state.getImageIdName());
        setState(state.setImageLoadingFromDownloads());
        disposable = getImageFileFromDownloadsUseCase.execute(state.getImageIdName())
                .subscribe(
                        image -> {
                            Logger.d(TAG, "onImageRequested - loadFrom downloads success" + state.getImageIdName());
                            setState(state.setImageLoadedFromDownloads().setLoadedImage(image));
                        },
                        error -> {
                            Logger.d(TAG, "onImageRequested - loadFrom downloads failed: " + state.getImageIdName());
                            onRequestImageFromCache();
                        }
                );
    }

    private void onRequestImageFromCache() {
        Logger.d(TAG, "onImageRequested - loadFrom cache: " + state.getImageIdName());
        setState(state.setImageLoadingFromCache());
        disposable = getImageFileFromCacheUseCase.execute(state.getImageIdName())
                .subscribe(
                        image -> {
                            Logger.d(TAG, "onImageRequested - loadFrom cache success: " + state.getImageIdName());
                            setState(state.setImageLoadedFromCache().setLoadedImage(image));
                        },
                        error -> {
                            Logger.d(TAG, "onImageRequested - loadFrom cache failed: " + state.getImageIdName());
                            onRequestImageFromNetwork();
                        }
                );
    }

    private void onRequestImageFromNetwork() {
        Logger.d(TAG, "onImageRequested - loadFrom network: " + state.getImageIdName());
        setState(state.setImageLoadingFromNetwork());
        disposable = getImageFileFromNetworkUseCase.execute(state.getImageIdName())
                .subscribe(
                        image -> {
                            Logger.d(TAG, "onImageRequested - loadFrom network success: " + state.getImageIdName());
                            setState(state.setImageLoadedFromNetwork().setLoadedImage(image));
                        },
                        error -> {
                            Logger.d(TAG, "onImageRequested - loadFrom network failed: " + state.getImageIdName());
                            view.showOnImageLoadingFailed();
                            setState(state.setImageLoadingFailure());
                        }
                );
    }

    @Override
    public void onImageDataReceived(String bitmapId, String bitmapName) {
        setState(state.setImageData(bitmapId, bitmapName));
    }


    @Override
    public void onSharePressed() {
        view.shareImageFile(state.getImageIdName());
    }

    @Override
    public void onDownloadPressed() {
        disposable = putImageFileToDownloadsUseCase
                .execute(state.getImageIdName(), state.getLoadedImage())
                .subscribe(
                        () -> {
                            setState(state.setImageLoadedFromDownloads());
                            view.showOnImageSaveSuccess();
                        },
                        error -> view.showOnImageSaveFailed()
                );
    }

    @Override
    public void onDestroy() {
        if (disposable != null) disposable.dispose();
        onEngagementEndUseCase.unregisterListener(this);
    }

    @Override
    public void engagementEnded() {
        Logger.d(TAG, "engagementEndedByOperator");
        view.engagementEnded();
    }
}
