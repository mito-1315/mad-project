package com.complaints.app.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.complaints.app.data.model.Complaint
import com.complaints.app.data.repository.ComplaintRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StudentDashboardUiState(
    val complaints:  List<Complaint> = emptyList(),
    val isLoading:   Boolean         = true,
    val error:       String          = "",
    val statusFilter: String         = "all"   // "all" | "pending" | "in-progress" | "resolved" | "rejected"
) {
    val filtered: List<Complaint>
        get() = if (statusFilter == "all") complaints
                else complaints.filter { it.status == statusFilter }

    val totalCount:   Int get() = complaints.size
    val pendingCount: Int get() = complaints.count { it.status == "pending" }
    val resolvedCount:Int get() = complaints.count { it.status == "resolved" }
    val highSevCount: Int get() = complaints.count { it.severity == "High" }
}

class StudentDashboardViewModel : ViewModel() {

    private val repo = ComplaintRepository()

    private val _uiState = MutableStateFlow(StudentDashboardUiState())
    val uiState: StateFlow<StudentDashboardUiState> = _uiState.asStateFlow()

    init { fetchComplaints() }

    fun fetchComplaints() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = "")
            repo.getComplaints().fold(
                onSuccess = { list ->
                    _uiState.value = _uiState.value.copy(isLoading = false, complaints = list)
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error     = err.message ?: "Failed to load complaints."
                    )
                }
            )
        }
    }

    fun setFilter(filter: String) {
        _uiState.value = _uiState.value.copy(statusFilter = filter)
    }
}
