package com.glia.widgets.push.notifications

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build

private const val FIREBASE_SERVICE_NAME = "com.google.firebase.messaging.FirebaseMessagingService"
private const val FIREBASE_EVENT = "com.google.firebase.MESSAGING_EVENT"

internal interface IsPushNotificationsSetUpUseCase {
    operator fun invoke(): Boolean
}

internal class IsPushNotificationsSetUpUseCaseImpl(private val applicationContext: Context) : IsPushNotificationsSetUpUseCase {
    private val intent: Intent by lazy {
        Intent(FIREBASE_EVENT).setPackage(applicationContext.packageName)
    }

    private val isFcmDependencyAvailable: Boolean by lazy {
        runCatching { Class.forName(FIREBASE_SERVICE_NAME) }.isSuccess
    }
    private val isFcmServiceDeclaredInManifest: Boolean by lazy {
        intentServices().any { it.serviceInfo.name != FIREBASE_SERVICE_NAME }
    }

    private fun intentServices(): List<ResolveInfo> = applicationContext.run {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentServices(intent, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong()))
        } else {
            packageManager.queryIntentServices(intent, PackageManager.MATCH_ALL)
        }
    }

    override fun invoke(): Boolean = isFcmDependencyAvailable && isFcmServiceDeclaredInManifest

}
