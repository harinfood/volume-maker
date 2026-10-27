package com.contoh.volumemaker

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView

class MainActivity : Activity() {

    private val handler = Handler(Looper.getMainLooper())
    private val seekAliran = HashMap<Int, SeekBar>()
    private val labelAliran = HashMap<Int, TextView>()
    private lateinit var swOtomatis: Switch
    private lateinit var txtStatus: TextView
    private lateinit var btnIzin: TextView
    private lateinit var txtTerakhir: TextView

    private val penyegar = object : Runnable {
        override fun run() {
            segarkanAliran()
            segarkanStatus()
            handler.postDelayed(this, 1000L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buatLayar())
    }

    override fun onResume() {
        super.onResume()
        handler.removeCallbacks(penyegar)
        handler.post(penyegar)
    }

    override fun onPause() {
        handler.removeCallbacks(penyegar)
        super.onPause()
    }

    private fun buatLayar(): View {
        val scroll = ScrollView(this)
        scroll.setBackgroundColor(Warna.latar)
        scroll.isFillViewport = true

        val root = linear(this)
        root.setPadding(0, 0, 0, dp(24))
        scroll.addView(root)

        root.addView(buatHeader())

        // Kartu status mode otomatis
        val kartuStatus = kartu(this)
        kartuStatus.addView(teks(this, "Volume otomatis per aplikasi", 16f, Warna.teksUtama, true))
        val barisSw = linear(this, false)
        barisSw.gravity = Gravity.CENTER_VERTICAL
        barisSw.addView(teks(this, "Aktifkan volume otomatis", 14f, Warna.teksUtama), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        swOtomatis = Switch(this)
        swOtomatis.setOnCheckedChangeListener { _, on -> Pengaturan.simpanAktifOtomatis(this, on) }
        barisSw.addView(swOtomatis)
        kartuStatus.addView(barisSw, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0f, dp(6)))
        txtStatus = teks(this, "-", 13f, Warna.teksRedup)
        kartuStatus.addView(txtStatus, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0f, dp(8)))
        btnIzin = tombol("Izinkan akses notifikasi") { bukaPengaturanAkses() }
        kartuStatus.addView(btnIzin, lp(LinearLayout.LayoutParams.MATCH_PARENT, dp(46), 0f, dp(10)))
        root.addView(kartuStatus, kartuLp(this, 16))

        // Kartu volume sistem
        val kartuAliran = kartu(this)
        kartuAliran.addView(teks(this, "Volume sekarang", 16f, Warna.teksUtama, true))
        for ((stream, nama) in VolumeController.ALIRAN) {
            kartuAliran.addView(buatSlider(stream, nama), lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0f, dp(12)))
        }
        root.addView(kartuAliran, kartuLp(this, 12))

        // Kartu aplikasi terakhir
        val kartuTerakhir = kartu(this)
        txtTerakhir = teks(this, "Belum ada aplikasi yang diatur otomatis.", 13f, Warna.teksRedup)
        kartuTerakhir.addView(txtTerakhir)
        root.addView(kartuTerakhir, kartuLp(this, 12))

        root.addView(
            tombol("Atur volume per aplikasi") { startActivity(Intent(this, AplikasiActivity::class.java)) },
            lp(LinearLayout.LayoutParams.MATCH_PARENT, dp(54), 0f, dp(16), dp(16), dp(16), 0)
        )

        // Kemunculan bertahap
        munculkan(kartuStatus, 0)
        munculkan(kartuAliran, 1)
        munculkan(kartuTerakhir, 2)

