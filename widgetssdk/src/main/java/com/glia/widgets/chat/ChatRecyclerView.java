package com.glia.widgets.chat;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.RecyclerView;

/**
 * @hide
 */
public class ChatRecyclerView extends RecyclerView {
    private int oldHeight;
    private boolean isInBottom;

    public ChatRecyclerView(Context context) {
        super(context);
    }

    public ChatRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int delta = b - t - this.oldHeight;
        this.oldHeight = b - t;
        if (isInBottom && delta < 0) {
            this.scrollBy(0, -delta);
        }
    }

    public void setInBottom(boolean isInBottom) {
        this.isInBottom = isInBottom;
    }
}
