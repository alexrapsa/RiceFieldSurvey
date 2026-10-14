package com.example.ricefieldsurvey.domain.model

import java.math.BigDecimal

/** All question IDs match Appendix A in the provided assessment. */
object SurveyValidator {
    fun validate(interview: Interview): Map<String, String> {
        val i = interview.normalized()
        val errors = linkedMapOf<String, String>()
        if (i.farmerName.isBlank()) errors["B1Q1"] = "Farmer's full name is required."
        if (i.sex !in SurveyOptions.sexes) errors["B1Q2"] = "Select a sex."
        val age = i.age.toIntOrNull()
        if (age == null || age !in 1..120) {
            errors["B1Q3"] = "Enter a whole number between 1 and 120."
        }
        if (i.province !in SurveyOptions.provinces) errors["B1Q4"] = "Select a province."
        if (i.municipality.isBlank()) errors["B1Q5"] = "Municipality or city is required."
        if (i.isRiceFarmer == null) errors["B1Q6"] = "Select Yes or No."

        if (i.isRiceFarmer == true) {
            val area = i.totalRiceArea.toBigDecimalOrNull()
            if (area == null || area <= BigDecimal.ZERO) {
                errors["B2Q1"] = "Enter a valid rice farm area greater than zero."
            }
            if (i.riceEcosystem !in SurveyOptions.ecosystems) {
                errors["B2Q2"] = "Select the primary rice ecosystem."
            }
            if (i.establishmentMethod !in SurveyOptions.establishmentMethods) {
                errors["B2Q3"] = "Select the establishment method."
            }
            if (i.usesCertifiedSeeds == null) errors["B2Q4"] = "Select Yes or No."
            if (i.usesCertifiedSeeds == true && i.seedVariety.isBlank()) {
                errors["B2Q5"] = "Enter the primary certified seed variety."
            }
            if (i.productionChallenges.isEmpty() || !SurveyOptions.challenges.containsAll(i.productionChallenges)) {
                errors["B2Q6"] = "Select at least one production challenge."
            }
            if (SurveyOptions.OTHER in i.productionChallenges && i.otherChallenge.isBlank()) {
                errors["B2Q7"] = "Please specify the other challenge."
            }
        }
        return errors
    }
}
