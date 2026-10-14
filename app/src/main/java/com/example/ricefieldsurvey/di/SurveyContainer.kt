package com.example.ricefieldsurvey.di

import android.content.Context
import androidx.room.Room
import com.example.ricefieldsurvey.data.local.SurveyDatabase
import com.example.ricefieldsurvey.data.repository.LocalAuthRepository
import com.example.ricefieldsurvey.data.repository.RoomInterviewRepository
import com.example.ricefieldsurvey.domain.usecase.GetInterviewUseCase
import com.example.ricefieldsurvey.domain.usecase.LoginUseCase
import com.example.ricefieldsurvey.domain.usecase.ObserveInterviewsUseCase
import com.example.ricefieldsurvey.domain.usecase.SaveDraftUseCase
import com.example.ricefieldsurvey.domain.usecase.SubmitInterviewUseCase

/** Composition root: UI depends on use cases, never Room or SharedPreferences. */
class SurveyContainer(context: Context) {
    private val database = Room.databaseBuilder(
        context.applicationContext, SurveyDatabase::class.java, "rice_field_survey.db",
    ).build()

    val auth = LocalAuthRepository(context.applicationContext)
    private val interviewRepository = RoomInterviewRepository(database.interviewDao())

    val login = LoginUseCase(auth)
    val observeInterviews = ObserveInterviewsUseCase(interviewRepository)
    val getInterview = GetInterviewUseCase(interviewRepository)
    val saveDraft = SaveDraftUseCase(interviewRepository)
    val submitInterview = SubmitInterviewUseCase(interviewRepository)
}
