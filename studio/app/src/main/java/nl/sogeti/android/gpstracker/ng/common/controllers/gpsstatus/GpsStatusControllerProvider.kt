package nl.sogeti.android.gpstracker.ng.common.controllers.gpsstatus

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build

open class GpsStatusControllerProvider {

    open fun createGpsStatusListenerProvider(context: Context, listener: GpsStatusController.Listener): GpsStatusController {
        return createGpsStatusListenerProvider(context, listener, Build.VERSION.SDK_INT)
    }

    @SuppressLint("NewApi")
    @Suppress("DEPRECATION")
    fun createGpsStatusListenerProvider(context: Context, listener: GpsStatusController.Listener, sdkVersion: Int): GpsStatusController {
        val controller: GpsStatusController
        if (sdkVersion >= Build.VERSION_CODES.N) {
            controller = GnnsStatusControllerImpl(context, listener)
        } else {
            controller = GpsStatusControllerImpl(context, listener)
        }

        return controller
    }
}
