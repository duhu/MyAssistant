package com.myassistant.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.myassistant.app.data.local.converter.Converters
import com.myassistant.app.data.local.dao.CardDao
import com.myassistant.app.data.local.entity.CardEntity
import com.myassistant.app.data.local.entity.CardFtsEntity

@Database(
    entities = [CardEntity::class, CardFtsEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao

    companion object {
        const val DATABASE_NAME = "my_assistant.db"
    }
}
