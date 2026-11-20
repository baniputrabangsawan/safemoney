package com.jagauang.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jagauang.app.AppContainer
import com.jagauang.app.domain.Book
import com.jagauang.app.domain.Period
import com.jagauang.app.domain.periodLabel
import com.jagauang.app.ui.components.TransactionRow
import com.jagauang.app.ui.components.toRowUi
import com.jagauang.app.ui.formatRupiah
import com.jagauang.app.ui.theme.Blue
import com.jagauang.app.ui.theme.BlueTint
import com.jagauang.app.ui.theme.ExpenseTint
import com.jagauang.app.ui.theme.Indigo
import com.jagauang.app.ui.theme.IncomeTint
import com.jagauang.app.ui.theme.Violet
import com.jagauang.app.ui.theme.VioletTint
import com.jagauang.app.ui.theme.Expense
import com.jagauang.app.ui.theme.Income
import com.jagauang.app.ui.theme.Ink
import com.jagauang.app.ui.theme.Muted
import com.jagauang.app.ui.theme.Navy
import com.jagauang.app.ui.theme.NavyLight

@Composable
fun HomeScreen(
    container: AppContainer,
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onTransfer: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenDetail: (String) -> Unit,
) {
    val books by container.books.collectAsStateWithLifecycle()
    val activeBookId by container.activeBookId.collectAsStateWithLifecycle()
    val period by container.period.collectAsStateWithLifecycle()
    val summary by container.summary.collectAsStateWithLifecycle()
    val transactions by container.transactions.collectAsStateWithLifecycle()
    val categories by container.categories.collectAsStateWithLifecycle()
    val methods by container.methods.collectAsStateWithLifecycle()

    val book = books.firstOrNull { it.id == activeBookId }
    val recent = remember(transactions) { transactions.take(8) }

    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("Jaga Uang", style = MaterialTheme.typography.headlineSmall, color = Navy) }
        item { BookPicker(books, book) { container.activeBookId.value = it } }
        item { QuickGrid(onAddIncome, onAddExpense, onTransfer, onOpenHistory) }
        item { SummaryCard(period, summary.income, summary.expense) }
        item { Text("Transaksi terbaru", style = MaterialTheme.typography.titleLarge, color = Ink) }
        if (recent.isEmpty()) {
            item { EmptyText("Belum ada transaksi pada buku ini.") }
        } else {
            items(recent, key = { it.id }) { t ->
                TransactionRow(t.toRowUi(categories, methods, includeDay = true)) { onOpenDetail(t.id) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookPicker(books: List<Book>, selected: Book?, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Buku catatan") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            books.forEach { b ->
                DropdownMenuItem(text = { Text(b.name) }, onClick = { onSelect(b.id); expanded = false })
            }
        }
    }
}

@Composable
private fun QuickGrid(onAddIncome: () -> Unit, onAddExpense: () -> Unit, onTransfer: () -> Unit, onOpenHistory: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickAction("Catat Pemasukan", Icons.Default.AddCircleOutline, Income, IncomeTint, Modifier.weight(1f), onAddIncome)
            QuickAction("Catat Pengeluaran", Icons.Default.ArrowUpward, Expense, ExpenseTint, Modifier.weight(1f), onAddExpense)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickAction("Transfer", Icons.Default.SwapHoriz, Blue, BlueTint, Modifier.weight(1f), onTransfer)
            QuickAction("Lihat Transaksi", Icons.Default.ListAlt, Violet, VioletTint, Modifier.weight(1f), onOpenHistory)
        }
    }
}

@Composable
private fun QuickAction(
    label: String,
    icon: ImageVector,
    accent: Color,
    tint: Color,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier.heightIn(min = 112.dp),
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = tint),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.Center) {
            Box(
                Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.padding(top = 12.dp))
            Text(label, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp)
        }
    }
}

@Composable
private fun SummaryCard(period: Period, income: Long, expense: Long) {
    Box(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(Indigo, Navy)))
            .padding(20.dp)
    ) {
        Column {
            Text(periodLabel(period), color = Color.White.copy(alpha = 0.78f), fontSize = 15.sp)
            Row(
                Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SummaryChip("Pemasukan", income, Color(0xFF34D399), Modifier.weight(1f))
                SummaryChip("Pengeluaran", expense, Color(0xFFFB7185), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SummaryChip(label: String, amount: Long, dot: Color, modifier: Modifier) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = 0.12f)).padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(dot))
            Spacer(Modifier.width(6.dp))
            Text(label, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
        }
        Text(formatRupiah(amount), color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun EmptyText(text: String) {
    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(text, color = Muted)
    }
}
