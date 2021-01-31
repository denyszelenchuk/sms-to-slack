package com.slacksms.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.slacksms.app.data.channels.Channel
import com.slacksms.app.data.channels.ChannelsDao
import com.slacksms.app.data.rules.Rule
import com.slacksms.app.data.rules.RulesDao


@Database(entities = [Channel::class, Rule::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun channelsDao(): ChannelsDao
    abstract fun rulesDao(): RulesDao

    companion object {

        private var INSTANCE: AppDatabase? = null
        private val sLock = Any()

        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Since we didn't alter the table, there's nothing else to do here.
            }
        }

        fun getInstance(context: Context): AppDatabase {
            synchronized(sLock) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        AppDatabase::class.java, "AppDb.db")
                        .addMigrations(MIGRATION_1_2)
                        .build()
                }
                return INSTANCE as AppDatabase
            }
        }
    }
}
