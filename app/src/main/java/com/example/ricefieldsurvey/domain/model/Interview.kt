package com.example.ricefieldsurvey.domain.model

import java.util.UUID

enum class InterviewStatus { DRAFT, COMPLETED }

/** Strings are intentionally used for partial numeric answers so drafts can hold incomplete input. */
data class Interview(
    val id: String = UUID.randomUUID().toString(),
    val enumeratorId: String,
    val farmerName: String = "",
    val sex: String = "",
    val age: String = "",
    val province: String = "",
    val municipality: String = "",
    val isRiceFarmer: Boolean? = null,
    val totalRiceArea: String = "",
    val riceEcosystem: String = "",
    val establishmentMethod: String = "",
    val usesCertifiedSeeds: Boolean? = null,
    val seedVariety: String = "",
    val productionChallenges: Set<String> = emptySet(),
    val otherChallenge: String = "",
    val status: InterviewStatus = InterviewStatus.DRAFT,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
) {
    /** Ensure answers to hidden questions are never stored or submitted. */
    fun normalized(): Interview {
        if (isRiceFarmer != true) return copy(
            totalRiceArea = "", riceEcosystem = "", establishmentMethod = "",
            usesCertifiedSeeds = null, seedVariety = "",
            productionChallenges = emptySet(), otherChallenge = "",
        )
        return copy(
            seedVariety = if (usesCertifiedSeeds == true) seedVariety else "",
            otherChallenge = if (SurveyOptions.OTHER in productionChallenges) otherChallenge else "",
        )
    }
}
