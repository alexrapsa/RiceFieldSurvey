package com.example.ricefieldsurvey.presentation.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ricefieldsurvey.domain.model.Interview
import com.example.ricefieldsurvey.domain.model.InterviewStatus
import com.example.ricefieldsurvey.domain.model.SurveyOptions
import com.example.ricefieldsurvey.presentation.components.ChoiceQuestion
import com.example.ricefieldsurvey.presentation.components.MultipleChoiceQuestion
import com.example.ricefieldsurvey.presentation.components.ProvinceQuestion
import com.example.ricefieldsurvey.presentation.components.QuestionBlock
import com.example.ricefieldsurvey.presentation.components.TextQuestion
import com.example.ricefieldsurvey.presentation.components.YesNoQuestion

@Composable
fun InterviewEditorScreen(
    state: EditorUiState,
    onUpdate: ((Interview) -> Interview) -> Unit,
    onSave: () -> Unit,
    onComplete: (onInvalid: (Set<String>) -> Unit) -> Unit,
) {
    var page by rememberSaveable { mutableIntStateOf(1) }
    val interview = state.interview
    val readOnly = interview?.status == InterviewStatus.COMPLETED
    val scroll = rememberScrollState()
    LaunchedEffect(page) { scroll.scrollTo(0) }

    BackHandler(enabled = !state.isSaving) { onSave() }

    Scaffold(
        bottomBar = {
            if (interview != null) {
                Surface(shadowElevation = 10.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth().imePadding().padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (readOnly) {
                            OutlinedButton(onClick = onSave, modifier = Modifier.weight(1f)) {
                                Text("Back to list")
                            }
                            if (interview.isRiceFarmer == true) {
                                Button(onClick = { page = if (page == 1) 2 else 1 },
                                    modifier = Modifier.weight(1f)) {
                                    Text(if (page == 1) "View farming info" else "Farmer info")
                                }
                            }
                        } else {
                            OutlinedButton(onClick = onSave, enabled = !state.isSaving,
                                modifier = Modifier.weight(1f)) { Text("Save draft") }
                            Button(
                                onClick = {
                                    if (page == 1 && interview.isRiceFarmer == true) {
                                        page = 2
                                    } else {
                                        onComplete { errors ->
                                            if (errors.any { it.startsWith("B1") }) page = 1
                                            else if (interview.isRiceFarmer == true) page = 2
                                        }
                                    }
                                },
                                enabled = !state.isSaving, modifier = Modifier.weight(1.2f),
                            ) {
                                Text(if (page == 1 && interview.isRiceFarmer == true)
                                    "Next: Farming" else "Complete interview")
                            }
                        }
                    }
                }
            }
        },
    ) { contentPadding ->
        if (state.isLoading) {
            Column(Modifier.fillMaxSize().padding(contentPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) { CircularProgressIndicator() }
        } else if (interview == null) {
            Column(Modifier.fillMaxSize().padding(contentPadding).padding(24.dp)) {
                Text(state.message ?: "Interview not available.",
                    color = MaterialTheme.colorScheme.error)
                TextButton(onClick = onSave) { Text("Back to interviews") }
            }
        } else {
            Column(Modifier.fillMaxSize().padding(contentPadding)) {
                Surface(color = MaterialTheme.colorScheme.surface) {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                        TextButton(onClick = onSave, enabled = !state.isSaving) {
                            Text("‹  All interviews")
                        }
                        Text(if (readOnly) "Completed interview" else "Farmer interview",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold)
                        Text(if (page == 1 && interview.isRiceFarmer == false)
                                "Farmer information  ·  Farming block skipped"
                            else if (page == 1) "Block 1 of 2  ·  Farmer information"
                            else "Block 2 of 2  ·  Rice farming information",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(progress = {
                            if (page == 2 || interview.isRiceFarmer == false) 1f else 0.5f
                        },
                            modifier = Modifier.fillMaxWidth())
                        if (readOnly) {
                            Spacer(Modifier.height(8.dp))
                            Text("Read-only record • Saved locally",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                Column(Modifier.fillMaxSize().verticalScroll(scroll)
                    .padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (state.message != null) {
                        Surface(color = MaterialTheme.colorScheme.errorContainer,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)) {
                            Text(state.message, Modifier.fillMaxWidth().padding(12.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                    if (page == 1) {
                        FarmerInfoBlock(interview, state.errors, !readOnly, onUpdate)
                        if (interview.isRiceFarmer == false) {
                            Surface(color = MaterialTheme.colorScheme.primaryContainer,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)) {
                                Text("Rice farming questions are skipped because the farmer answered No.",
                                    Modifier.fillMaxWidth().padding(16.dp))
                            }
                        }
                    } else {
                        if (interview.isRiceFarmer == true) {
                            TextButton(onClick = { page = 1 }) { Text("‹  Back to farmer information") }
                            FarmingInfoBlock(interview, state.errors, !readOnly, onUpdate)
                        }
                    }
                    if (!readOnly) {
                        Text("Changes are saved automatically as a draft. Use Save draft before leaving.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary)
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun FarmerInfoBlock(
    i: Interview, errors: Map<String, String>, enabled: Boolean,
    update: ((Interview) -> Interview) -> Unit,
) {
    QuestionBlock(
        title = "Block 1 · Farmer information",
        instructions = "Record the basic information of the farmer being interviewed.",
    ) {
        TextQuestion("B1Q1", "Farmer's full name", i.farmerName,
            onValueChange = { name -> update { it.copy(farmerName = name) } },
            enabled = enabled, error = errors["B1Q1"], placeholder = "Full name")
        ChoiceQuestion("B1Q2", "Sex", SurveyOptions.sexes, i.sex,
            onSelected = { selected -> update { it.copy(sex = selected) } },
            enabled = enabled, error = errors["B1Q2"])
        TextQuestion("B1Q3", "Age", i.age,
            onValueChange = { age -> update { it.copy(age = age.filter(Char::isDigit)) } },
            keyboardType = KeyboardType.Number, enabled = enabled,
            error = errors["B1Q3"], placeholder = "Whole number")
        ProvinceQuestion(i.province, onSelected = { province ->
            update { it.copy(province = province) }
        }, enabled = enabled, error = errors["B1Q4"])
        TextQuestion("B1Q5", "Municipality / city", i.municipality,
            onValueChange = { municipality -> update { it.copy(municipality = municipality) } },
            enabled = enabled, error = errors["B1Q5"], placeholder = "Municipality or city")
        HorizontalDivider()
        YesNoQuestion("B1Q6", "Is the farmer currently engaged in rice farming?",
            selected = i.isRiceFarmer,
            onSelected = { yes -> update { it.copy(isRiceFarmer = yes) } },
            enabled = enabled, error = errors["B1Q6"])
    }
}

@Composable
private fun FarmingInfoBlock(
    i: Interview, errors: Map<String, String>, enabled: Boolean,
    update: ((Interview) -> Interview) -> Unit,
) {
    QuestionBlock(
        title = "Block 2 · Rice farming information",
        instructions = "Answer these questions for farmers currently engaged in rice farming.",
    ) {
        TextQuestion("B2Q1", "Total rice farm area (hectares)", i.totalRiceArea,
            onValueChange = { area ->
                // A single decimal separator makes on-device data entry more predictable.
                update { it.copy(totalRiceArea = area.filterIndexed { index, char ->
                    char.isDigit() || (char == '.' && '.' !in area.substring(0, index))
                }) }
            },
            enabled = enabled, error = errors["B2Q1"],
            keyboardType = KeyboardType.Decimal, placeholder = "e.g. 2.50")
        ChoiceQuestion("B2Q2", "Primary rice ecosystem", SurveyOptions.ecosystems,
            selected = i.riceEcosystem,
            onSelected = { ecosystem -> update { it.copy(riceEcosystem = ecosystem) } },
            enabled = enabled, error = errors["B2Q2"])
        ChoiceQuestion("B2Q3", "Rice establishment method", SurveyOptions.establishmentMethods,
            selected = i.establishmentMethod,
            onSelected = { method -> update { it.copy(establishmentMethod = method) } },
            enabled = enabled, error = errors["B2Q3"])
        YesNoQuestion("B2Q4", "Does the farmer use certified rice seeds?",
            selected = i.usesCertifiedSeeds,
            onSelected = { yes -> update { it.copy(usesCertifiedSeeds = yes) } },
            enabled = enabled, error = errors["B2Q4"])
        if (i.usesCertifiedSeeds == true) {
            TextQuestion("B2Q5", "Primary certified seed variety", i.seedVariety,
                onValueChange = { variety -> update { it.copy(seedVariety = variety) } },
                enabled = enabled, error = errors["B2Q5"], placeholder = "e.g. NSIC Rc 222")
        }
        HorizontalDivider()
        MultipleChoiceQuestion(
            selected = i.productionChallenges,
            onToggle = { challenge ->
                update { old ->
                    val selected = if (challenge in old.productionChallenges)
                        old.productionChallenges - challenge
                    else old.productionChallenges + challenge
                    old.copy(productionChallenges = selected)
                }
            }, enabled = enabled, error = errors["B2Q6"],
        )
        if (SurveyOptions.OTHER in i.productionChallenges) {
            TextQuestion("B2Q7", "Other challenge (please specify)", i.otherChallenge,
                onValueChange = { other -> update { it.copy(otherChallenge = other) } },
                enabled = enabled, error = errors["B2Q7"],
                placeholder = "Describe the other challenge")
        }
    }
}
