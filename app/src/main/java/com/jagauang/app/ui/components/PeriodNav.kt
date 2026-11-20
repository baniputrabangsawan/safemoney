package com.jagauang.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jagauang.app.domain.Period
import com.jagauang.app.domain.PeriodType
import com.jagauang.app.domain.periodLabel
import com.jagauang.app.ui.theme.Ink
import com.jagauang.app.ui.theme.Muted
import com.jagauang.app.ui.theme.Navy

@Composable
fun PeriodTypeChips(selected: PeriodType, onSelect: (PeriodType) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PeriodType.entries.forEach { type ->
            FilterChip(selected = selected == type, onClick = { onSelect(type) }, label = { Text(type.label) })
        }
    }
}

@Composable
fun PeriodNav(period: Period, onPrev: () -> Unit, onNext: () -> Unit, onJump: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onPrev, modifier = Modifier.semantics { contentDescription = "Periode sebelumnya" }) {
            Icon(Icons.Default.ChevronLeft, null, tint = Navy)
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onJump)
                .semantics { contentDescription = "Periode aktif" },
        ) {
            Text(periodLabel(period), color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text("Ketuk untuk memilih tanggal", color = Muted, fontSize = 12.sp)
        }
        IconButton(onClick = onNext, modifier = Modifier.semantics { contentDescription = "Periode berikutnya" }) {
            Icon(Icons.Default.ChevronRight, null, tint = Navy)
        }
    }
}
