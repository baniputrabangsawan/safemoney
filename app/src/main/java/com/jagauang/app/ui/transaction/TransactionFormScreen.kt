package com.jagauang.app.ui.transaction

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jagauang.app.AppContainer
import com.jagauang.app.domain.Category
import com.jagauang.app.domain.PaymentMethod
import com.jagauang.app.domain.Recurring
import com.jagauang.app.domain.TransactionType
import com.jagauang.app.domain.TransactionValidator
import com.jagauang.app.ui.categoryName
import com.jagauang.app.ui.components.ScreenTopBar
import com.jagauang.app.ui.formatDay
import com.jagauang.app.ui.formatRupiah
import com.jagauang.app.ui.formatTime
import com.jagauang.app.ui.paymentMethodName
import com.jagauang.app.ui.theme.Canvas
import com.jagauang.app.ui.theme.Expense
import com.jagauang.app.ui.theme.Ink
import com.jagauang.app.ui.theme.Muted
import com.jagauang.app.ui.theme.Navy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private const val MILLIS_PER_DAY = 86_400_000L

@Composable
fun TransactionFormScreen(
    container: AppContainer,
    type: TransactionType,
    day: Long,
    id: String?,
    onDone: () -> Unit,
    onBack: () -> Unit,
) {
    val vm: TransactionFormViewModel = viewModel(
        factory = remember(type, day, id) {
            viewModelFactory {
                initializer {
                    TransactionFormViewModel(
                        container = container,
                        isEdit = id != null,
                        id = id,
                        initialType = type,
                        initialDay = LocalDate.ofEpochDay(day),
                    )
                }
            }
        }
    )

    val saved = vm.saved
    val gone = vm.gone
    LaunchedEffect(saved) { if (saved) onDone() }
    LaunchedEffect(gone) { if (gone) onBack() }

    val categories by container.repository.observeCategories().collectAsStateWithLifecycle(emptyList())
    val methods by container.repository.observePaymentMethods().collectAsStateWithLifecycle(emptyList())

    var showDiscard by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    BackHandler(enabled = vm.dirty) { showDiscard = true }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { u ->
            scope.launch { vm.onAttachmentChange(withContext(Dispatchers.IO) { copyToInternal(context, u) }) }
        }
    }
    val speech = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        r.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.let { vm.onVoiceResult(it) }
    }

    val title = when {
        vm.isEdit -> "Ubah transaksi"
        vm.type == TransactionType.INCOME -> "Catat Pemasukan"
        vm.type == TransactionType.EXPENSE -> "Catat Pengeluaran"
        else -> "Transfer"
    }

    Column(Modifier.fillMaxSize().background(Canvas)) {
        ScreenTopBar(title, onBack = { if (vm.dirty) showDiscard = true else onBack() })

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item { AmountDisplay(vm.amountText) }
            item { AmountKeypad(vm::appendDigit, vm::appendZeroes, vm::backspace) }
            if (vm.amountError != null) item { Text(vm.amountError!!, color = Expense) }

            if (vm.type != TransactionType.TRANSFER) {
                item {
                    SelectField("Kategori", categoryName(categories, vm.categoryCode),
                        listOf("" to "Tanpa kategori") + categories.filter { it.type == vm.type }.map { it.code to it.name }) {
                        vm.onCategoryChange(it.ifEmpty { null })
                    }
                }
                item {
                    SelectField("Metode Pembayaran", paymentMethodName(methods, vm.paymentMethodId),
                        methods.map { it.id to it.name }) {
                        vm.onPaymentMethodChange(it.ifEmpty { null })
                    }
                }
            } else {
                item {
                    SelectField("Dari (asal)", paymentMethodName(methods, vm.transferSourceId),
                        methods.map { it.id to it.name }) { vm.onSourceChange(it.ifEmpty { null }) }
                }
                item {
                    SelectField("Ke (tujuan)", paymentMethodName(methods, vm.transferDestinationId),
                        methods.map { it.id to it.name }) { vm.onDestinationChange(it.ifEmpty { null }) }
                }
                if (vm.transferError != null) item { Text(vm.transferError!!, color = Expense) }
            }

            item {
                NoteField(
                    note = vm.note,
                    onNoteChange = vm::onNoteChange,
                    attachmentPath = vm.attachmentPath,
                    onRemoveAttachment = { vm.onAttachmentChange(null) },
                    onAttach = { pickImage.launch("image/*") },
                    onVoice = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
                        }
                        runCatching { speech.launch(intent) }
                    },
                )
            }
            item { ReadonlyField("Tanggal", formatDay(vm.day), Icons.Default.CalendarMonth) { showDatePicker = true } }
            item { ReadonlyField("Jam", formatTime(vm.timeMinutes), Icons.Default.Schedule) { showTimePicker = true } }
            if (vm.type != TransactionType.TRANSFER) {
                item {
                    SelectField("Pengingat Berulang", Recurring.fromCode(vm.recurringCode).label,
                        Recurring.entries.map { it.code to it.label }) {
                        vm.onRecurringChange(it.ifEmpty { null })
                    }
                }
            }
            if (vm.error != null) item { Text(vm.error!!, color = Expense) }
        }

        Button(
            onClick = vm::save,
            enabled = !vm.saving,
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 16.dp).height(56.dp),
            shape = RoundedCornerShape(14.dp),
        ) { Text(if (vm.saving) "Menyimpan…" else "Simpan", fontSize = 16.sp) }
    }

    if (showDatePicker) {
        SingleDatePickerDialog(
            initial = vm.day,
            onConfirm = { vm.onDayChange(it); showDatePicker = false },
            onDismiss = { showDatePicker = false },
        )
    }
    if (showTimePicker) {
        TimePickerDialog(
            initialMinutes = vm.timeMinutes,
            onConfirm = { vm.onTimeChange(it); showTimePicker = false },
            onDismiss = { showTimePicker = false },
        )
    }
    if (showDiscard) {
        AlertDialog(
            onDismissRequest = { showDiscard = false },
            title = { Text("Buang perubahan?") },
            text = { Text("Perubahan yang belum disimpan akan hilang.") },
            confirmButton = { TextButton(onClick = { showDiscard = false; onBack() }) { Text("Buang perubahan") } },
            dismissButton = { TextButton(onClick = { showDiscard = false }) { Text("Lanjut mengedit") } },
        )
    }
}

