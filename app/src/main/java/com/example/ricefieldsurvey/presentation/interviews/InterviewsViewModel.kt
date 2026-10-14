package com.example.ricefieldsurvey.presentation.interviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ricefieldsurvey.domain.model.Interview
import com.example.ricefieldsurvey.domain.usecase.ObserveInterviewsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

class InterviewsViewModel(private val observe: ObserveInterviewsUseCase) : ViewModel() {
    private val activeUser = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val interviews: StateFlow<List<Interview>> = activeUser.flatMapLatest { id ->
        if (id.isBlank()) flowOf(emptyList()) else observe(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun forUser(userId: String) {
        activeUser.value = userId
    }

    class Factory(private val observe: ObserveInterviewsUseCase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            InterviewsViewModel(observe) as T
    }
}
