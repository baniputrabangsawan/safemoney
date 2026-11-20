package com.jagauang.app.ui.transaction

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jagauang.app.AppContainer
import com.jagauang.app.domain.Transaction
import com.jagauang.app.domain.TransactionType
import com.jagauang.app.domain.Recurring
import com.jagauang.app.ui.categoryName
import com.jagauang.app.ui.components.ScreenTopBar
import com.jagauang.app.ui.formatDay
import com.jagauang.app.ui.formatRupiah
import com.jagauang.app.ui.formatTime
import com.jagauang.app.ui.paymentMethodName
import com.jagauang.app.ui.theme.Canvas
import com.jagauang.app.ui.theme.Expense
import com.jagauang.app.ui.theme.Income
import com.jagauang.app.ui.theme.Ink
import com.jagauang.app.ui.theme.Muted

@Composable
fun TransactionDetailScreen(container: AppContainer, id: String, onEdit: (Transaction) -> Unit, onBack: () -> Unit) {
    val vm: TransactionDetailViewModel = viewModel(
        factory = remember(id) { viewModelFactory { initializer { TransactionDetailViewModel(container, id) } } }
    )
    val transaction by vm.transaction.collectAsStateWithLifecycle()
    val categories by container.repository.observeCategories().collectAsStateWithLifecycle(emptyList())
    val methods by container.repository.observePaymentMethods().collectAsStateWithLifecycle(emptyList())
    val deleted = vm.deleted
    LaunchedEffect(deleted) { if (deleted) onBack() }

    var showConfirm by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(Canvas)) {
        ScreenTopBar("Detail transaksi", onBack)
        val t = transaction
        if (t == null) {
            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text("Transaksi sudah tidak tersedia.", color = Muted)
            }
        } else {
            Column(Modifier.weight(1f).padding(20.dp)) {
                Text(
                    text = when (t.type) {
                        TransactionType.INCOME -> "Pemasukan"
                        TransactionType.EXPENSE -> "Pengeluaran"
                        TransactionType.TRANSFER -> "Transfer"
                    },
                    color = Muted, fontSize = 15.sp,
                )
                Text(
                    text = when (t.type) {
                        TransactionType.INCOME -> "+" + formatRupiah(t.amountIdr)
                        TransactionType.EXPENSE -> "-" + formatRupiah(t.amountIdr)
                        TransactionType.TRANSFER -> formatRupiah(t.amountIdr)
                    },
                    color = when (t.type) {
                        TransactionType.INCOME -> Income
                        TransactionType.EXPENSE -> Expense
                        TransactionType.TRANSFER -> Muted
                    },
                    fontSize = 32.sp, fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(20.dp))
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        if (t.type == TransactionType.TRANSFER) {
                            DetailRow("Dari", paymentMethodName(methods, t.transferSourceId))
                            HorizontalDivider(Modifier.padding(vertical = 12.dp))
                            DetailRow("Ke", paymentMethodName(methods, t.transferDestinationId))
                        } else {
                            DetailRow("Kategori", categoryName(categories, t.categoryCode))
                            HorizontalDivider(Modifier.padding(vertical = 12.dp))
                            DetailRow("Metode", paymentMethodName(methods, t.paymentMethodId))
                        }
                        HorizontalDivider(Modifier.padding(vertical = 12.dp))
                        DetailRow("Tanggal", formatDay(t.day))
                        HorizontalDivider(Modifier.padding(vertical = 12.dp))
                        DetailRow("Jam", formatTime(t.timeMinutes))
                        if (!t.note.isNullOrBlank()) {
                            HorizontalDivider(Modifier.padding(vertical = 12.dp))
                            DetailRow("Catatan", t.note!!)
                        }
                        if (Recurring.fromCode(t.recurringCode) != Recurring.NONE) {
                            HorizontalDivider(Modifier.padding(vertical = 12.dp))
                            DetailRow("Pengingat", Recurring.fromCode(t.recurringCode).label)
                        }
                        t.attachmentPath?.let { path ->
                            val bitmap = remember(path) { runCatching { BitmapFactory.decodeFile(path) }.getOrNull() }
                            if (bitmap != null) {
                                HorizontalDivider(Modifier.padding(vertical = 12.dp))
                                Image(bitmap.asImageBitmap(), "Struk", modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)))
                            }
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                Button(onClick = { onEdit(t) }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(14.dp)) {
                    Text("Ubah transaksi", fontSize = 16.sp)
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { showConfirm = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Expense),
                ) { Text("Hapus transaksi", fontSize = 16.sp) }
                if (vm.deleteError) {
                    Spacer(Modifier.height(12.dp))
                    Text("Transaksi belum bisa dihapus.", color = Expense)
                }
            }
        }
    }

    val t = transaction
    if (showConfirm && t != null) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Hapus transaksi ini?") },
            text = { Text("${formatDay(t.day)} · ${formatRupiah(t.amountIdr)}") },
            confirmButton = { TextButton(onClick = { showConfirm = false; vm.delete() }) { Text("Hapus", color = Expense) } },
            dismissButton = { TextButton(onClick = { showConfirm = false }) { Text("Batal") } },
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, color = Muted, modifier = Modifier.weight(0.35f))
        Text(value, color = Ink, fontWeight = FontWeight.Medium, modifier = Modifier.weight(0.65f))
    }
}
