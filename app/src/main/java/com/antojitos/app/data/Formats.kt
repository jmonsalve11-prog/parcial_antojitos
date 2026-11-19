package com.antojitos.app.data

import android.graphics.Color
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Cat(val name: String, val emoji: String)

object Categories {
    @JvmField
    val ALL: List<Cat> = listOf(
        Cat("Ropa", "👕"),
        Cat("Tecnología", "💻"),
        Cat("Comida", "🍔"),
        Cat("Ocio", "🎮"),
        Cat("Belleza", "💄"),
        Cat("Hogar", "🏠"),
        Cat("Otros", "✨")
    )

    @JvmStatic
    fun emoji(name: String): String = ALL.firstOrNull { it.name == name }?.emoji ?: "✨"
}

object Priority {
    @JvmStatic
    fun label(p: Int): String = when (p) {
        0 -> "🔥 Alta"
        1 -> "⚡ Media"
        else -> "🌱 Baja"
    }

    @JvmStatic
    fun color(p: Int): Int = when (p) {
        0 -> Color.parseColor("#E53970")
        1 -> Color.parseColor("#D18A00")
        else -> Color.parseColor("#1FA463")
    }
}

object Fmt {
    /** 12000 -> "$ 12.000" */
    @JvmStatic
    fun cop(value: Long): String =
        NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply { maximumFractionDigits = 0 }.format(value)

    @JvmStatic
    fun date(timestamp: Long): String =
        SimpleDateFormat("d MMM yyyy", Locale("es", "CO")).format(Date(timestamp))
}
