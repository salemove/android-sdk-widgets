package com.glia.widgets;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

public interface StringProvider {
    String getRemoteString(@StringRes int stringKey, StringKeyPair... values);
    void reportImproperInitialisation(@NonNull Exception exception);
}
