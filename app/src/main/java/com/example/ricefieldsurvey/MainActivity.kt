package com.example.ricefieldsurvey

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ricefieldsurvey.di.SurveyContainer
import com.example.ricefieldsurvey.presentation.auth.AuthViewModel
import com.example.ricefieldsurvey.presentation.auth.LoginScreen
import com.example.ricefieldsurvey.presentation.editor.EditorViewModel
import com.example.ricefieldsurvey.presentation.editor.InterviewEditorScreen
import com.example.ricefieldsurvey.presentation.interviews.InterviewListScreen
import com.example.ricefieldsurvey.presentation.interviews.InterviewsViewModel
import com.example.ricefieldsurvey.presentation.theme.SurveyTheme
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SurveyRoot() }
    }
}

@Composable
private fun SurveyRoot() {
    val context = LocalContext.current.applicationContext as SurveyApplication
    val container: SurveyContainer = context.container
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.Factory(container.auth, container.login),
    )
    val auth by authViewModel.uiState.collectAsStateWithLifecycle()

    SurveyTheme {
        Surface(Modifier.fillMaxSize().safeDrawingPadding()) {
            val userId = auth.userId
            if (userId == null) {
                LoginScreen(error = auth.error, onLogIn = authViewModel::logIn)
            } else {
                // A new navigation stack is created when a different enumerator signs in.
                var route by rememberSaveable(userId) { mutableStateOf("list") }
                var activeInterviewId by rememberSaveable(userId) { mutableStateOf("") }

                if (route == "editor") {
                    val editor: EditorViewModel = viewModel(
                        key = "editor-$userId-$activeInterviewId",
                        factory = EditorViewModel.Factory(
                            container.getInterview, container.saveDraft, container.submitInterview,
                        ),
                    )
                    val state by editor.uiState.collectAsStateWithLifecycle()
                    LaunchedEffect(activeInterviewId, userId) {
                        editor.load(activeInterviewId, userId)
                    }
                    InterviewEditorScreen(
                        state = state,
                        onUpdate = editor::update,
                        onSave = { editor.saveAndLeave { route = "list" } },
                        onComplete = { onInvalid ->
                            editor.complete(onCompleted = { route = "list" }, onInvalid = onInvalid)
                        },
                    )
                } else {
                    val listing: InterviewsViewModel = viewModel(
                        key = "interviews-$userId",
                        factory = InterviewsViewModel.Factory(
                            container.observeInterviews, container.deleteDraft,
                        ),
                    )
                    LaunchedEffect(userId) { listing.forUser(userId) }
                    val interviews by listing.interviews.collectAsStateWithLifecycle()
                    val deletionFeedback by listing.deletionFeedback.collectAsStateWithLifecycle()
                    InterviewListScreen(
                        userId = userId,
                        interviews = interviews,
                        onNew = {
                            activeInterviewId = UUID.randomUUID().toString()
                            route = "editor"
                        },
                        onOpen = { id ->
                            activeInterviewId = id
                            route = "editor"
                        },
                        onDeleteDraft = listing::deleteDraft,
                        deletionFeedback = deletionFeedback,
                        onFeedbackShown = listing::clearDeletionFeedback,
                        onLogout = authViewModel::logOut,
                    )
                }
            }
        }
    }
}
