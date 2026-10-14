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

    /** Restrict deletion in SQL itself, even if the UI/use case is called incorrectly. */
    @Query("DELETE FROM interviews WHERE id = :id AND enumeratorId = :userId AND status = 'DRAFT'")
    suspend fun deleteDraft(id: String, userId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: InterviewEntity)
}
