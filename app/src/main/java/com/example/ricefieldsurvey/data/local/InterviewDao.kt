package com.example.ricefieldsurvey.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InterviewDao {
    @Query("SELECT * FROM interviews WHERE enumeratorId = :userId ORDER BY updatedAt DESC")
    fun observeForEnumerator(userId: String): Flow<List<InterviewEntity>>

    @Query("SELECT * FROM interviews WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): InterviewEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: InterviewEntity)
}
