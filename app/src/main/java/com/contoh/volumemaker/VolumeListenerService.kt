package com.contoh.volumemaker

import android.content.ComponentName
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService

/**
 * Memantau aplikasi mana yang sedang memutar audio (lewat sesi media).
 * Saat ada aplikasi baru yang mulai memutar dan punya preset, volume musik disetel sesuai preset.
 * Izin ini diberikan pengguna lewat Pengaturan > Akses notifikasi.
 */
class VolumeListenerService : NotificationListenerService() {

    private val handler = Handler(Looper.getMainLooper())
    private var manager: MediaSessionManager? = null
    private var sedangMain: String? = null

    private val pemeriksa = object : Runnable {
        override fun run() {
            periksa()
            handler.postDelayed(this, 1500L)
        }
    }

    override fun onListenerConnected() {
        manager = getSystemService(MediaSessionManager::class.java)
        handler.removeCallbacks(pemeriksa)
        handler.post(pemeriksa)
    }

    override fun onListenerDisconnected() {
        handler.removeCallbacks(pemeriksa)
        sedangMain = null
    }

    override fun onDestroy() {
        handler.removeCallbacks(pemeriksa)
        super.onDestroy()
    }

    private fun periksa() {
        if (!Pengaturan.aktifOtomatis(this)) {
            sedangMain = null
            return
        }
        val m = manager ?: return
        val komponen = ComponentName(this, VolumeListenerService::class.java)
        val sesi = try {
            m.getActiveSessions(komponen)
        } catch (e: SecurityException) {
            return
        }
        val paketMain = sesi.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING }?.packageName
        if (paketMain == sedangMain) return
        sedangMain = paketMain
        if (paketMain != null) VolumeController.terapkanUntukPaket(this, paketMain)
    }
}
