package com.stepa0751.finderalertbutton.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {
    @Insert
    fun insertItem(item: Item)
    @Query("SELECT * FROM items")
    fun getAllItem(): Flow<List<Item>>
    @Query("SELECT `offset` FROM items")
    fun getOffsetItem(): Flow<List<Item>>

    @Query("SELECT MAX(`offset`) FROM items")
    fun getMaxOffset(): Flow<List<Item>>

    companion object {
        fun getAllItem() {

        }
    }
}