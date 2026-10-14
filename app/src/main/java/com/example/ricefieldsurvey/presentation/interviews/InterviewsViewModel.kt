package com.example.ricefieldsurvey.presentation.interviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ricefieldsurvey.domain.model.Interview
import com.example.ricefieldsurvey.domain.usecase.DeleteDraftUseCase
import com.example.ricefieldsurvey.domain.usecase.ObserveInterviewsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

class InterviewsViewModel(
    private val observe: ObserveInterviewsUseCase,
    private val deleteDraftUseCase: DeleteDraftUseCase,
) : ViewModel() {
    private val activeUser = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val interviews: StateFlow<List<Interview>> = activeUser.flatMapLatest { id ->
        if (id.isBlank()) flowOf(emptyList()) else observe(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _deletionFeedback = MutableStateFlow<String?>(null)
    val deletionFeedback: StateFlow<String?> = _deletionFeedback

    fun deleteDraft(interviewId: String) {
        val userId = activeUser.value
        if (userId.isBlank()) return
        viewModelScope.launch {
            _deletionFeedback.value = try {
                if (deleteDraftUseCase(interviewId, userId)) "Draft deleted."
                else "Draft not found or cannot be deleted."
            } catch (_: Exception) {
                "Could not delete the draft. Please try again."
            }
        }
    }

    fun clearDeletionFeedback() {
        _deletionFeedback.value = null
    }

    fun forUser(userId: String) {
        activeUser.value = userId
    }

    class Factory(
        private val observe: ObserveInterviewsUseCase,
        private val deleteDraftUseCase: DeleteDraftUseCase,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            InterviewsViewModel(observe, deleteDraftUseCase) as T
    }
}
