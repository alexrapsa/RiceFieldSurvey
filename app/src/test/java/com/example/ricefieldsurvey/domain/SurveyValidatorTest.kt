package com.example.ricefieldsurvey.domain

import com.example.ricefieldsurvey.domain.model.Interview
import com.example.ricefieldsurvey.domain.model.SurveyOptions
import com.example.ricefieldsurvey.domain.model.SurveyValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SurveyValidatorTest {
    private fun farmer() = Interview(
        enumeratorId = "enumerator1", farmerName = "Maria Dela Cruz",
        sex = "Female", age = "47", province = "Nueva Ecija",
        municipality = "Muñoz", isRiceFarmer = true,
        totalRiceArea = "2.75", riceEcosystem = "Irrigated",
        establishmentMethod = "Transplanting", usesCertifiedSeeds = true,
        seedVariety = "NSIC Rc 222", productionChallenges = setOf("High fertilizer cost"),
    )

    @Test fun `valid complete farming interview has no errors`() {
        assertTrue(SurveyValidator.validate(farmer()).isEmpty())
    }

    @Test fun `non rice farmer can complete without block two`() {
        val noRice = farmer().copy(isRiceFarmer = false)
        assertTrue(SurveyValidator.validate(noRice).isEmpty())
        assertTrue(noRice.normalized().productionChallenges.isEmpty())
        assertEquals("", noRice.normalized().totalRiceArea)
    }

    @Test fun `seed variety only required when certified seeds used`() {
        assertTrue("B2Q5" in SurveyValidator.validate(farmer().copy(seedVariety = "")))
        assertFalse("B2Q5" in SurveyValidator.validate(
            farmer().copy(usesCertifiedSeeds = false, seedVariety = "")))
    }

    @Test fun `other challenge requires explanation`() {
        val withOther = farmer().copy(productionChallenges = setOf(SurveyOptions.OTHER))
        assertTrue("B2Q7" in SurveyValidator.validate(withOther))
        assertFalse("B2Q7" in SurveyValidator.validate(withOther.copy(otherChallenge = "Typhoon")))
        assertEquals("", withOther.copy(productionChallenges = emptySet()).normalized().otherChallenge)
    }

    @Test fun `required fields and invalid numeric values are rejected`() {
        val errors = SurveyValidator.validate(farmer().copy(
            farmerName = "", age = "4.5", totalRiceArea = "-3", municipality = "",
            productionChallenges = emptySet(),
        ))
        assertTrue(errors.keys.containsAll(listOf("B1Q1", "B1Q3", "B1Q5", "B2Q1", "B2Q6")))
    }

    @Test fun `unknown rice farming selection is rejected`() {
        val errors = SurveyValidator.validate(farmer().copy(isRiceFarmer = null))
        assertTrue("B1Q6" in errors)
        assertFalse(errors.keys.any { it.startsWith("B2") })
    }
}
