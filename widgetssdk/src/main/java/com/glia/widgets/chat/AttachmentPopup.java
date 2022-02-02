package com.glia.widgets.chat;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;

import com.glia.widgets.R;

public class AttachmentPopup {

    public static void show(final View anchor, Callback callback) {
        View layout = inflateLayout(anchor.getContext());
        PopupWindow popupWindow = createPopupWindow(anchor, layout);
        attachCallback(popupWindow, callback);
    }

    @NonNull
    private static View inflateLayout(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.chat_attachment_popup, null);
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        return view;
    }

    @NonNull
    private static PopupWindow createPopupWindow(View anchor, View popupView) {
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                popupView.getMeasuredWidth(),
                popupView.getMeasuredHeight(),
                true
        );
        popupWindow.setAnimationStyle(R.style.Animation_AppCompat_Dialog);
        int margin = anchor.getContext().getResources()
                .getDimensionPixelSize(R.dimen.glia_chat_attachment_menu_margin);
        popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
        popupWindow.showAsDropDown(anchor, -margin, -margin, Gravity.END);

        return popupWindow;
    }

    private static void attachCallback(PopupWindow popupWindow, Callback callback) {
        View view = popupWindow.getContentView();
        view.findViewById(R.id.photo_library_item).setOnClickListener(v -> {
            popupWindow.dismiss();
            callback.onGalleryClicked();
        });
        view.findViewById(R.id.photo_or_video_item).setOnClickListener(v -> {
            popupWindow.dismiss();
            callback.onTakePhotoClicked();
        });
        view.findViewById(R.id.browse_item).setOnClickListener(v -> {
            popupWindow.dismiss();
            callback.onBrowseClicked();
        });
    }

    public interface Callback {
        void onGalleryClicked();

        void onTakePhotoClicked();

        void onBrowseClicked();
    }
}
