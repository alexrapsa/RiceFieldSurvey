// Run with: bash tools/check-domain.sh
import com.example.ricefieldsurvey.domain.model.Interview
import com.example.ricefieldsurvey.domain.model.SurveyOptions
import com.example.ricefieldsurvey.domain.model.SurveyValidator

fun main() {
    val interview = Interview(
        enumeratorId = "enumerator1", farmerName = "Demo Farmer", sex = "Female",
        age = "45", province = "Nueva Ecija", municipality = "Muñoz",
        isRiceFarmer = true, totalRiceArea = "1.25",
        riceEcosystem = "Irrigated", establishmentMethod = "Direct Seeding",
        usesCertifiedSeeds = true, seedVariety = "NSIC Rc 222",
        productionChallenges = setOf("High fertilizer cost"),
    )
    check(SurveyValidator.validate(interview).isEmpty())
    check("B2Q5" in SurveyValidator.validate(interview.copy(seedVariety = "")))
    check("B2Q7" in SurveyValidator.validate(interview.copy(productionChallenges = setOf(SurveyOptions.OTHER))))
    check(SurveyValidator.validate(interview.copy(isRiceFarmer = false)).isEmpty())
    check(interview.copy(isRiceFarmer = false).normalized().seedVariety.isEmpty())
    check("B1Q3" in SurveyValidator.validate(interview.copy(age = "bad")))
    println("PASS: valid answers, missing answers, branching, and normalization")
}
