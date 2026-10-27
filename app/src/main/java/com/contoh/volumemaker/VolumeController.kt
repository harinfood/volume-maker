package com.contoh.volumemaker

import android.content.Context
import android.media.AudioManager
import kotlin.math.roundToInt

/**
 * Mengatur volume sistem. Android tidak mengizinkan aplikasi mengubah volume
 * aplikasi lain secara langsung, jadi preset diterapkan ke volume musik sistem.
 */
object VolumeController {

    /** Pasangan (id aliran, nama) yang ditampilkan di layar utama. */
    val ALIRAN: List<Pair<Int, String>> = listOf(
        Pair(AudioManager.STREAM_MUSIC, "Musik & video"),
        Pair(AudioManager.STREAM_NOTIFICATION, "Notifikasi"),
        Pair(AudioManager.STREAM_RING, "Dering"),
        Pair(AudioManager.STREAM_ALARM, "Alarm")
    )

    private fun am(c: Context): AudioManager = c.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun persen(c: Context, stream: Int): Int {
        val m = am(c)
        val maks = m.getStreamMaxVolume(stream)
        return if (maks == 0) 0 else (m.getStreamVolume(stream) * 100f / maks).roundToInt()
    }

    fun aturPersen(c: Context, stream: Int, persen: Int) {
        val m = am(c)
        val maks = m.getStreamMaxVolume(stream)
        m.setStreamVolume(stream, (persen.coerceIn(0, 100) / 100f * maks).roundToInt(), 0)
    }

    /** Menyetel volume musik sesuai preset aplikasi. Mengembalikan false jika aplikasi belum punya preset. */
    fun terapkanUntukPaket(c: Context, paket: String): Boolean {
        val p = Pengaturan.presetApp(c, paket) ?: return false
        aturPersen(c, AudioManager.STREAM_MUSIC, p)
        Pengaturan.catatTerakhir(c, paket, p)
        return true
    }
}
