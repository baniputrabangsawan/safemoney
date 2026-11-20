package com.jagauang.app.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jagauang.app.AppContainer
import com.jagauang.app.domain.TransactionValidator
import com.jagauang.app.ui.components.PeriodNav
import com.jagauang.app.ui.components.PeriodTypeChips
import com.jagauang.app.ui.components.TransactionRow
import com.jagauang.app.ui.components.toRowUi
import com.jagauang.app.ui.formatDay
import com.jagauang.app.ui.formatRupiah
import com.jagauang.app.ui.theme.Expense
import com.jagauang.app.ui.theme.Income
import com.jagauang.app.ui.theme.ExpenseTint
import com.jagauang.app.ui.theme.IncomeTint
import com.jagauang.app.ui.theme.Ink
import com.jagauang.app.ui.theme.Muted
import com.jagauang.app.ui.theme.Navy
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private const val MILLIS_PER_DAY = 86_400_000L

@Composable
fun HistoryScreen(
    container: AppContainer,
    onOpenDetail: (String) -> Unit,
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onTransfer: () -> Unit,
) {
    val period by container.period.collectAsStateWithLifecycle()
    val summary by container.summary.collectAsStateWithLifecycle()
    val transactions by container.transactions.collectAsStateWithLifecycle()
    val categories by container.categories.collectAsStateWithLifecycle()
    val methods by container.methods.collectAsStateWithLifecycle()

    val grouped = remember(transactions) { transactions.groupBy { it.day }.toList() }
    var showDatePicker by remember { mutableStateOf(false) }
    var showAddMenu by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Text("Riwayat", style = MaterialTheme.typography.headlineSmall, color = Navy) }
            item { PeriodTypeChips(period.type) { container.period.value = container.period.value.copy(type = it) } }
            item {
                PeriodNav(
                    period = period,
                    onPrev = { container.period.value = container.period.value.shift(-1) },
                    onNext = { container.period.value = container.period.value.shift(1) },
                    onJump = { showDatePicker = true },
                )
            }
            item { TotalsRow(summary.income, summary.expense) }
            if (transactions.isEmpty()) {
                item { EmptyHistory() }
            } else {
                grouped.forEach { (day, list) ->
                    item(key = "header-${day.toEpochDay()}") {
                        Text(formatDay(day), color = Muted, style = MaterialTheme.typography.bodyMedium)
                    }
                    items(list, key = { it.id }) { t ->
                        TransactionRow(t.toRowUi(categories, methods, includeDay = false)) { onOpenDetail(t.id) }
                    }
                }
            }
        }

        Box(Modifier.align(Alignment.BottomEnd).padding(20.dp)) {
            ExtendedFloatingActionButton(
                onClick = { showAddMenu = true },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Catat") },
            )
            DropdownMenu(expanded = showAddMenu, onDismissRequest = { showAddMenu = false }) {
                DropdownMenuItem(text = { Text("Catat pemasukan") }, onClick = { showAddMenu = false; onAddIncome() })
                DropdownMenuItem(text = { Text("Catat pengeluaran") }, onClick = { showAddMenu = false; onAddExpense() })
                DropdownMenuItem(text = { Text("Transfer") }, onClick = { showAddMenu = false; onTransfer() })
            }
        }
    }

    if (showDatePicker) {
        SingleDatePickerDialog(
            onConfirm = {
                container.period.value = container.period.value.copy(anchor = it)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
    }
}

@Composable
private fun TotalsRow(income: Long, expense: Long) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        TotalCard("Pemasukan", income, Income, IncomeTint, Modifier.weight(1f))
        TotalCard("Pengeluaran", expense, Expense, ExpenseTint, Modifier.weight(1f))
    }
}

@Composable
private fun TotalCard(label: String, amount: Long, accent: Color, tint: Color, modifier: Modifier) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(16.dp)).background(tint).padding(14.dp),
    ) {
        Text(label, color = accent.copy(alpha = 0.85f), fontSize = 13.sp)
        Text(formatRupiah(amount), color = accent, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun EmptyHistory() {
    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Text("Tidak ada transaksi pada periode ini.", color = Muted)
    }
}

private fun utcMillisToLocalDate(millis: Long): LocalDate =
    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SingleDatePickerDialog(onConfirm: (LocalDate) -> Unit, onDismiss: () -> Unit) {
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
        initialSelectedDateMillis = LocalDate.now().toEpochDay() * MILLIS_PER_DAY,
        selectableDates = selectableDates,
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { state.selectedDateMillis?.let { onConfirm(utcMillisToLocalDate(it)) } }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } },
    ) { DatePicker(state = state) }
}
