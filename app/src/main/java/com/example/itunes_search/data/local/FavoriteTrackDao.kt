package com.example.itunes_search.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteTrackDao {
    @Query("SELECT * FROM favorite_tracks ORDER BY trackName COLLATE NOCASE, trackId")
    fun observeAll(): Flow<List<FavoriteTrackEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_tracks WHERE trackId = :trackId)")
    fun observeIsFavorite(trackId: Long): Flow<Boolean>

    @Query("SELECT * FROM favorite_tracks WHERE trackId = :trackId LIMIT 1")
    suspend fun findById(trackId: Long): FavoriteTrackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(track: FavoriteTrackEntity)

    @Query("DELETE FROM favorite_tracks WHERE trackId = :trackId")
    suspend fun delete(trackId: Long)
}
