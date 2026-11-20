package com.jagauang.app

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.jagauang.app.data.local.JagaUangDatabase
import com.jagauang.app.data.repository.TransactionRepository
import com.jagauang.app.data.repository.TransactionRepositoryImpl
import com.jagauang.app.domain.Book
import com.jagauang.app.domain.Category
import com.jagauang.app.domain.LedgerSummary
import com.jagauang.app.domain.PaymentMethod
import com.jagauang.app.domain.Period
import com.jagauang.app.domain.PeriodType
import com.jagauang.app.domain.Transaction
import com.jagauang.app.domain.summaryOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

/** Manual dependency container, created once at Application scope. */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AppContainer(context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val database = JagaUangDatabase.getInstance(context)

    val repository: TransactionRepository = TransactionRepositoryImpl(
        transactionDao = database.transactionDao(),
        bookDao = database.bookDao(),
        paymentMethodDao = database.paymentMethodDao(),
        categoryDao = database.categoryDao(),
    )

    /** Active notebook (buku catatan); shared across Beranda/Riwayat/Laporan. */
    val activeBookId = MutableStateFlow("book-rumah")

    /** Active period filter; shared across Beranda/Riwayat/Laporan. */
    val period = MutableStateFlow(Period(PeriodType.MONTHLY, LocalDate.now()))

    /** One-shot success message surfaced via snackbar. */
    val successMessage = MutableStateFlow<String?>(null)

    // App-scoped hot caches so tab switches never re-run Room queries or summary math.
    val books: StateFlow<List<Book>> = repository.observeBooks().stateIn(scope, SharingStarted.Eagerly, emptyList())
    val categories: StateFlow<List<Category>> = repository.observeCategories().stateIn(scope, SharingStarted.Eagerly, emptyList())
    val methods: StateFlow<List<PaymentMethod>> = repository.observePaymentMethods().stateIn(scope, SharingStarted.Eagerly, emptyList())
    val transactions: StateFlow<List<Transaction>> =
        activeBookId.flatMapLatest { repository.observeTransactions(it) }
            .stateIn(scope, SharingStarted.Eagerly, emptyList())

    /** Precomputed totals + per-category breakdown, computed off the main thread. */
    val summary: StateFlow<LedgerSummary> =
        combine(transactions, period) { list, p ->
            val (start, end) = p.range()
            summaryOf(list, start, end)
        }.stateIn(scope, SharingStarted.Eagerly, LedgerSummary())
}

@Composable
fun rememberAppContainer(): AppContainer {
    val context = LocalContext.current
    return remember { (context.applicationContext as JagaUangApplication).container }
}
