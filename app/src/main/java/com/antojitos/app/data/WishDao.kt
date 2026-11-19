package com.antojitos.app.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WishDao {
    @Query("SELECT * FROM wishes WHERE purchased = 0 ORDER BY priority ASC, createdAt DESC")
    fun pending(): LiveData<List<Wish>>

    @Query("SELECT * FROM wishes WHERE purchased = 1 ORDER BY purchasedAt DESC")
    fun bought(): LiveData<List<Wish>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(wish: Wish): Long

    @Delete
    fun delete(wish: Wish)
}
