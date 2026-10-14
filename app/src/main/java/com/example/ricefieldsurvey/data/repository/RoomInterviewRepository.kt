package com.example.ricefieldsurvey.data.repository

import com.example.ricefieldsurvey.data.local.InterviewDao
import com.example.ricefieldsurvey.data.local.toDomain
import com.example.ricefieldsurvey.data.local.toEntity
import com.example.ricefieldsurvey.domain.model.Interview
import com.example.ricefieldsurvey.domain.repository.InterviewRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomInterviewRepository(private val dao: InterviewDao) : InterviewRepository {
    override fun observeForEnumerator(enumeratorId: String): Flow<List<Interview>> =
        dao.observeForEnumerator(enumeratorId).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(interviewId: String): Interview? = dao.getById(interviewId)?.toDomain()

    override suspend fun deleteDraft(interviewId: String, enumeratorId: String): Boolean =
        dao.deleteDraft(interviewId, enumeratorId) > 0

    override suspend fun upsert(interview: Interview) {
        dao.upsert(interview.toEntity())
    }
}
