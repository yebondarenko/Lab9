package com.example.lab9.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.lab9.data.local.entity.LocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Query("SELECT * FROM locations ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<LocationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: LocationEntity): Long

    @Query("DELETE FROM locations")
    suspend fun clear()

    @Delete
    suspend fun delete(location: LocationEntity)
}