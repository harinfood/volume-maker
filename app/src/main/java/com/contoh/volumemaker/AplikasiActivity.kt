package com.contoh.volumemaker

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView

/** Daftar aplikasi yang bisa diberi preset volume. */
class AplikasiActivity : Activity() {

    private data class AppItem(val paket: String, val nama: String)

    private val daftar = mutableListOf<AppItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val hasil = pm.queryIntentActivities(intent, 0)
        val unik = LinkedHashMap<String, AppItem>()
        for (r in hasil) {
            val paket = r.activityInfo.packageName
            if (paket != packageName && !unik.containsKey(paket)) {
                unik[paket] = AppItem(paket, r.loadLabel(pm).toString())
            }
        }
        daftar.addAll(unik.values.sortedBy { it.nama.lowercase() })

        val outer = linear(this)
        outer.setBackgroundColor(Warna.latar)

        val bar = linear(this, false)
        bar.gravity = Gravity.CENTER_VERTICAL
        bar.setPadding(dp(16), dp(40), dp(16), dp(16))
        bar.setBackgroundColor(Warna.gelap)
        val kembali = teks(this, "← Kembali", 16f, Warna.kuning, true)
        kembali.setOnClickListener { finish() }
        bar.addView(kembali)
        val judul = teks(this, "Volume per aplikasi", 18f, Color.WHITE, true)
        judul.setPadding(dp(16), 0, 0, 0)
        bar.addView(judul)
        outer.addView(bar)

        val list = ListView(this)
        list.adapter = Adapter()
        list.dividerHeight = dp(1)
        list.divider = android.graphics.drawable.ColorDrawable(Color.parseColor("#E0E0E0"))
        outer.addView(list, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))

        setContentView(outer)
    }

    private inner class Adapter : BaseAdapter() {
        override fun getCount(): Int = daftar.size
        override fun getItem(position: Int): Any = daftar[position]
        override fun getItemId(position: Int): Long = position.toLong()
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View =
            buatBaris(daftar[position])
    }

    private fun buatBaris(item: AppItem): View {
        val baris = linear(this, false)
        baris.gravity = Gravity.CENTER_VERTICAL
        baris.setPadding(dp(14), dp(10), dp(14), dp(10))
        baris.setBackgroundColor(Color.WHITE)

        val ikon = ImageView(this)
        ikon.setImageDrawable(runCatching { packageManager.getApplicationIcon(item.paket) }.getOrNull())
        baris.addView(ikon, LinearLayout.LayoutParams(dp(40), dp(40)))

        val kolom = linear(this)
        kolom.setPadding(dp(12), 0, dp(8), 0)
        val nama = teks(this, item.nama, 15f, Warna.teksUtama, true)
        val keterangan = teks(this, "", 12f, Warna.teksRedup)
        val seek = SeekBar(this)
        seek.max = 100
        seek.progressTintList = ColorStateList.valueOf(Warna.kuning)
        seek.progressBackgroundTintList = ColorStateList.valueOf(Color.parseColor("#E0E0E0"))
        seek.thumbTintList = ColorStateList.valueOf(Warna.oranye)

        val preset = Pengaturan.presetApp(this, item.paket)
        val sw = Switch(this)
        sw.isChecked = preset != null
        seek.progress = preset ?: 50
        seek.isEnabled = sw.isChecked
        keterangan.text = if (preset != null) "Volume ${seek.progress}% saat aplikasi ini memutar" else "Tidak diatur"

        kolom.addView(nama)
        kolom.addView(keterangan)
        kolom.addView(seek)
        baris.addView(kolom, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        baris.addView(sw)

        sw.setOnCheckedChangeListener { _, on ->
            seek.isEnabled = on
            Pengaturan.simpanPreset(this, item.paket, if (on) seek.progress else null)
            keterangan.text = if (on) "Volume ${seek.progress}% saat aplikasi ini memutar" else "Tidak diatur"
        }
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                if (fromUser && sw.isChecked) {
                    Pengaturan.simpanPreset(this@AplikasiActivity, item.paket, p)
                    keterangan.text = "Volume $p% saat aplikasi ini memutar"
                }
            }

            override fun onStartTrackingTouch(s: SeekBar?) {}

            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
        return baris
    }
}
