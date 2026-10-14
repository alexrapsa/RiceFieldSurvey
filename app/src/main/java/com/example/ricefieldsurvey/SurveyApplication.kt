package com.example.ricefieldsurvey

import android.app.Application
import com.example.ricefieldsurvey.di.SurveyContainer

class SurveyApplication : Application() {
    val container: SurveyContainer by lazy { SurveyContainer(this) }
}
