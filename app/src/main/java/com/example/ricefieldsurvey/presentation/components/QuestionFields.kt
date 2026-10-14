package com.example.ricefieldsurvey.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ricefieldsurvey.domain.model.SurveyOptions

@Composable
fun QuestionHeader(id: String, label: String, required: Boolean, error: String? = null) {
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(id, style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary)
            if (required) Text("REQUIRED", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary)
        }
        Spacer(Modifier.height(5.dp))
        Text(label, style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold)
        if (error != null) {
            Spacer(Modifier.height(3.dp))
            Text(error, color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun TextQuestion(
    id: String,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    required: Boolean = true,
    enabled: Boolean = true,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    placeholder: String = "Enter response",
) {
    Column(Modifier.fillMaxWidth()) {
        QuestionHeader(id, label, required, error)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value, onValueChange = onValueChange, enabled = enabled,
            placeholder = { Text(placeholder) }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            isError = error != null, modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun ChoiceQuestion(
    id: String,
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
    required: Boolean = true,
    enabled: Boolean = true,
    error: String? = null,
) {
    Column(Modifier.fillMaxWidth()) {
        QuestionHeader(id, label, required, error)
        Spacer(Modifier.height(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)) {
            options.forEach { option ->
                FilterChip(
                    selected = option == selected,
                    onClick = { onSelected(option) },
                    enabled = enabled,
                    label = { Text(option) },
                )
            }
        }
    }
}

@Composable
fun YesNoQuestion(
    id: String, label: String, selected: Boolean?,
    onSelected: (Boolean) -> Unit, enabled: Boolean,
    error: String? = null,
) {
    ChoiceQuestion(
        id = id, label = label, options = listOf("Yes", "No"),
        selected = when (selected) { true -> "Yes"; false -> "No"; null -> "" },
        onSelected = { onSelected(it == "Yes") }, enabled = enabled, error = error,
    )
}

@Composable
fun MultipleChoiceQuestion(
    selected: Set<String>, onToggle: (String) -> Unit,
    enabled: Boolean, error: String?,
) {
    Column {
        QuestionHeader("B2Q6", "What are the farmer's main rice production challenges?",
            required = true, error = error)
        Text("Select all that apply", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.height(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SurveyOptions.challenges.forEach { option ->
                FilterChip(selected = option in selected, onClick = { onToggle(option) },
                    enabled = enabled, label = { Text(option) })
            }
        }
    }
}

@Composable
fun ProvinceQuestion(value: String, onSelected: (String) -> Unit,
                     enabled: Boolean, error: String?) {
    var showPicker by rememberSaveable { mutableStateOf(false) }
    Column {
        QuestionHeader("B1Q4", "Province", required = true, error = error)
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = { showPicker = true }, enabled = enabled,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(if (value.isBlank()) "Choose a province  ▾" else "$value  ▾")
        }
    }

    if (showPicker) {
        var query by rememberSaveable { mutableStateOf("") }
        val matches = SurveyOptions.provinces.filter { it.contains(query.trim(), ignoreCase = true) }
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text("Select province") },
            text = {
                Column {
                    OutlinedTextField(value = query, onValueChange = { query = it },
                        placeholder = { Text("Search provinces") },
                        singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(Modifier.height(300.dp)) {
                        items(matches) { province ->
                            TextButton(onClick = {
                                onSelected(province)
                                showPicker = false
                            }, modifier = Modifier.fillMaxWidth()) {
                                Text(province, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showPicker = false }) { Text("Cancel") } },
        )
    }
}

@Composable
fun QuestionBlock(title: String, instructions: String, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Column {
                Text(title, style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(5.dp))
                Text(instructions, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary)
            }
            content()
        }
    }
}
