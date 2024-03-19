package com.glia.widgets;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

/**
 * Retrieving string from custom locales.
 *
 * See [Android Custom Locales guide][https://docs.glia.com/glia-mobile/docs/android-custom-locales] for more details.
 */
public interface StringProvider {
    String getRemoteString(@StringRes int stringKey, StringKeyPair... values);
    void reportImproperInitialisation(@NonNull Exception exception);
}
