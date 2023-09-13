package com.glia.widgets;

import androidx.annotation.StringRes;

public interface StringProvider {
    public String getRemoteString(@StringRes int stringKey, String... values);
}
