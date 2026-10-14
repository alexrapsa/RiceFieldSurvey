package com.example.ricefieldsurvey.presentation.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ricefieldsurvey.domain.model.Interview
import com.example.ricefieldsurvey.domain.model.InterviewStatus
import com.example.ricefieldsurvey.domain.usecase.GetInterviewUseCase
import com.example.ricefieldsurvey.domain.usecase.SaveDraftUseCase
import com.example.ricefieldsurvey.domain.usecase.SubmitInterviewUseCase
import com.example.ricefieldsurvey.domain.usecase.SubmitResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Both explicit saves and autosaves use a mutex to serialize writes. */
data class EditorUiState(
    val interview: Interview? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errors: Map<String, String> = emptyMap(),
    val message: String? = null,
)

class EditorViewModel(
    private val getInterview: GetInterviewUseCase,
    private val saveDraft: SaveDraftUseCase,
    private val submitInterview: SubmitInterviewUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState = _uiState.asStateFlow()
    private val writeMutex = Mutex()
    private var autosaveJob: Job? = null

    fun load(id: String, userId: String) {
        // Reload whenever the user reopens the editor, including after completing a survey.
        _uiState.value = EditorUiState(isLoading = true)
        viewModelScope.launch {
            try {
                val found = getInterview(id)
                if (found != null && found.enumeratorId != userId) {
                    _uiState.value = EditorUiState(isLoading = false, message = "Interview unavailable.")
                    return@launch
                }
                val interview = found ?: Interview(id = id, enumeratorId = userId)
                _uiState.value = EditorUiState(interview = interview, isLoading = false)
                // Immediately persist a new empty draft. A partially filled interview can then
                // survive unexpected app closure even before the first explicit Save.
                if (found == null) writeMutex.withLock { saveDraft(interview) }
            } catch (error: Exception) {
                _uiState.value = EditorUiState(isLoading = false, message =
                    "Cannot load the interview: ${error.localizedMessage ?: "storage error"}")
            }
        }
    }

    fun update(change: (Interview) -> Interview) {
        val current = _uiState.value.interview ?: return
        if (current.status != InterviewStatus.DRAFT) return
        val changed = change(current).normalized()
        _uiState.value = _uiState.value.copy(
            interview = changed, errors = emptyMap(), message = null,
        )
        autosaveJob?.cancel()
        autosaveJob = viewModelScope.launch {
            delay(550)
            try {
                writeMutex.withLock { saveDraft(changed) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(message =
                    "Autosave failed. Use Save draft to retry.")
            }
        }
    }

    fun saveAndLeave(onSaved: () -> Unit) {
        val current = _uiState.value.interview ?: return
        autosaveJob?.cancel()
        if (current.status == InterviewStatus.COMPLETED) {
            onSaved()
            return
        }
        _uiState.value = _uiState.value.copy(isSaving = true)
        viewModelScope.launch {
            try {
                writeMutex.withLock { saveDraft(current) }
                onSaved()
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false, message = "Could not save draft: ${error.localizedMessage}",
                )
            }
        }
    }

    fun complete(onCompleted: () -> Unit, onInvalid: (Set<String>) -> Unit) {
        val current = _uiState.value.interview ?: return
        if (current.status == InterviewStatus.COMPLETED) return
        autosaveJob?.cancel()
        _uiState.value = _uiState.value.copy(isSaving = true, message = null)
        viewModelScope.launch {
            try {
                val result = writeMutex.withLock { submitInterview(current) }
                when (result) {
                    SubmitResult.Success -> onCompleted()
                    is SubmitResult.ValidationErrors -> {
                        _uiState.value = _uiState.value.copy(
                            isSaving = false, errors = result.errors,
                            message = "Please complete the highlighted required questions.",
                        )
                        onInvalid(result.errors.keys)
                    }
                }
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false, message = "Could not complete interview: ${error.localizedMessage}",
                )
            }
        }
    }

    class Factory(
        private val get: GetInterviewUseCase,
        private val save: SaveDraftUseCase,
        private val submit: SubmitInterviewUseCase,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            EditorViewModel(get, save, submit) as T
    }
}
