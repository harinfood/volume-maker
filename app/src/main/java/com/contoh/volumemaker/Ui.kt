package com.contoh.volumemaker

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.widget.LinearLayout
import android.widget.TextView

fun Context.dp(v: Int): Int = Math.round(v * resources.displayMetrics.density)

fun Context.dpF(v: Float): Float = v * resources.displayMetrics.density

object Warna {
    val gelap = Color.parseColor("#1E1E1E")
    val gelapTerang = Color.parseColor("#3A3A3A")
    val kuning = Color.parseColor("#FFC107")
    val oranye = Color.parseColor("#FF8F00")
    val latar = Color.parseColor("#F5F5F5")
    val teksUtama = Color.parseColor("#212121")
    val teksRedup = Color.parseColor("#757575")
    val hijau = Color.parseColor("#2E7D32")
}

fun kotak(ctx: Context, warna: Int, radiusDp: Int, garis: Int = 0): GradientDrawable {
    val g = GradientDrawable()
    g.setColor(warna)
    g.setCornerRadius(ctx.dpF(radiusDp.toFloat()))
    if (garis != 0) g.setStroke(ctx.dp(1), garis)
    return g
}

fun teks(ctx: Context, isi: String, sp: Float, warna: Int, tebal: Boolean = false): TextView {
    val t = TextView(ctx)
    t.text = isi
    t.textSize = sp
    t.setTextColor(warna)
    if (tebal) t.typeface = Typeface.DEFAULT_BOLD
    return t
}

fun linear(ctx: Context, vertikal: Boolean = true): LinearLayout {
    val l = LinearLayout(ctx)
    l.orientation = if (vertikal) LinearLayout.VERTICAL else LinearLayout.HORIZONTAL
    return l
}

fun lp(
    lebar: Int,
    tinggi: Int,
    bobot: Float = 0f,
    atas: Int = 0,
    kiri: Int = 0,
    kanan: Int = 0,
    bawah: Int = 0
): LinearLayout.LayoutParams {
    val p = LinearLayout.LayoutParams(lebar, tinggi, bobot)
    p.setMargins(kiri, atas, kanan, bawah)
    return p
}

fun kartu(ctx: Context): LinearLayout {
    val k = linear(ctx)
    k.setPadding(ctx.dp(18), ctx.dp(16), ctx.dp(18), ctx.dp(16))
    k.background = kotak(ctx, Color.WHITE, 22, Color.parseColor("#E0E0E0"))
    return k
}

fun kartuLp(ctx: Context, atas: Int = 16): LinearLayout.LayoutParams =
    lp(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT,
        0f,
        ctx.dp(atas),
        ctx.dp(16),
        ctx.dp(16),
        0
    )
