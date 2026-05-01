package com.complaints.app.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.complaints.app.data.model.Complaint
import com.complaints.app.data.model.StatsResponse
import com.complaints.app.data.repository.ComplaintRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Local edit state for inline editing a complaint
data class ComplaintEdit(
    val status:     String,
    val adminNotes: String
)

data class AdminPanelUiState(
    val complaints:  List<Complaint>        = emptyList(),
    val stats:       StatsResponse?         = null,
    val isLoading:   Boolean                = true,
    val error:       String                 = "",
    // Filters
    val sevFilter:   String                 = "All",
    val catFilter:   String                 = "All",
    val staFilter:   String                 = "All",
    // Per-complaint edit state (id → pending edit)
    val edits:       Map<String, ComplaintEdit> = emptyMap(),
    val saving:      Set<String>            = emptySet(),
    val saved:       Set<String>            = emptySet(),
    val snackMessage: String                = ""
)

val STATUS_OPTIONS   = listOf("pending", "in-progress", "resolved", "rejected")
val SEVERITY_OPTIONS = listOf("All", "High", "Medium", "Low", "Unclassified")
val CATEGORY_OPTIONS = listOf(
    "All", "IT Support", "Hostels", "Academics", "Fees / Finance",
    "Maintenance", "Transport", "Security / Discipline", "Administration", "Other"
)

class AdminPanelViewModel : ViewModel() {

    private val repo = ComplaintRepository()
    private val _uiState = MutableStateFlow(AdminPanelUiState())
    val uiState: StateFlow<AdminPanelUiState> = _uiState.asStateFlow()

    init { fetchAll() }

    fun fetchAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = "")
            val s = _uiState.value
            val complaintsResult = repo.getComplaints(
                severity = s.sevFilter.takeIf { it != "All" },
                category = s.catFilter.takeIf { it != "All" },
                status   = s.staFilter.takeIf { it != "All" }
            )
            val statsResult = repo.getStats()

            val newState = _uiState.value.copy(isLoading = false)
            _uiState.value = newState.copy(
                complaints = complaintsResult.getOrElse { emptyList() },
                stats      = statsResult.getOrNull(),
                error      = complaintsResult.exceptionOrNull()?.message ?: ""
            )
        }
    }

    fun setSevFilter(f: String) { _uiState.value = _uiState.value.copy(sevFilter = f); fetchAll() }
    fun setCatFilter(f: String) { _uiState.value = _uiState.value.copy(catFilter = f); fetchAll() }
    fun setStaFilter(f: String) { _uiState.value = _uiState.value.copy(staFilter = f); fetchAll() }

    /** Track inline edits for a specific complaint */
    fun onEditStatus(id: String, status: String) {
        val current = _uiState.value.edits[id]
        val complaint = _uiState.value.complaints.find { it.id == id }
        val notes = current?.adminNotes ?: complaint?.adminNotes ?: ""
        _uiState.value = _uiState.value.copy(
            edits = _uiState.value.edits + (id to ComplaintEdit(status, notes))
        )
    }

    fun onEditNotes(id: String, notes: String) {
        val current = _uiState.value.edits[id]
        val complaint = _uiState.value.complaints.find { it.id == id }
        val status = current?.status ?: complaint?.status ?: "pending"
        _uiState.value = _uiState.value.copy(
            edits = _uiState.value.edits + (id to ComplaintEdit(status, notes))
        )
    }

    fun getEditedStatus(id: String): String {
        return _uiState.value.edits[id]?.status
            ?: _uiState.value.complaints.find { it.id == id }?.status
            ?: "pending"
    }

    fun getEditedNotes(id: String): String {
        return _uiState.value.edits[id]?.adminNotes
            ?: _uiState.value.complaints.find { it.id == id }?.adminNotes
            ?: ""
    }

    fun saveEdit(id: String) {
        val edit = _uiState.value.edits[id] ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(saving = _uiState.value.saving + id)
            val result = repo.updateComplaint(id, edit.status, edit.adminNotes)
            result.fold(
                onSuccess = { updated ->
                    val newComplaints = _uiState.value.complaints.map { if (it.id == id) updated else it }
                    val newEdits = _uiState.value.edits - id
                    val newSaved = _uiState.value.saved + id
                    _uiState.value = _uiState.value.copy(
                        saving     = _uiState.value.saving - id,
                        complaints = newComplaints,
                        edits      = newEdits,
                        saved      = newSaved,
                        snackMessage = "Complaint updated."
                    )
                    // Clear "saved" indicator after 2s
                    kotlinx.coroutines.delay(2000)
                    _uiState.value = _uiState.value.copy(saved = _uiState.value.saved - id)
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(
                        saving       = _uiState.value.saving - id,
                        snackMessage = err.message ?: "Save failed."
                    )
                }
            )
        }
    }

    fun deleteComplaint(id: String) {
        viewModelScope.launch {
            repo.deleteComplaint(id).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        complaints   = _uiState.value.complaints.filter { it.id != id },
                        snackMessage = "Complaint deleted."
                    )
                    fetchAll() // refresh stats
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(snackMessage = err.message ?: "Delete failed.")
                }
            )
        }
    }

    fun clearSnack() { _uiState.value = _uiState.value.copy(snackMessage = "") }
}
