package com.glia.widgets.view.unifieduiconfig.component.base;

import androidx.annotation.Dimension;

import com.glia.widgets.view.unifieduiconfig.deserializer.DpDeserializer;
import com.glia.widgets.view.unifieduiconfig.deserializer.SpDeserializer;

/**
 * Represents density independent pixels
 */
public interface Size {

    /**
     * @return size in pixels
     */
    @Dimension
    float getSizePx();

    default int getIntSizePx() {
        return Math.round(getSizePx());
    }

    /**
     * Represents size in Sp (Scalable pixel)
     * Used by deserializer to differ sp from dp
     *
     * @see SpDeserializer
     */
    interface Sp extends Size {
    }

    /**
     * Represents size in Dp (Density independent pixel)
     * Used by deserializer to differ sp from dp
     *
     * @see DpDeserializer
     */
    interface Dp extends Size {
    }
}
