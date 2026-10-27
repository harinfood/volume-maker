package com.contoh.volumemaker

import android.content.Context

/** Preset volume per aplikasi dan status pengaturan. */
object Pengaturan {
    private const val NAMA = "volume_maker"

    private fun prefs(c: Context) = c.getSharedPreferences(NAMA, Context.MODE_PRIVATE)

    fun aktifOtomatis(c: Context): Boolean = prefs(c).getBoolean("aktif", true)

    fun simpanAktifOtomatis(c: Context, v: Boolean) {
        prefs(c).edit().putBoolean("aktif", v).apply()
    }

    /** Preset volume (0-100) untuk aplikasi tertentu, atau null jika belum diatur. */
    fun presetApp(c: Context, paket: String): Int? {
        val p = prefs(c)
        return if (p.contains("p_$paket")) p.getInt("p_$paket", 50) else null
    }

    fun simpanPreset(c: Context, paket: String, persen: Int?) {
        val e = prefs(c).edit()
        if (persen == null) e.remove("p_$paket") else e.putInt("p_$paket", persen)
        e.apply()
    }

    fun catatTerakhir(c: Context, paket: String, persen: Int) {
        prefs(c).edit().putString("t_paket", paket).putInt("t_persen", persen).apply()
    }

    fun terakhirPaket(c: Context): String? = prefs(c).getString("t_paket", null)

    fun terakhirPersen(c: Context): Int = prefs(c).getInt("t_persen", 0)
}
