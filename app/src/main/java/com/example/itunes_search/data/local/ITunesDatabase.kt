package com.example.itunes_search.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FavoriteTrackEntity::class], version = 1, exportSchema = false)
abstract class ITunesDatabase : RoomDatabase() {
    abstract fun favoriteTrackDao(): FavoriteTrackDao
}
