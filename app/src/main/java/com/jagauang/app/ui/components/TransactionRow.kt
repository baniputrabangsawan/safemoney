package com.jagauang.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jagauang.app.domain.Category
import com.jagauang.app.domain.PaymentMethod
import com.jagauang.app.domain.Transaction
import com.jagauang.app.domain.TransactionType
import com.jagauang.app.ui.categoryName
import com.jagauang.app.ui.formatDayShort
import com.jagauang.app.ui.formatRupiah
import com.jagauang.app.ui.formatTime
import com.jagauang.app.ui.paymentMethodName
import com.jagauang.app.ui.theme.Expense
import com.jagauang.app.ui.theme.Income
import com.jagauang.app.ui.theme.Ink
import com.jagauang.app.ui.theme.Muted

data class TransactionRowUi(
    val id: String,
    val title: String,
    val subtitle: String,
    val amountText: String,
    val color: Color,
    val icon: ImageVector,
    val typeLabel: String,
)

fun Transaction.toRowUi(
    categories: List<Category>,
    methods: List<PaymentMethod>,
    includeDay: Boolean,
): TransactionRowUi {
    val title = when (type) {
        TransactionType.TRANSFER -> "Transfer ${paymentMethodName(methods, transferSourceId)} → ${paymentMethodName(methods, transferDestinationId)}"
        else -> categoryName(categories, categoryCode)
    }
    val subtitleParts = mutableListOf<String>()
    if (includeDay) subtitleParts.add(formatDayShort(day))
    subtitleParts.add(formatTime(timeMinutes))
    if (type != TransactionType.TRANSFER) {
        paymentMethodName(methods, paymentMethodId).takeIf { it.isNotEmpty() }?.let { subtitleParts.add(it) }
    }
    if (!note.isNullOrBlank()) subtitleParts.add(note!!)

    return when (type) {
        TransactionType.INCOME -> TransactionRowUi(
            id = id, title = title, subtitle = subtitleParts.joinToString(" · "),
            amountText = "+" + formatRupiah(amountIdr), color = Income, icon = Icons.Default.ArrowDownward, typeLabel = "Pemasukan",
        )
        TransactionType.EXPENSE -> TransactionRowUi(
            id = id, title = title, subtitle = subtitleParts.joinToString(" · "),
            amountText = "-" + formatRupiah(amountIdr), color = Expense, icon = Icons.Default.ArrowUpward, typeLabel = "Pengeluaran",
        )
        TransactionType.TRANSFER -> TransactionRowUi(
            id = id, title = title, subtitle = subtitleParts.joinToString(" · "),
            amountText = formatRupiah(amountIdr), color = Muted, icon = Icons.Default.SwapHoriz, typeLabel = "Transfer",
        )
    }
}

@Composable
fun TransactionRow(row: TransactionRowUi, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(row.color.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(row.icon, contentDescription = row.typeLabel, tint = row.color)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(row.title, color = Ink, fontWeight = FontWeight.Medium, maxLines = 1)
                Text(row.subtitle, color = Muted, fontSize = 14.sp, maxLines = 1)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(row.amountText, color = row.color, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                Text(row.typeLabel, color = Muted, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