@Composable
private fun AmountDisplay(text: String) {
    val display = text.toLongOrNull()?.let { formatRupiah(it) } ?: "Rp 0"
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Navy)) {
        Text(
            display, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth().padding(20.dp),
        )
    }
}

@Composable
private fun AmountKeypad(onDigit: (Char) -> Unit, onZeroes: () -> Unit, onBackspace: () -> Unit) {
    val rows = listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"), listOf("000", "0", "⌫"))
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { key ->
                    KeyButton(key, Modifier.weight(1f)) {
                        when (key) {
                            "⌫" -> onBackspace()
                            "000" -> onZeroes()
                            else -> onDigit(key[0])
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyButton(label: String, modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier.height(52.dp), shape = RoundedCornerShape(12.dp), color = Color.White) {
        Box(contentAlignment = Alignment.Center) {
            if (label == "⌫") Icon(Icons.Default.Backspace, null, tint = Navy)
            else Text(label, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Ink)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectField(label: String, selectedLabel: String, options: List<Pair<String, String>>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedLabel, onValueChange = {}, readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (id, lbl) ->
                DropdownMenuItem(text = { Text(lbl) }, onClick = { onSelect(id); expanded = false })
            }
        }
    }
}

@Composable
private fun NoteField(
    note: String,
    onNoteChange: (String) -> Unit,
    attachmentPath: String?,
    onRemoveAttachment: () -> Unit,
    onAttach: () -> Unit,
    onVoice: () -> Unit,
) {
    OutlinedTextField(note, onNoteChange, label = { Text("Catatan (opsional)") }, minLines = 2, modifier = Modifier.fillMaxWidth())
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
        OutlinedButton(onClick = onAttach) {
            Icon(Icons.Default.AttachFile, null)
            Spacer(Modifier.width(4.dp)); Text("Struk")
        }
        OutlinedButton(onClick = onVoice) {
            Icon(Icons.Default.Mic, null)
            Spacer(Modifier.width(4.dp)); Text("Suara")
        }
    }
    attachmentPath?.let { path ->
        val bitmap = remember(path) { runCatching { BitmapFactory.decodeFile(path) }.getOrNull() }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
            if (bitmap != null) {
                Image(bitmap.asImageBitmap(), "Struk", modifier = Modifier.size(72.dp).clip(RoundedCornerShape(8.dp)))
                Spacer(Modifier.width(8.dp))
            }
            Text("Struk terlampir", color = Muted, fontSize = 13.sp)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onRemoveAttachment) { Text("Hapus") }
        }
    }
}

@Composable
private fun ReadonlyField(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box {
        OutlinedTextField(
            value = value, onValueChange = {}, readOnly = true, label = { Text(label) },
            trailingIcon = { Icon(icon, null) },
            modifier = Modifier.fillMaxWidth(),
        )
        Box(Modifier.matchParentSize().clickable(onClick = onClick))
    }
}

private fun copyToInternal(context: Context, uri: Uri): String? = runCatching {
    val dir = File(context.filesDir, "receipts").apply { mkdirs() }
    val file = File(dir, "struk_${System.currentTimeMillis()}.jpg")
    context.contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(file).use { out -> input.copyTo(out) }
    }
    file.absolutePath
}.getOrNull()

private fun utcMillisToLocalDate(millis: Long): LocalDate =
    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SingleDatePickerDialog(initial: LocalDate, onConfirm: (LocalDate) -> Unit, onDismiss: () -> Unit) {
    val selectableDates = remember {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val d = utcMillisToLocalDate(utcTimeMillis)
                return !d.isBefore(TransactionValidator.MIN_DATE) && !d.isAfter(LocalDate.now())
            }

            override fun isSelectableYear(year: Int): Boolean =
                year in TransactionValidator.MIN_DATE.year..LocalDate.now().year
        }
    }
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initial.toEpochDay() * MILLIS_PER_DAY,
        selectableDates = selectableDates,
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = { state.selectedDateMillis?.let { onConfirm(utcMillisToLocalDate(it)) } }) { Text("OK") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } },
    ) { DatePicker(state = state) }
}

@Composable
private fun TimePickerDialog(initialMinutes: Int, onConfirm: (Int) -> Unit, onDismiss: () -> Unit) {
    var hour by remember { mutableStateOf(initialMinutes / 60) }
    var minute by remember { mutableStateOf(initialMinutes % 60) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pilih jam") },
        text = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                WheelPicker((0..23).toList(), hour) { hour = it }
                Text(":", fontSize = 24.sp, modifier = Modifier.padding(horizontal = 8.dp))
                WheelPicker((0..59).toList(), minute) { minute = it }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(hour * 60 + minute) }) { Text("OK") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } },
    )
}

@Composable
private fun WheelPicker(items: List<Int>, selected: Int, onSelect: (Int) -> Unit) {
    LazyColumn(Modifier.height(160.dp).width(64.dp)) {
        items(items) { v ->
            val isSel = v == selected
            Box(Modifier.fillMaxWidth().clickable { onSelect(v) }.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                Text("%02d".format(v), color = if (isSel) Navy else Muted, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}
