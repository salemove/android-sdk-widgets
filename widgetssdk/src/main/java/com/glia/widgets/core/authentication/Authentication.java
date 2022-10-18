package com.glia.widgets.core.authentication;

import androidx.annotation.NonNull;

import com.glia.androidsdk.RequestCallback;
import com.glia.widgets.di.Dependencies;

public class Authentication implements com.glia.androidsdk.visitor.Authentication {
    private final com.glia.androidsdk.visitor.Authentication authentication;

    public Authentication(com.glia.androidsdk.visitor.Authentication authentication) {
        this.authentication = authentication;
    }

    @Override
    public void setBehavior(@NonNull Behavior behavior) {
        authentication.setBehavior(behavior);
    }

    @Override
    public void authenticate(RequestCallback<Void> requestCallback, String jwtToken) {
        Dependencies.getControllerFactory().destroyControllers();
        authentication.authenticate(requestCallback, jwtToken);
    }

    @Override
    public void deauthenticate(RequestCallback<Void> requestCallback) {
        Dependencies.getControllerFactory().destroyControllers();
        authentication.deauthenticate(requestCallback);
    }

    @Override
    public boolean isAuthenticated() {
        return authentication.isAuthenticated();
    }
}
