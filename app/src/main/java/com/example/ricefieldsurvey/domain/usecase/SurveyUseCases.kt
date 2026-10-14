package com.example.ricefieldsurvey.domain.usecase

import com.example.ricefieldsurvey.domain.model.Interview
import com.example.ricefieldsurvey.domain.model.InterviewStatus
import com.example.ricefieldsurvey.domain.model.SurveyValidator
import com.example.ricefieldsurvey.domain.repository.InterviewRepository
import kotlinx.coroutines.flow.Flow

class ObserveInterviewsUseCase(private val repository: InterviewRepository) {
    operator fun invoke(userId: String): Flow<List<Interview>> = repository.observeForEnumerator(userId)
}

/** A completed record or another enumerator's record must never be deleted. */
class DeleteDraftUseCase(private val repository: InterviewRepository) {
    suspend operator fun invoke(interviewId: String, enumeratorId: String): Boolean {
        if (interviewId.isBlank() || enumeratorId.isBlank()) return false
        return repository.deleteDraft(interviewId, enumeratorId)
    }
}

class GetInterviewUseCase(private val repository: InterviewRepository) {
    suspend operator fun invoke(id: String): Interview? = repository.getById(id)
}

class SaveDraftUseCase(private val repository: InterviewRepository) {
    suspend operator fun invoke(interview: Interview) {
        require(interview.status == InterviewStatus.DRAFT) { "Completed interviews are read-only." }
        repository.upsert(interview.normalized().copy(updatedAt = System.currentTimeMillis()))
    }
}

sealed interface SubmitResult {
    data object Success : SubmitResult
    data class ValidationErrors(val errors: Map<String, String>) : SubmitResult
}

class SubmitInterviewUseCase(private val repository: InterviewRepository) {
    suspend operator fun invoke(interview: Interview): SubmitResult {
        if (interview.status == InterviewStatus.COMPLETED) {
            throw IllegalStateException("This interview has already been completed.")
        }
        val normalized = interview.normalized()
        val errors = SurveyValidator.validate(normalized)
        if (errors.isNotEmpty()) return SubmitResult.ValidationErrors(errors)
        val now = System.currentTimeMillis()
        repository.upsert(normalized.copy(
            status = InterviewStatus.COMPLETED, updatedAt = now, completedAt = now,
        ))
        return SubmitResult.Success
    }
}
