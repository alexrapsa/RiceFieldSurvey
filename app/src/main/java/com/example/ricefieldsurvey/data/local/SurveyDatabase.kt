package com.example.ricefieldsurvey.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [InterviewEntity::class], version = 1, exportSchema = false)
abstract class SurveyDatabase : RoomDatabase() {
    abstract fun interviewDao(): InterviewDao
}
