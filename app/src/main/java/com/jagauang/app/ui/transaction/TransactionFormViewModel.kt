package com.jagauang.app.ui.transaction

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jagauang.app.AppContainer
import com.jagauang.app.data.repository.WriteResult
import com.jagauang.app.domain.AmountParser
import com.jagauang.app.domain.Transaction
import com.jagauang.app.domain.TransactionType
import com.jagauang.app.domain.TransactionValidator
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

class TransactionFormViewModel(
    private val container: AppContainer,
    val isEdit: Boolean,
    private val id: String?,
    initialType: TransactionType,
    initialDay: LocalDate,
) : ViewModel() {

    var type by mutableStateOf(initialType)
        private set
    var amountText by mutableStateOf("")
        private set
    var day by mutableStateOf(initialDay)
        private set
    var timeMinutes by mutableStateOf(LocalTime.now().hour * 60 + LocalTime.now().minute)
        private set
    var categoryCode by mutableStateOf<String?>(null)
        private set
    var paymentMethodId by mutableStateOf<String?>(null)
        private set
    var transferSourceId by mutableStateOf<String?>(null)
        private set
    var transferDestinationId by mutableStateOf<String?>(null)
        private set
    var note by mutableStateOf("")
        private set
    var attachmentPath by mutableStateOf<String?>(null)
        private set
    var recurringCode by mutableStateOf<String?>(null)
        private set

    var amountError by mutableStateOf<String?>(null)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var transferError by mutableStateOf<String?>(null)
        private set
    var saving by mutableStateOf(false)
        private set
    var dirty by mutableStateOf(false)
        private set
    var saved by mutableStateOf(false)
        private set
    var gone by mutableStateOf(false)
        private set

    private var originalCreatedAtMs: Long? = null

    init {
        if (isEdit && id != null) {
            viewModelScope.launch {
                val existing = container.repository.getById(id)
                if (existing == null) {
                    gone = true
                } else {
                    type = existing.type
                    amountText = existing.amountIdr.toString()
                    day = existing.day
                    timeMinutes = existing.timeMinutes
                    categoryCode = existing.categoryCode
                    paymentMethodId = existing.paymentMethodId
                    transferSourceId = existing.transferSourceId
                    transferDestinationId = existing.transferDestinationId
                    note = existing.note ?: ""
                    attachmentPath = existing.attachmentPath
                    recurringCode = existing.recurringCode
                    originalCreatedAtMs = existing.createdAtMs
                }
            }
        }
    }

    fun appendDigit(d: Char) {
        if (amountText.length < 13) { amountText += d; amountError = null; dirty = true }
    }

    fun appendZeroes() { repeat(3) { if (amountText.length < 13) amountText += '0' }; amountError = null; dirty = true }

    fun backspace() { amountText = amountText.dropLast(1); amountError = null; dirty = true }

    fun onDayChange(d: LocalDate) { day = d; dirty = true }
    fun onTimeChange(m: Int) { timeMinutes = m; dirty = true }
    fun onCategoryChange(c: String?) { categoryCode = c; dirty = true }
    fun onPaymentMethodChange(id: String?) { paymentMethodId = id; dirty = true }
    fun onSourceChange(id: String?) { transferSourceId = id; transferError = null; dirty = true }
    fun onDestinationChange(id: String?) { transferDestinationId = id; transferError = null; dirty = true }
    fun onNoteChange(v: String) { note = v.take(TransactionValidator.MAX_NOTE_LENGTH); dirty = true }
    fun onVoiceResult(v: String) { note = (note + " " + v).trim().take(TransactionValidator.MAX_NOTE_LENGTH); dirty = true }
    fun onAttachmentChange(p: String?) { attachmentPath = p; dirty = true }
    fun onRecurringChange(c: String?) { recurringCode = c; dirty = true }

    fun save() {
        if (saving) return
        val amount = AmountParser.parse(amountText)
        if (amount == null) { amountError = amountProblem(amountText); return }
        if (!TransactionValidator.isDateInRange(day, LocalDate.now())) {
            error = "Pilih tanggal dari 1 Januari 1900 sampai hari ini."
            return
        }
        if (type == TransactionType.TRANSFER && !TransactionValidator.isTransferValid(transferSourceId, transferDestinationId)) {
            transferError = "Metode asal dan tujuan harus berbeda."
            return
        }
        error = null
        saving = true
        viewModelScope.launch {
            val t = Transaction(
                id = id ?: UUID.randomUUID().toString(),
                bookId = container.activeBookId.value,
                type = type,
                amountIdr = amount,
                day = day,
                timeMinutes = timeMinutes,
                categoryCode = if (type == TransactionType.TRANSFER) null else categoryCode,
                paymentMethodId = if (type == TransactionType.TRANSFER) null else paymentMethodId,
                transferSourceId = if (type == TransactionType.TRANSFER) transferSourceId else null,
                transferDestinationId = if (type == TransactionType.TRANSFER) transferDestinationId else null,
                note = TransactionValidator.normalizeNote(note),
                attachmentPath = attachmentPath,
                recurringCode = recurringCode,
                createdAtMs = originalCreatedAtMs ?: System.currentTimeMillis(),
                updatedAtMs = System.currentTimeMillis(),
            )
            val result = runCatching {
                if (isEdit) container.repository.update(t) else container.repository.add(t)
            }
            saving = false
            result.onSuccess { r ->
                when (r) {
                    WriteResult.Success -> {
                        container.successMessage.value = if (isEdit) "Transaksi diperbarui." else "Transaksi tersimpan."
                        saved = true
                    }
                    WriteResult.NotFound -> { error = "Transaksi sudah tidak tersedia."; gone = true }
                    is WriteResult.Invalid -> error = "Transaksi belum bisa disimpan."
                }
            }.onFailure {
                error = "Transaksi belum bisa disimpan."
            }
        }
    }

    private fun amountProblem(raw: String): String {
        val s = raw.trim()
        if (s.isEmpty()) return "Masukkan nominal antara Rp1 dan Rp999.999.999.999."
        val badChars = s.any { !it.isDigit() && it != '.' }
        val badEdges = s.first() == '.' || s.last() == '.' || s.contains("..")
        if (badChars || badEdges) return "Masukkan nominal dalam rupiah bulat, misalnya 125.000."
        val parts = s.split('.')
        val badGrouping = parts.size > 1 && (parts[0].length !in 1..3 || parts.drop(1).any { it.length != 3 })
        if (badGrouping) return "Masukkan nominal dalam rupiah bulat, misalnya 125.000."
        return "Masukkan nominal antara Rp1 dan Rp999.999.999.999."
    }
}
