package com.example.ricefieldsurvey.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.ricefieldsurvey.domain.model.Interview
import com.example.ricefieldsurvey.domain.model.InterviewStatus

@Entity(
    tableName = "interviews",
    indices = [Index(value = ["enumeratorId", "updatedAt"])],
)
data class InterviewEntity(
    @PrimaryKey val id: String,
    val enumeratorId: String,
    val farmerName: String,
    val sex: String,
    val age: String,
    val province: String,
    val municipality: String,
    val isRiceFarmer: Boolean?,
    val totalRiceArea: String,
    val riceEcosystem: String,
    val establishmentMethod: String,
    val usesCertifiedSeeds: Boolean?,
    val seedVariety: String,
    val productionChallenges: String,
    val otherChallenge: String,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long,
    val completedAt: Long?,
)

// Challenge values come from a fixed list without separators. This format is stable for v1.
private const val SEPARATOR = "\u001F"

fun Interview.toEntity() = InterviewEntity(
    id = id,
    enumeratorId = enumeratorId,
    farmerName = farmerName,
    sex = sex,
    age = age,
    province = province,
    municipality = municipality,
    isRiceFarmer = isRiceFarmer,
    totalRiceArea = totalRiceArea,
    riceEcosystem = riceEcosystem,
    establishmentMethod = establishmentMethod,
    usesCertifiedSeeds = usesCertifiedSeeds,
    seedVariety = seedVariety,
    productionChallenges = productionChallenges.sorted().joinToString(SEPARATOR),
    otherChallenge = otherChallenge,
    status = status.name,
    createdAt = createdAt,
    updatedAt = updatedAt,
    completedAt = completedAt,
)

fun InterviewEntity.toDomain() = Interview(
    id = id,
    enumeratorId = enumeratorId,
    farmerName = farmerName,
    sex = sex,
    age = age,
    province = province,
    municipality = municipality,
    isRiceFarmer = isRiceFarmer,
    totalRiceArea = totalRiceArea,
    riceEcosystem = riceEcosystem,
    establishmentMethod = establishmentMethod,
    usesCertifiedSeeds = usesCertifiedSeeds,
    seedVariety = seedVariety,
    productionChallenges = if (productionChallenges.isEmpty()) emptySet() else
        productionChallenges.split(SEPARATOR).toSet(),
    otherChallenge = otherChallenge,
    status = InterviewStatus.valueOf(status),
    createdAt = createdAt,
    updatedAt = updatedAt,
    completedAt = completedAt,
)
