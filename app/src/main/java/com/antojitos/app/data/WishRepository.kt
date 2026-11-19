package com.antojitos.app.data

import android.content.Context
import androidx.lifecycle.LiveData
import java.io.File
import java.util.concurrent.Executors

/** Punto único de acceso a los datos. Las escrituras corren en un hilo de fondo. */
class WishRepository private constructor(context: Context) {

    private val dao = AppDatabase.get(context).wishDao()
    private val io = Executors.newSingleThreadExecutor()

    /** Modalidad 1: antojos pendientes */
    val pending: LiveData<List<Wish>> = dao.pending()

    /** Modalidad 2: compras realizadas */
    val bought: LiveData<List<Wish>> = dao.bought()

    fun save(wish: Wish) {
        io.execute { dao.upsert(wish) }
    }

    fun setPurchased(wish: Wish, purchased: Boolean) {
        io.execute {
            dao.upsert(
                wish.copy(
                    purchased = purchased,
                    purchasedAt = if (purchased) System.currentTimeMillis() else null
                )
            )
        }
    }

    fun delete(wish: Wish) {
        io.execute {
            dao.delete(wish)
            wish.imagePath?.let { runCatching { File(it).delete() } }
        }
    }

    companion object {
        @Volatile
        private var instance: WishRepository? = null

        @JvmStatic
        fun get(context: Context): WishRepository =
            instance ?: synchronized(this) {
                instance ?: WishRepository(context.applicationContext).also { instance = it }
            }
    }
}
