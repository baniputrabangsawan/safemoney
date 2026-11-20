package com.jagauang.app.ui.transaction

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jagauang.app.AppContainer
import com.jagauang.app.data.repository.WriteResult
import com.jagauang.app.domain.Transaction
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TransactionDetailViewModel(
    private val container: AppContainer,
    private val id: String,
) : ViewModel() {

    val transaction: StateFlow<Transaction?> = container.repository.observeById(id)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    var deleted by mutableStateOf(false)
        private set
    var deleteError by mutableStateOf(false)
        private set

    fun delete() {
        viewModelScope.launch {
            runCatching { container.repository.delete(id) }
                .onSuccess { r ->
                    when (r) {
                        WriteResult.Success, WriteResult.NotFound -> {
                            container.successMessage.value = "Transaksi dihapus."
                            deleted = true
                        }
                        else -> deleteError = true
                    }
                }
                .onFailure { deleteError = true }
        }
    }
}
