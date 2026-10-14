package com.example.ricefieldsurvey.presentation.interviews

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ricefieldsurvey.domain.model.Interview
import com.example.ricefieldsurvey.domain.model.InterviewStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InterviewListScreen(
    userId: String,
    interviews: List<Interview>,
    onNew: () -> Unit,
    onOpen: (String) -> Unit,
    onLogout: () -> Unit,
) {
    var search by rememberSaveable { mutableStateOf("") }
    var tab by rememberSaveable { mutableStateOf("All") }
    val filtered = interviews.filter { interview ->
        (tab == "All" || (tab == "Drafts" && interview.status == InterviewStatus.DRAFT) ||
            (tab == "Completed" && interview.status == InterviewStatus.COMPLETED)) &&
            (search.isBlank() || interview.farmerName.contains(search, ignoreCase = true) ||
                interview.municipality.contains(search, ignoreCase = true))
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNew, containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary) {
                Text("+ New interview", Modifier.padding(horizontal = 18.dp),
                    style = MaterialTheme.typography.labelLarge)
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Surface(color = MaterialTheme.colorScheme.primary) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Field interviews", color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold)
                            Text("Signed in as $userId", color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.bodyMedium)
                        }
                        TextButton(onClick = onLogout) {
                            Text("Log out", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                        Text("●  Offline ready  •  Saved on this device",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.labelLarge)
                    }
                    Spacer(Modifier.height(18.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SummaryCount("Total", interviews.size, Modifier.weight(1f))
                        SummaryCount("Drafts", interviews.count { it.status == InterviewStatus.DRAFT }, Modifier.weight(1f))
                        SummaryCount("Done", interviews.count { it.status == InterviewStatus.COMPLETED }, Modifier.weight(1f))
                    }
                }
            }
            Column(Modifier.padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = search, onValueChange = { search = it },
                    modifier = Modifier.fillMaxWidth(), label = { Text("Search farmer or municipality") },
                    singleLine = true,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All", "Drafts", "Completed").forEach { filter ->
                        FilterChip(selected = tab == filter, onClick = { tab = filter },
                            label = { Text(filter) })
                    }
                }
            }
            if (filtered.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth().padding(30.dp),
                    contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🌱", style = MaterialTheme.typography.headlineLarge)
                        Spacer(Modifier.height(8.dp))
                        Text(if (interviews.isEmpty()) "No interviews yet" else "No matching interviews",
                            style = MaterialTheme.typography.titleMedium)
                        Text(if (interviews.isEmpty()) "Tap New interview to begin collecting data."
                            else "Try another search or filter.",
                            style = MaterialTheme.typography.bodyMedium)
                        if (interviews.isEmpty()) {
                            Spacer(Modifier.height(18.dp))
                            Button(onClick = onNew) { Text("Start first interview") }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp, end = 16.dp, bottom = 110.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(filtered, key = { it.id }) { interview ->
                        InterviewCard(interview) { onOpen(interview.id) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCount(label: String, value: Int, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f)) {
        Column(Modifier.padding(12.dp)) {
            Text(value.toString(), color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(label, color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun InterviewCard(interview: Interview, onClick: () -> Unit) {
    val completed = interview.status == InterviewStatus.COMPLETED
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(interview.farmerName.ifBlank { "Unnamed farmer" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold, maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                    Text(interview.municipality.ifBlank { "Location not entered" } +
                        if (interview.province.isBlank()) "" else ", ${interview.province}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary)
                }
                Spacer(Modifier.width(8.dp))
                Surface(
                    color = if (completed) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(50.dp),
                ) {
                    Text(if (completed) "Completed" else "Draft",
                        Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelMedium)
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Updated ${formatTimestamp(interview.updatedAt)}",
                    style = MaterialTheme.typography.bodySmall)
                Text(if (completed) "View →" else "Resume →",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

private fun formatTimestamp(epoch: Long): String =
    SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault()).format(Date(epoch))
