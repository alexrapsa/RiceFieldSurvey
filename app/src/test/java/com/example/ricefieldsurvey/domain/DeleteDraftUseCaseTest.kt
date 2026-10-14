package com.example.ricefieldsurvey.domain

import com.example.ricefieldsurvey.domain.model.Interview
import com.example.ricefieldsurvey.domain.model.InterviewStatus
import com.example.ricefieldsurvey.domain.repository.InterviewRepository
import com.example.ricefieldsurvey.domain.usecase.DeleteDraftUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for the domain deletion contract. The Room query also enforces these rules. */
class DeleteDraftUseCaseTest {
    private class FakeRepository(var record: Interview?) : InterviewRepository {
        var deleteCalls = 0

        override fun observeForEnumerator(enumeratorId: String): Flow<List<Interview>> =
            flowOf(listOfNotNull(record).filter { it.enumeratorId == enumeratorId })

        override suspend fun getById(interviewId: String): Interview? =
            record?.takeIf { it.id == interviewId }

        override suspend fun upsert(interview: Interview) { record = interview }

        override suspend fun deleteDraft(interviewId: String, enumeratorId: String): Boolean {
            deleteCalls++
            val current = record ?: return false
            if (current.id != interviewId || current.enumeratorId != enumeratorId ||
                current.status != InterviewStatus.DRAFT) return false
            record = null
            return true
        }
    }

    @Test fun `current enumerator can delete own draft`() = runBlocking {
        val draft = Interview(id = "a", enumeratorId = "enumerator1")
        val repository = FakeRepository(draft)
        assertTrue(DeleteDraftUseCase(repository)("a", "enumerator1"))
        assertNull(repository.record)
    }

    @Test fun `another enumerator cannot delete a draft`() = runBlocking {
        val draft = Interview(id = "a", enumeratorId = "enumerator1")
        val repository = FakeRepository(draft)
        assertFalse(DeleteDraftUseCase(repository)("a", "enumerator2"))
        assertTrue(repository.record == draft)
    }

    @Test fun `completed records cannot be deleted`() = runBlocking {
        val completed = Interview(id = "a", enumeratorId = "enumerator1", status = InterviewStatus.COMPLETED)
        val repository = FakeRepository(completed)
        assertFalse(DeleteDraftUseCase(repository)("a", "enumerator1"))
        assertTrue(repository.record == completed)
    }

    @Test fun `blank identifiers do not reach repository`() = runBlocking {
        val repository = FakeRepository(Interview(id = "a", enumeratorId = "enumerator1"))
        val delete = DeleteDraftUseCase(repository)
        assertFalse(delete("", "enumerator1"))
        assertFalse(delete("a", ""))
        assertTrue(repository.deleteCalls == 0)
    }
}
