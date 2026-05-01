package com.complaints.app.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.complaints.app.data.repository.ComplaintRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SubmitComplaintUiState(
    val title:           String  = "",
    val description:     String  = "",
    val category:        String  = "",
    val titleError:      String  = "",
    val descError:       String  = "",
    val categoryError:   String  = "",
    val apiError:        String  = "",
    val successMessage:  String  = "",
    val isLoading:       Boolean = false,
    val navigateBack:    Boolean = false
)

val COMPLAINT_CATEGORIES = listOf(
    "IT Support", "Hostels", "Academics", "Fees / Finance",
    "Maintenance", "Transport", "Security / Discipline", "Administration", "Other"
)

class SubmitComplaintViewModel : ViewModel() {

    private val repo = ComplaintRepository()

    private val _uiState = MutableStateFlow(SubmitComplaintUiState())
    val uiState: StateFlow<SubmitComplaintUiState> = _uiState.asStateFlow()

    fun onTitleChange(v: String)    { _uiState.value = _uiState.value.copy(title = v, titleError = "") }
    fun onDescChange(v: String)     { _uiState.value = _uiState.value.copy(description = v, descError = "") }
    fun onCategoryChange(v: String) { _uiState.value = _uiState.value.copy(category = v, categoryError = "") }

    fun submit() {
        val s = _uiState.value
        var tErr = ""; var dErr = ""; var cErr = ""
        if (s.title.trim().length < 5)          tErr = "Title must be at least 5 characters"
        if (s.description.trim().length < 20)   dErr = "Description must be at least 20 characters"
        if (s.category.isBlank())               cErr = "Please select a category"

        if (tErr.isNotEmpty() || dErr.isNotEmpty() || cErr.isNotEmpty()) {
            _uiState.value = s.copy(titleError = tErr, descError = dErr, categoryError = cErr)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, apiError = "", successMessage = "")
            repo.submitComplaint(s.title.trim(), s.description.trim(), s.category).fold(
                onSuccess = { complaint ->
                    _uiState.value = _uiState.value.copy(
                        isLoading      = false,
                        successMessage = "✅ Complaint submitted! AI classified severity as: ${complaint.severity}",
                        title          = "",
                        description    = "",
                        category       = ""
                    )
                    // Auto-navigate after 2 seconds
                    kotlinx.coroutines.delay(2000)
                    _uiState.value = _uiState.value.copy(navigateBack = true)
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        apiError  = err.message ?: "Submission failed. Try again."
                    )
                }
            )
        }
    }

    fun clearNavigation() { _uiState.value = _uiState.value.copy(navigateBack = false) }
}
