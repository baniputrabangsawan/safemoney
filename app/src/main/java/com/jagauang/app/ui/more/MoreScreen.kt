package com.jagauang.app.ui.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jagauang.app.AppContainer
import com.jagauang.app.domain.Book
import com.jagauang.app.domain.Category
import com.jagauang.app.domain.PaymentMethod
import com.jagauang.app.domain.Recurring
import com.jagauang.app.domain.TransactionType
import com.jagauang.app.ui.theme.Blue
import com.jagauang.app.ui.theme.BlueTint
import com.jagauang.app.ui.theme.IndigoTint
import com.jagauang.app.ui.theme.Indigo
import com.jagauang.app.ui.theme.Income
import com.jagauang.app.ui.theme.IncomeTint
import com.jagauang.app.ui.theme.Violet
import com.jagauang.app.ui.theme.VioletTint
import com.jagauang.app.ui.components.ScreenTopBar
import com.jagauang.app.ui.theme.Canvas
import com.jagauang.app.ui.theme.Ink
import com.jagauang.app.ui.theme.Muted
import com.jagauang.app.ui.theme.Navy
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@Composable
fun MoreScreen(
    onOpenBooks: () -> Unit,
    onOpenMethods: () -> Unit,
    onOpenCategories: () -> Unit,
    onOpenReminders: () -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Lainnya", style = MaterialTheme.typography.headlineSmall, color = Navy)
        Spacer(Modifier.height(16.dp))
        MenuItem("Buku Catatan", "Tambah, ubah nama, dan pindah buku", Icons.Default.Book, Blue, BlueTint, onOpenBooks)
        MenuItem("Metode Pembayaran", "Kelola Tunai, Bank, Dompet Digital, dan lainnya", Icons.Default.AccountBalanceWallet, Indigo, IndigoTint, onOpenMethods)
        MenuItem("Kategori", "Kelola kategori pemasukan dan pengeluaran", Icons.Default.Category, Violet, VioletTint, onOpenCategories)
        MenuItem("Pengingat", "Lihat transaksi berulang", Icons.Default.Notifications, Income, IncomeTint, onOpenReminders)
    }
}

@Composable
private fun MenuItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    tint: Color,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(tint),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = accent)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Ink, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = Muted, fontSize = 13.sp)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Muted)
        }
    }
}

// --- Buku Catatan ---
@Composable
fun BooksScreen(container: AppContainer, onBack: () -> Unit) {
    val books by container.repository.observeBooks().collectAsStateWithLifecycle(emptyList())
    val activeId by container.activeBookId.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var adding by remember { mutableStateOf(false) }
    var renaming by remember { mutableStateOf<Book?>(null) }

    ManagementScaffold("Buku Catatan", onBack, onAdd = { adding = true }) {
        items(books, key = { it.id }) { b ->
            Card(Modifier.fillMaxWidth().clickable { container.activeBookId.value = b.id }, shape = RoundedCornerShape(14.dp)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(b.name, color = Ink, fontWeight = FontWeight.Medium)
                        if (b.id == activeId) Text("Aktif", color = com.jagauang.app.ui.theme.Income, fontSize = 13.sp)
                    }
                    IconButton(onClick = { renaming = b }) { Icon(Icons.Default.Edit, "Ubah nama") }
                }
            }
        }
    }

    if (adding) NameDialog(
        title = "Tambah buku",
        initial = "",
        onConfirm = { name -> scope.launch { container.repository.addBook(name) }; adding = false },
        onDismiss = { adding = false },
    )
    renaming?.let { b ->
        NameDialog(
            title = "Ubah nama buku",
            initial = b.name,
            onConfirm = { name -> scope.launch { container.repository.renameBook(b.id, name) }; renaming = null },
            onDismiss = { renaming = null },
        )
    }
}

