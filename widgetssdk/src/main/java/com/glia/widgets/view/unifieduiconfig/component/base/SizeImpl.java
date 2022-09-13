package com.glia.widgets.view.unifieduiconfig.component.base;

public class SizeImpl implements Size.Sp, Size.Dp {
    private final float size;

    public SizeImpl(float size) {
        this.size = size;
    }

    @Override
    public float getSizePx() {
        return size;
    }
}
