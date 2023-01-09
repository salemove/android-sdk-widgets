package com.glia.widgets.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.glia.widgets.R;
import com.glia.widgets.core.callvisualizer.domain.VisitorCodeRepository;
import com.glia.widgets.helper.Logger;

public class VisitorCodeView extends FrameLayout {
    private final String TAG = VisitorCodeView.class.getSimpleName();

    private VisitorCodeRepository visitorCodeRepository;

    public VisitorCodeView(@NonNull Context context) {
        this(context, null);
    }

    public VisitorCodeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VisitorCodeView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
//        View view = LayoutInflater.from(getContext()).inflate(R.layout.    );
        // TODO
    }

    public void setVisitorCodeRepository(@NonNull VisitorCodeRepository visitorCodeRepository) {
        this.visitorCodeRepository = visitorCodeRepository;
        loadNewVisitorCode();
    }

    private void loadNewVisitorCode() {
        if (visitorCodeRepository == null) {
            Exception exception = new IllegalStateException("Missing visitor code repository");
            notifyOfError("Internal error, invalid view setup", exception);
        }
    }

    private void notifyOfError(@NonNull String publicErrorMessage, @Nullable Throwable exception) {
        Logger.e(TAG, publicErrorMessage, exception);

        // TODO: show some error inside the view, covered in next tickets
    }
}
