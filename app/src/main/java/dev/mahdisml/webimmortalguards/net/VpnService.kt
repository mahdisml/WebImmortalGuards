package dev.mahdisml.webimmortalguards.net

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_CANCEL_CURRENT
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_NO_CREATE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.ProxyInfo
import android.net.VpnService
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import dev.mahdisml.webimmortalguards.MainActivity
import dev.mahdisml.webimmortalguards.R
import dev.mahdisml.webimmortalguards.Strings.app_name
import dev.mahdisml.webimmortalguards.Strings.channel_id
import dev.mahdisml.webimmortalguards.Strings.home_text_on
import dev.mahdisml.webimmortalguards.Strings.package_name
import dev.mahdisml.webimmortalguards.Strings.stop_action
import dev.mahdisml.webimmortalguards.Strings.vpn_address
import java.io.IOException
import kotlin.system.exitProcess


class VpnService : VpnService() {

    private val vpnAdapter = VpnAdapter()
    private var vpnInterface: ParcelFileDescriptor? = null

    override fun onCreate() {
        super.onCreate()
        startVpn()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null){
            intent.action?.let {
                if (it == stop_action){
                    stopVpn()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
    }

    private fun stopVpn(){
        vpnAdapter.stop()
        try {
            vpnInterface?.close()
        } catch (e: IOException) {
            Log.e("Sml","parcelFileDescriptor.close()", e)
        }
        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } catch (_: Exception) { }
        try {
            stopSelf()
        } catch (_: Exception) { }

    }
    private fun startVpn(){
        try {
            if (vpnInterface == null) {
                val channel = NotificationChannel(
                    channel_id,
                    app_name,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
                val intent = packageManager.getLaunchIntentForPackage(package_name)
                val pendingIntent = if (intent != null) {
                    PendingIntent.getActivity(
                        this,
                        0,
                        intent,
                        FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
                    )
                }else{
                    PendingIntent.getActivity(
                        this, 0,
                        Intent(this, MainActivity::class.java), FLAG_IMMUTABLE or FLAG_NO_CREATE or FLAG_UPDATE_CURRENT or FLAG_CANCEL_CURRENT
                    )
                }
                val notification = NotificationCompat.Builder(this, channel_id)
                    .setContentTitle(app_name)
                    .setSmallIcon(R.drawable.notification_logo)
                    .setLargeIcon(BitmapFactory.decodeResource(resources,R.drawable.notification_logo))
                    .setContentText(home_text_on)
                    .setContentIntent(pendingIntent).build()
                startForeground(1, notification)
                vpnAdapter.start()
                val builder = Builder()
                builder.setUnderlyingNetworks(null)
                builder.setMetered(false)
                builder.addDisallowedApplication(package_name)
                builder.setHttpProxy(ProxyInfo.buildDirectProxy("127.0.0.1",4525))
                builder.addAddress(vpn_address, 32)
                builder.addDnsServer("8.8.8.8")

                vpnInterface = builder.setSession(app_name).establish()
            }
        } catch (e: Exception) {
            Log.e("Sml", "error", e)
            exitProcess(0)
        }
    }
    fun isOn(): Boolean {
        return vpnAdapter.isOn()
    }

}