        return scroll
    }

    private fun munculkan(v: View, urutan: Int) {
        v.alpha = 0f
        v.translationY = dpF(24f)
        v.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(urutan * 100L)
            .setDuration(480L)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    private fun buatHeader(): View {
        val h = linear(this, false)
        h.gravity = Gravity.CENTER_VERTICAL
        h.setPadding(dp(18), dp(40), dp(18), dp(18))
        h.background = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(Warna.gelap, Warna.gelapTerang)
        )

        val logo = ImageView(this)
        logo.setImageResource(R.drawable.logo_volume)
        logo.scaleType = ImageView.ScaleType.FIT_CENTER
        h.addView(logo, LinearLayout.LayoutParams(dp(60), dp(60)).apply { marginEnd = dp(12) })

        val kolom = linear(this)
        kolom.addView(teks(this, "Volume Maker", 22f, Warna.kuning, true))
        kolom.addView(teks(this, "volume per aplikasi by Harinfood", 13f, Color.parseColor("#BDBDBD")))
        h.addView(kolom, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        return h
    }

    private fun tombol(label: String, aksi: () -> Unit): TextView {
        val t = teks(this, label, 15f, Warna.gelap, true)
        t.gravity = Gravity.CENTER
        val g = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(Warna.kuning, Warna.oranye))
        g.cornerRadius = dpF(24f)
        t.background = RippleDrawable(ColorStateList.valueOf(Color.parseColor("#55FFFFFF")), g, null)
        t.setOnClickListener { aksi() }
        return t
    }

    private fun buatSlider(stream: Int, nama: String): LinearLayout {
        val k = linear(this)
        val baris = linear(this, false)
        baris.addView(teks(this, nama, 14f, Warna.teksUtama), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        val label = teks(this, "0%", 14f, Warna.oranye, true)
        labelAliran[stream] = label
        baris.addView(label)
        k.addView(baris)

        val sb = SeekBar(this)
        sb.max = 100
        sb.progressTintList = ColorStateList.valueOf(Warna.kuning)
        sb.progressBackgroundTintList = ColorStateList.valueOf(Color.parseColor("#E0E0E0"))
        sb.thumbTintList = ColorStateList.valueOf(Warna.oranye)
        sb.progress = VolumeController.persen(this, stream)
        label.text = "${sb.progress}%"
        sb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                label.text = "$p%"
                if (fromUser) VolumeController.aturPersen(this@MainActivity, stream, p)
            }

            override fun onStartTrackingTouch(s: SeekBar?) {}

            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
        seekAliran[stream] = sb
        k.addView(sb)
        return k
    }

    private fun segarkanAliran() {
        for ((stream, sb) in seekAliran) {
            if (sb.isPressed) continue
            val p = VolumeController.persen(this, stream)
            if (sb.progress != p) {
                sb.progress = p
                labelAliran[stream]?.text = "$p%"
            }
        }
    }

    private fun izinAktif(): Boolean =
        Settings.Secure.getString(contentResolver, "enabled_notification_listeners")?.contains(packageName) == true

    private fun segarkanStatus() {
        val izin = izinAktif()
        val aktif = Pengaturan.aktifOtomatis(this)
        btnIzin.visibility = if (izin) View.GONE else View.VISIBLE
        if (swOtomatis.isChecked != aktif) swOtomatis.isChecked = aktif
        txtStatus.text = when {
            !izin -> "Akses notifikasi belum diizinkan. Izinkan agar volume bisa menyesuaikan aplikasi yang sedang memutar audio."
            aktif -> "Aktif. Volume musik akan disetel sesuai preset aplikasi saat aplikasi itu mulai memutar audio."
            else -> "Nonaktif. Volume tidak berubah otomatis."
        }

        val paket = Pengaturan.terakhirPaket(this)
        txtTerakhir.text = if (paket == null) {
            "Belum ada aplikasi yang diatur otomatis."
        } else {
            "Terakhir: ${namaAplikasi(paket)} · volume musik ${Pengaturan.terakhirPersen(this)}%"
        }
    }

    private fun namaAplikasi(paket: String): String = try {
        packageManager.getApplicationLabel(packageManager.getApplicationInfo(paket, 0)).toString()
    } catch (e: Exception) {
        paket
    }

    private fun bukaPengaturanAkses() {
        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }
}
