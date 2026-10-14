package com.example.ricefieldsurvey.domain.repository

import com.example.ricefieldsurvey.domain.model.Interview
import kotlinx.coroutines.flow.Flow

interface InterviewRepository {
    fun observeForEnumerator(enumeratorId: String): Flow<List<Interview>>
    suspend fun getById(interviewId: String): Interview?
    suspend fun upsert(interview: Interview)
}
