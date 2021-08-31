package com.glia.widgets.filepreview.ui;

import android.graphics.Bitmap;

class State {
    private final ImageLoadingState imageLoadingState;
    private final boolean isShowShareButton;
    private final boolean isShowDownloadButton;
    private final Bitmap loadedImage;

    private final String imageIdName;
    private final String imageName;
    private final String imageId;

    public State() {
        this.imageLoadingState = ImageLoadingState.INITIAL;
        this.isShowShareButton = false;
        this.isShowDownloadButton = false;
        this.loadedImage = null;
        this.imageIdName = "";
        this.imageName = "";
        this.imageId = "";
    }

    public State(
            ImageLoadingState imageLoadingState,
            boolean isShowShareButton,
            boolean isShowDownloadButton,
            Bitmap loadedImage,
            String imageId,
            String imageName,
            String imageIdName
    ) {
        this.imageLoadingState = imageLoadingState;
        this.isShowShareButton = isShowShareButton;
        this.isShowDownloadButton = isShowDownloadButton;
        this.loadedImage = loadedImage;
        this.imageId = imageId;
        this.imageName = imageName;
        this.imageIdName = imageIdName;
    }

    public ImageLoadingState getImageLoadingState() {
        return this.imageLoadingState;
    }

    public boolean getIsShowShareButton() {
        return this.isShowShareButton;
    }

    public boolean getIsShowDownloadButton() {
        return this.isShowDownloadButton;
    }

    public Bitmap getLoadedImage() {
        return this.loadedImage;
    }

    public String getImageIdName() {
        return this.imageIdName;
    }

    public String getImageName() {
        return this.imageName;
    }

    public String getImageId() {
        return this.imageId;
    }

    public State setImageLoadingFromCache() {
        return new StateBuilder()
                .copyFrom(this)
                .setImageLoadingState(State.ImageLoadingState.LOADING_FROM_CACHE)
                .setIsShowShareButton(false)
                .setIsShowDownloadButton(false)
                .build();
    }

    public State setImageLoadingFromDownloads() {
        return new StateBuilder()
                .copyFrom(this)
                .setImageLoadingState(ImageLoadingState.LOADING_FROM_DOWNLOADS)
                .setIsShowShareButton(false)
                .setIsShowDownloadButton(false)
                .build();
    }

    public State setImageLoadingFromNetwork() {
        return new StateBuilder()
                .copyFrom(this)
                .setImageLoadingState(ImageLoadingState.LOADING_FROM_NETWORK)
                .setIsShowShareButton(false)
                .setIsShowDownloadButton(false)
                .build();
    }

    public State setImageLoadedFromNetwork() {
        return new StateBuilder()
                .copyFrom(this)
                .setImageLoadingState(ImageLoadingState.SUCCESS_FROM_NETWORK)
                .setIsShowShareButton(false)
                .setIsShowDownloadButton(true)
                .build();
    }

    public State setImageLoadedFromCache() {
        return new StateBuilder()
                .copyFrom(this)
                .setImageLoadingState(ImageLoadingState.SUCCESS_FROM_CACHE)
                .setIsShowShareButton(false)
                .setIsShowDownloadButton(true)
                .build();
    }

    public State setImageLoadedFromDownloads() {
        return new StateBuilder()
                .copyFrom(this)
                .setImageLoadingState(ImageLoadingState.SUCCESS_FROM_DOWNLOADS)
                .setIsShowShareButton(true)
                .setIsShowDownloadButton(false)
                .build();
    }

    public State setImageData(String id, String name) {
        return new StateBuilder()
                .copyFrom(this)
                .setImageName(id)
                .setImageId(name)
                .setImageIdName(id + "." + name)
                .build();
    }

    public State setLoadedImage(Bitmap image) {
        return new StateBuilder()
                .copyFrom(this)
                .setLoadedImage(image)
                .build();
    }

    public State setImageLoadingFailure() {
        return new StateBuilder()
                .copyFrom(this)
                .setImageLoadingState(ImageLoadingState.FAILURE)
                .build();
    }

    public State setImageSaved() {
        return new StateBuilder()
                .copyFrom(this)
                .setIsShowShareButton(true)
                .setIsShowDownloadButton(false)
                .build();
    }

    public State reset() {
        return new StateBuilder()
                .copyFrom(new State())
                .build();
    }

    protected enum ImageLoadingState {
        INITIAL,
        LOADING_FROM_DOWNLOADS,
        LOADING_FROM_CACHE,
        LOADING_FROM_NETWORK,
        SUCCESS_FROM_DOWNLOADS,
        SUCCESS_FROM_CACHE,
        SUCCESS_FROM_NETWORK,
        FAILURE
    }

    private static class StateBuilder {
        private State.ImageLoadingState imageLoadingState;
        private boolean isShowShareButton;
        private boolean isShowDownloadButton;
        private Bitmap loadedImage;
        private String imageName;
        private String imageId;
        private String imageIdName;

        public StateBuilder setImageLoadingState(State.ImageLoadingState imageLoadingState) {
            this.imageLoadingState = imageLoadingState;
            return this;
        }

        public StateBuilder setIsShowShareButton(boolean isShowShareButton) {
            this.isShowShareButton = isShowShareButton;
            return this;
        }

        public StateBuilder setIsShowDownloadButton(boolean isShowDownloadButton) {
            this.isShowDownloadButton = isShowDownloadButton;
            return this;
        }

        public StateBuilder setLoadedImage(Bitmap bitmap) {
            this.loadedImage = bitmap;
            return this;
        }

        public StateBuilder setImageName(String imageName) {
            this.imageName = imageName;
            return this;
        }

        public StateBuilder setImageId(String imageId) {
            this.imageId = imageId;
            return this;
        }

        public StateBuilder setImageIdName(String imageIdName) {
            this.imageIdName = imageIdName;
            return this;
        }

        public State build() {
            return new State(
                    imageLoadingState,
                    isShowShareButton,
                    isShowDownloadButton,
                    loadedImage,
                    imageId,
                    imageName,
                    imageIdName
            );
        }

        private StateBuilder copyFrom(State state) {
            imageLoadingState = state.getImageLoadingState();
            isShowShareButton = state.getIsShowShareButton();
            isShowDownloadButton = state.getIsShowDownloadButton();
            loadedImage = state.getLoadedImage();
            imageName = state.getImageName();
            imageId = state.getImageId();
            imageIdName = state.getImageIdName();
            return this;
        }
    }
}
