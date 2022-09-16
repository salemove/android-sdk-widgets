package com.glia.widgets.view.unifieduiconfig.component.base;

import androidx.annotation.StringDef;

import com.glia.widgets.view.unifieduiconfig.deserializer.AlignmentDeserializer;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Represents Alignment from remote config.
 *
 * @see AlignmentDeserializer
 */
public class Alignment {
    public static final String TYPE_LEADING = "leading";
    public static final String TYPE_CENTER = "center";
    public static final String TYPE_TRAILING = "trailing";

    @StringDef({TYPE_LEADING, TYPE_CENTER, TYPE_TRAILING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
    }

    @Type
    private final String type;

    public Alignment(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
