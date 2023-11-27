package com.glia.widgets;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

public interface StringProvider {
    public String getRemoteString(@StringRes int stringKey, StringKeyPair... values);
    public void reportImproperInitialisation(@NonNull Exception exception);
}
