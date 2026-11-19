package com.antojitos.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Un antojo: algo que quiero comprar. priority: 0 = Alta, 1 = Media, 2 = Baja */
@Entity(tableName = "wishes")
data class Wish(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val price: Long,
    val priority: Int,
    val category: String,
    val imagePath: String? = null,
    val link: String? = null,
    val purchased: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val purchasedAt: Long? = null
)
