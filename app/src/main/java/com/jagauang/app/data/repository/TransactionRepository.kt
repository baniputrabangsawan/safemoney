package com.jagauang.app.data.repository

import com.jagauang.app.data.local.BookDao
import com.jagauang.app.data.local.BookEntity
import com.jagauang.app.data.local.CategoryDao
import com.jagauang.app.data.local.CategoryEntity
import com.jagauang.app.data.local.PaymentMethodDao
import com.jagauang.app.data.local.PaymentMethodEntity
import com.jagauang.app.data.local.TransactionDao
import com.jagauang.app.domain.AmountParser
import com.jagauang.app.domain.Book
import com.jagauang.app.domain.Category
import com.jagauang.app.domain.PaymentMethod
import com.jagauang.app.domain.Transaction
import com.jagauang.app.domain.TransactionType
import com.jagauang.app.domain.TransactionValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID

sealed interface WriteResult {
    data object Success : WriteResult
    data object NotFound : WriteResult
    data class Invalid(val message: String) : WriteResult
}

interface TransactionRepository {
    fun observeBooks(): Flow<List<Book>>
    fun observePaymentMethods(): Flow<List<PaymentMethod>>
    fun observeCategories(): Flow<List<Category>>

    suspend fun addBook(name: String): WriteResult
    suspend fun renameBook(id: String, name: String): WriteResult
    suspend fun addPaymentMethod(name: String): WriteResult
    suspend fun renamePaymentMethod(id: String, name: String): WriteResult
    suspend fun addCategory(type: TransactionType, name: String): WriteResult
    suspend fun renameCategory(code: String, name: String): WriteResult

    fun observeTransactions(bookId: String): Flow<List<Transaction>>
    fun observeById(id: String): Flow<Transaction?>
    suspend fun getById(id: String): Transaction?
    suspend fun add(transaction: Transaction): WriteResult
    suspend fun update(transaction: Transaction): WriteResult
    suspend fun delete(id: String): WriteResult
}

class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val bookDao: BookDao,
    private val paymentMethodDao: PaymentMethodDao,
    private val categoryDao: CategoryDao,
) : TransactionRepository {

    override fun observeBooks() = bookDao.observeAll().map { list -> list.map { it.toDomain() } }
    override fun observePaymentMethods() = paymentMethodDao.observeAll().map { list -> list.map { it.toDomain() } }
    override fun observeCategories() = categoryDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun addBook(name: String): WriteResult {
        val n = TransactionValidator.normalizeName(name) ?: return WriteResult.Invalid("Nama buku tidak boleh kosong.")
        bookDao.insert(BookEntity("book-" + UUID.randomUUID().toString(), n, System.currentTimeMillis()))
        return WriteResult.Success
    }

    override suspend fun renameBook(id: String, name: String): WriteResult {
        val n = TransactionValidator.normalizeName(name) ?: return WriteResult.Invalid("Nama buku tidak boleh kosong.")
        bookDao.rename(id, n)
        return WriteResult.Success
    }

    override suspend fun addPaymentMethod(name: String): WriteResult {
        val n = TransactionValidator.normalizeName(name) ?: return WriteResult.Invalid("Nama metode tidak boleh kosong.")
        paymentMethodDao.insert(PaymentMethodEntity("pm-" + UUID.randomUUID().toString(), n, System.currentTimeMillis()))
        return WriteResult.Success
    }

    override suspend fun renamePaymentMethod(id: String, name: String): WriteResult {
        val n = TransactionValidator.normalizeName(name) ?: return WriteResult.Invalid("Nama metode tidak boleh kosong.")
        paymentMethodDao.rename(id, n)
        return WriteResult.Success
    }

    override suspend fun addCategory(type: TransactionType, name: String): WriteResult {
        val n = TransactionValidator.normalizeName(name) ?: return WriteResult.Invalid("Nama kategori tidak boleh kosong.")
        categoryDao.insert(CategoryEntity("cat-" + UUID.randomUUID().toString(), type.code, n, System.currentTimeMillis()))
        return WriteResult.Success
    }

    override suspend fun renameCategory(code: String, name: String): WriteResult {
        val n = TransactionValidator.normalizeName(name) ?: return WriteResult.Invalid("Nama kategori tidak boleh kosong.")
        categoryDao.rename(code, n)
        return WriteResult.Success
    }

    override fun observeTransactions(bookId: String): Flow<List<Transaction>> =
        transactionDao.observeByBook(bookId).map { list -> list.map { it.toDomain() } }

    override fun observeById(id: String): Flow<Transaction?> =
        transactionDao.observeById(id).map { it?.toDomain() }

    override suspend fun getById(id: String): Transaction? = transactionDao.getById(id)?.toDomain()

    override suspend fun add(transaction: Transaction): WriteResult {
        validate(transaction)?.let { return it }
        transactionDao.upsert(transaction.toEntity())
        return WriteResult.Success
    }

    override suspend fun update(transaction: Transaction): WriteResult {
        validate(transaction)?.let { return it }
        if (transactionDao.getById(transaction.id) == null) return WriteResult.NotFound
        transactionDao.upsert(transaction.toEntity())
        return WriteResult.Success
    }

    override suspend fun delete(id: String): WriteResult =
        if (transactionDao.deleteById(id) > 0) WriteResult.Success else WriteResult.NotFound

    private fun validate(t: Transaction): WriteResult.Invalid? {
        if (t.amountIdr !in AmountParser.MIN_AMOUNT..AmountParser.MAX_AMOUNT) return WriteResult.Invalid("nominal")
        if (!TransactionValidator.isDateInRange(t.day, LocalDate.now())) return WriteResult.Invalid("tanggal")
        if (!TransactionValidator.isTimeValid(t.timeMinutes)) return WriteResult.Invalid("jam")
        if (t.note != null && t.note.length > TransactionValidator.MAX_NOTE_LENGTH) return WriteResult.Invalid("catatan")
        if (t.type == TransactionType.TRANSFER && !TransactionValidator.isTransferValid(t.transferSourceId, t.transferDestinationId)) {
            return WriteResult.Invalid("transfer")
        }
        return null
    }
}
