package com.glia.widgets;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.glia.androidsdk.Glia;
import com.glia.androidsdk.locale.LocaleManager;

/**
 * @deprecated Retrieving Widgets locale strings is for SDK internal use only
 * For changing current locale in Widgets SDK use {@link Glia#getLocaleManager()} -> {@link LocaleManager#overrideLocaleCode(String)}
 */
@Deprecated
public interface StringProvider {
    @Deprecated
    String getRemoteString(@StringRes int stringKey, StringKeyPair... values);

    @Deprecated
    void reportImproperInitialisation(@NonNull Exception exception);
}
