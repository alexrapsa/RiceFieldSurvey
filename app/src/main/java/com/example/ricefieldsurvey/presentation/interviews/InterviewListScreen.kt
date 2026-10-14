package com.example.ricefieldsurvey.presentation.interviews

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.ricefieldsurvey.domain.model.Interview
import com.example.ricefieldsurvey.domain.model.InterviewStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.offset

@Composable
fun InterviewListScreen(
    userId: String,
    interviews: List<Interview>,
    onNew: () -> Unit,
    onOpen: (String) -> Unit,
    onDeleteDraft: (String) -> Unit,
    deletionFeedback: String?,
    onFeedbackShown: () -> Unit,
    onLogout: () -> Unit,
) {
    var search by rememberSaveable(userId) { mutableStateOf("") }
    var tab by rememberSaveable(userId) { mutableStateOf("All") }
    var pendingDeletion by remember { mutableStateOf<Interview?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(deletionFeedback) {
        if (deletionFeedback != null) {
            // A Room Flow refresh will remove the deleted draft without manual list mutation.
            snackbarHostState.showSnackbar(deletionFeedback)
            onFeedbackShown()
        }
    }

    val filtered = interviews.filter { interview ->
        (tab == "All" || (tab == "Drafts" && interview.status == InterviewStatus.DRAFT) ||
            (tab == "Completed" && interview.status == InterviewStatus.COMPLETED)) &&
            (search.isBlank() || interview.farmerName.contains(search, ignoreCase = true) ||
                interview.municipality.contains(search, ignoreCase = true))
    }

    // A swipe only reveals the Delete action; deletion requires a tap and confirmation.
    pendingDeletion?.let { interview ->
        AlertDialog(
            onDismissRequest = { pendingDeletion = null },
            title = { Text("Delete saved draft?") },
            text = {
                Text("Delete the draft for ${interview.farmerName.ifBlank { "Unnamed farmer" }}? " +
                    "This permanently removes the locally saved interview and cannot be undone.")
            },
            confirmButton = {
                TextButton(onClick = {
                    pendingDeletion = null
                    onDeleteDraft(interview.id)
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeletion = null }) { Text("Cancel") }
            },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp, bottom = 110.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(filtered, key = { it.id }) { interview ->
                        SwipeableInterviewRow(
                            interview = interview,
                            onOpen = { onOpen(interview.id) },
                            onDelete = { pendingDeletion = interview },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SwipeableInterviewRow(
    interview: Interview,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
) {
    if (interview.status == InterviewStatus.COMPLETED) {
        InterviewCard(interview, onClick = onOpen)
        return
    }

    val revealWidth = 104.dp
    val revealWidthPx = with(LocalDensity.current) { revealWidth.toPx() }
    var offsetPx by remember(interview.id) { mutableFloatStateOf(0f) }
    var isDragging by remember(interview.id) { mutableStateOf(false) }
    val animatedOffset by animateFloatAsState(
        targetValue = offsetPx,
        animationSpec = tween(durationMillis = 180),
        label = "Draft delete action",
    )

    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))) {
        // This action remains behind the row until it is swiped to the left.
        Row(
            modifier = Modifier.matchParentSize(),
            horizontalArrangement = Arrangement.End,
        ) {
            Surface(
                modifier = Modifier.width(revealWidth).fillMaxHeight().clickable(onClick = onDelete),
                color = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
                shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text("Delete", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        InterviewCard(
            interview = interview,
            onClick = {
                // Opening is still possible after the action has been revealed.
                if (offsetPx == 0f) onOpen() else offsetPx = 0f
            },
            modifier = Modifier
                .offset { IntOffset((if (isDragging) offsetPx else animatedOffset).roundToInt(), 0) }
                .pointerInput(interview.id, revealWidthPx) {
                    detectHorizontalDragGestures(
                        onDragStart = { isDragging = true },
                        onHorizontalDrag = { change, amount ->
                            change.consume()
                            offsetPx = (offsetPx + amount).coerceIn(-revealWidthPx, 0f)
                        },
                        onDragCancel = {
                            isDragging = false
                            offsetPx = 0f
                        },
                        onDragEnd = {
                            isDragging = false
                            offsetPx = if (offsetPx < -revealWidthPx * 0.4f) -revealWidthPx else 0f
                        },
                    )
                },
        )
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
private fun InterviewCard(
    interview: Interview,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val completed = interview.status == InterviewStatus.COMPLETED
    Card(modifier.fillMaxWidth().clickable(onClick = onClick)) {
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
