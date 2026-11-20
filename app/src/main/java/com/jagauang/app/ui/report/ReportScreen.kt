package com.jagauang.app.ui.report
import androidx.compose.foundation.border

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jagauang.app.AppContainer
import com.jagauang.app.domain.Category
import com.jagauang.app.ui.components.DonutChart
import com.jagauang.app.ui.components.PeriodNav
import com.jagauang.app.ui.components.chartColors
import com.jagauang.app.ui.formatRupiah
import com.jagauang.app.ui.theme.Expense
import com.jagauang.app.ui.theme.Income
import com.jagauang.app.ui.theme.ExpenseTint
import com.jagauang.app.ui.theme.IncomeTint
import com.jagauang.app.ui.theme.Ink
import com.jagauang.app.ui.theme.Control
import com.jagauang.app.ui.theme.Muted
import com.jagauang.app.ui.theme.Navy

private enum class ReportTab { SUMMARY, EXPENSE, INCOME }

@Composable
fun ReportScreen(container: AppContainer) {
    val period by container.period.collectAsStateWithLifecycle()
    val summary by container.summary.collectAsStateWithLifecycle()
    val categories by container.categories.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableStateOf(ReportTab.SUMMARY) }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Laporan", style = MaterialTheme.typography.headlineSmall, color = Navy)
        PeriodNav(
            period = period,
            onPrev = { container.period.value = container.period.value.shift(-1) },
            onNext = { container.period.value = container.period.value.shift(1) },
            onJump = {},
        )
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = tab == ReportTab.SUMMARY,
                onClick = { tab = ReportTab.SUMMARY },
                shape = SegmentedButtonDefaults.itemShape(0, 3),
            ) { Text("Ringkasan") }
            SegmentedButton(
                selected = tab == ReportTab.EXPENSE,
                onClick = { tab = ReportTab.EXPENSE },
                shape = SegmentedButtonDefaults.itemShape(1, 3),
            ) { Text("Pengeluaran") }
            SegmentedButton(
                selected = tab == ReportTab.INCOME,
                onClick = { tab = ReportTab.INCOME },
                shape = SegmentedButtonDefaults.itemShape(2, 3),
            ) { Text("Pemasukan") }
        }
        when (tab) {
            ReportTab.SUMMARY -> SummaryTab(summary.income, summary.expense)
            ReportTab.EXPENSE -> CategoryTab(summary.expenseByCategory, categories, "Pengeluaran")
            ReportTab.INCOME -> CategoryTab(summary.incomeByCategory, categories, "Pemasukan")
        }
    }
}

@Composable
private fun SummaryTab(income: Long, expense: Long) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 20.dp)) {
        TotalCard("Total Pemasukan", income, Income, IncomeTint)
        TotalCard("Total Pengeluaran", expense, Expense, ExpenseTint)
    }
}

@Composable
private fun TotalCard(label: String, amount: Long, accent: androidx.compose.ui.graphics.Color, tint: androidx.compose.ui.graphics.Color) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(tint).padding(18.dp),
    ) {
        Text(label, color = accent.copy(alpha = 0.85f), style = MaterialTheme.typography.bodyLarge)
        Text(formatRupiah(amount), color = accent, fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CategoryTab(byCategory: List<Pair<String, Long>>, categories: List<Category>, label: String) {
    val total = remember(byCategory) { byCategory.sumOf { it.second } }
    if (byCategory.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text("Belum ada transaksi $label pada periode ini.", color = Muted)
        }
        return
    }
    val slices = remember(byCategory) {
        byCategory.mapIndexed { i, (_, value) -> chartColors[i % chartColors.size] to value }
    }
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(top = 20.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Total $label", color = Muted, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        formatRupiah(total),
                        color = if (label == "Pemasukan") Income else Expense,
                        fontSize = 28.sp,
                        lineHeight = 36.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Box(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                            .padding(vertical = 12.dp)
                            .widthIn(max = 240.dp)
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .semantics(mergeDescendants = true) {
                                contentDescription = "Komposisi $label, ${byCategory.size} kategori. Rincian tersedia di bawah."
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        DonutChart(slices, Modifier.fillMaxSize())
                        Column(
                            modifier = Modifier.clearAndSetSemantics {},
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                "${byCategory.size}",
                                color = Navy,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text("kategori", color = Muted, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
        item {
            Text(
                "Rincian kategori",
                modifier = Modifier.padding(top = 8.dp),
                color = Navy,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        itemsIndexed(byCategory, key = { _, entry -> entry.first }) { index, (code, value) ->
            val name = categories.firstOrNull { it.code == code }?.name ?: "Tanpa kategori"
            val percent = if (total > 0) (value * 100 / total).toInt() else 0
            val share = if (value > 0 && percent == 0) "<1%" else "$percent%"
            Card(
                modifier = Modifier.fillMaxWidth().semantics(mergeDescendants = true) {},
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(12.dp).clip(CircleShape).background(chartColors[index % chartColors.size]).border(1.dp, Control.copy(alpha = 0.25f), CircleShape))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(name, color = Ink, fontWeight = FontWeight.Medium)
                        Text(
                            formatRupiah(value),
                            color = Ink,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(share, color = Muted, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