// --- Metode Pembayaran ---
@Composable
fun PaymentMethodsScreen(container: AppContainer, onBack: () -> Unit) {
    val methods by container.repository.observePaymentMethods().collectAsStateWithLifecycle(emptyList())
    val scope = rememberCoroutineScope()
    var adding by remember { mutableStateOf(false) }
    var renaming by remember { mutableStateOf<PaymentMethod?>(null) }

    ManagementScaffold("Metode Pembayaran", onBack, onAdd = { adding = true }) {
        items(methods, key = { it.id }) { m ->
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(m.name, color = Ink, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    IconButton(onClick = { renaming = m }) { Icon(Icons.Default.Edit, "Ubah nama") }
                }
            }
        }
    }

    if (adding) NameDialog(
        title = "Tambah metode pembayaran",
        initial = "",
        onConfirm = { name -> scope.launch { container.repository.addPaymentMethod(name) }; adding = false },
        onDismiss = { adding = false },
    )
    renaming?.let { m ->
        NameDialog(
            title = "Ubah nama metode",
            initial = m.name,
            onConfirm = { name -> scope.launch { container.repository.renamePaymentMethod(m.id, name) }; renaming = null },
            onDismiss = { renaming = null },
        )
    }
}

// --- Kategori ---
@Composable
fun CategoriesScreen(container: AppContainer, onBack: () -> Unit) {
    val categories by container.repository.observeCategories().collectAsStateWithLifecycle(emptyList())
    val scope = rememberCoroutineScope()
    var addingType by remember { mutableStateOf<TransactionType?>(null) }
    var renaming by remember { mutableStateOf<Category?>(null) }

    ManagementScaffold("Kategori", onBack, onAdd = { addingType = TransactionType.INCOME }) {
        item { Text("Pemasukan", style = MaterialTheme.typography.titleMedium, color = Navy) }
        items(categories.filter { it.type == TransactionType.INCOME }, key = { it.code }) { c ->
            CategoryRow(c) { renaming = c }
        }
        item { Text("Pengeluaran", style = MaterialTheme.typography.titleMedium, color = Navy, modifier = Modifier.padding(top = 12.dp)) }
        items(categories.filter { it.type == TransactionType.EXPENSE }, key = { it.code }) { c ->
            CategoryRow(c) { renaming = c }
        }
    }

    addingType?.let { type ->
        val label = if (type == TransactionType.INCOME) "pemasukan" else "pengeluaran"
        NameDialog(
            title = "Tambah kategori $label",
            initial = "",
            onConfirm = { name -> scope.launch { container.repository.addCategory(type, name) }; addingType = null },
            onDismiss = { addingType = null },
        )
    }
    renaming?.let { c ->
        NameDialog(
            title = "Ubah nama kategori",
            initial = c.name,
            onConfirm = { name -> scope.launch { container.repository.renameCategory(c.code, name) }; renaming = null },
            onDismiss = { renaming = null },
        )
    }
}

@Composable
private fun CategoryRow(c: Category, onRename: () -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(c.name, color = Ink, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            IconButton(onClick = onRename) { Icon(Icons.Default.Edit, "Ubah nama") }
        }
    }
}

// --- Pengingat ---
@Composable
fun RemindersScreen(container: AppContainer, onBack: () -> Unit) {
    val transactions by container.transactions.collectAsStateWithLifecycle(emptyList())
    val scope = rememberCoroutineScope()
    val recurring = transactions.filter { Recurring.fromCode(it.recurringCode) != Recurring.NONE }

    Column(Modifier.fillMaxSize().background(Canvas)) {
        ScreenTopBar("Pengingat", onBack)
        if (recurring.isEmpty()) {
            androidx.compose.foundation.layout.Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text("Belum ada transaksi berulang.", color = Muted)
            }
        } else {
            LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(recurring, key = { it.id }) { t ->
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(t.note ?: "Transaksi", color = Ink, fontWeight = FontWeight.Medium)
                                Text("Berulang: ${Recurring.fromCode(t.recurringCode).label}", color = Muted, fontSize = 13.sp)
                            }
                            OutlinedButton(onClick = {
                                scope.launch {
                                    container.repository.update(t.copy(recurringCode = Recurring.NONE.code, updatedAtMs = System.currentTimeMillis()))
                                }
                            }) { Text("Hapus") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ManagementScaffold(
    title: String,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
) {
    Column(Modifier.fillMaxSize().background(Canvas)) {
        ScreenTopBar(title, onBack)
        LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
        Button(
            onClick = onAdd,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp).height(52.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("Tambah")
        }
    }
}

@Composable
private fun NameDialog(title: String, initial: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { OutlinedTextField(text, { text = it }, singleLine = true, label = { Text("Nama") }) },
        confirmButton = { TextButton(onClick = { onConfirm(text) }, enabled = text.isNotBlank()) { Text("Simpan") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } },
    )
}
