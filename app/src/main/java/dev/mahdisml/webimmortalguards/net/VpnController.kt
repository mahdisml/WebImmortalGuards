package dev.mahdisml.webimmortalguards.net

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dev.mahdisml.webimmortalguards.Strings.package_name
import dev.mahdisml.webimmortalguards.Strings.start_action
import dev.mahdisml.webimmortalguards.Strings.stop_action
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

class VpnController (private val ctx: Context) {
    private var controllerScope: CoroutineScope? = null

    fun startVpn(){
        controllerScope?.cancel("stop")
        val startServiceIntent = Intent(ctx, VpnService::class.java)
        startServiceIntent.action = start_action
        ContextCompat.startForegroundService(ctx, startServiceIntent)
    }
    fun endVpn(){
        controllerScope?.cancel("stop")
        val stopServiceIntent = Intent(ctx, VpnService::class.java)
        stopServiceIntent.action = stop_action
        ContextCompat.startForegroundService(ctx, stopServiceIntent)
    }
    fun isOn(): Boolean {
        var isFound = false
        val notificationManager = (ctx as Activity).getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notifications = notificationManager.activeNotifications
        notifications?.let {
            for (i in notifications){
                if (package_name == i.packageName){
                    isFound = true
                }
            }
        }
        return isFound
    }
}