package com.glia.widgets.fcm

import androidx.annotation.CallSuper
import com.glia.widgets.di.Dependencies
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Class which is intended to be registered in integrator's application if and only if integrator
 * does not have any FCM services besides Glia's one. In that case integrator should declare this
 * class in his app's manifest:
 *
 * ```xml
 * <manifest xmlns:android="http://schemas.android.com/apk/res/android">
 *     <application ...>
 *
 *         <service
 *             android:name="com.glia.widgets.fcm.GliaFcmService"
 *             android:exported="false">
 *             <intent-filter>
 *                 <action android:name="com.google.firebase.MESSAGING_EVENT" />
 *             </intent-filter>
 *         </service>
 *
 *     </application>
 * </manifest>
 * ```
 */
class GliaFcmService : FirebaseMessagingService() {
    private val pushNotifications by lazy { Dependencies.pushNotifications }

    @CallSuper
    override fun onNewToken(token: String) {
        pushNotifications.updateFcmToken(token)
    }

    @CallSuper
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        pushNotifications.onNewMessage(this, remoteMessage)
    }
}
