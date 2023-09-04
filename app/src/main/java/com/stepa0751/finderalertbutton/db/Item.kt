package com.stepa0751.finderalertbutton.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity (tableName = "items")
data class Item (
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    @ColumnInfo(name = "updateId")
    var updateId: Long,
    @ColumnInfo(name = "date")
    var date: Long,
    @ColumnInfo(name = "text")
    var text: String,
    @ColumnInfo(name = "latitude")
    var latitude: Float,
    @ColumnInfo(name = "longitude")
    var longitude: Float
)
