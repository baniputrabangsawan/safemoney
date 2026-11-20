package com.jagauang.app.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        val now = System.currentTimeMillis()
        db.execSQL("CREATE TABLE IF NOT EXISTS books (id TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL, created_at_ms INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS payment_methods (id TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL, created_at_ms INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS categories (code TEXT NOT NULL PRIMARY KEY, type TEXT NOT NULL, name TEXT NOT NULL, created_at_ms INTEGER NOT NULL)")

        db.execSQL("INSERT INTO books (id, name, created_at_ms) VALUES ('book-rumah','Rumah',$now),('book-pribadi','Pribadi',$now),('book-usaha','Usaha',$now)")
        db.execSQL("INSERT INTO payment_methods (id, name, created_at_ms) VALUES ('pm-cash','Tunai',$now),('pm-bank','Bank',$now),('pm-ewallet','Dompet Digital',$now)")
        db.execSQL(
            "INSERT INTO categories (code, type, name, created_at_ms) VALUES " +
                "('SALARY','INCOME','Gaji',$now),('BUSINESS','INCOME','Usaha',$now),('OTHER_INCOME','INCOME','Lainnya',$now)," +
                "('FOOD','EXPENSE','Makan',$now),('TRANSPORT','EXPENSE','Transportasi',$now),('BILLS','EXPENSE','Tagihan',$now)," +
                "('SHOPPING','EXPENSE','Belanja',$now),('OTHER_EXPENSE','EXPENSE','Lainnya',$now)"
        )

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS transactions_new (" +
                "id TEXT NOT NULL PRIMARY KEY, book_id TEXT NOT NULL, type TEXT NOT NULL, amount_idr INTEGER NOT NULL, " +
                "transaction_day INTEGER NOT NULL, time_minutes INTEGER NOT NULL, category_code TEXT, payment_method_id TEXT, " +
                "transfer_source_id TEXT, transfer_destination_id TEXT, note TEXT, attachment_path TEXT, recurring_code TEXT, " +
                "created_at_ms INTEGER NOT NULL, updated_at_ms INTEGER NOT NULL)"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_book_id_transaction_day_created_at_ms_id ON transactions_new (book_id, transaction_day, created_at_ms, id)")
        db.execSQL(
            "INSERT INTO transactions_new (id, book_id, type, amount_idr, transaction_day, time_minutes, category_code, " +
                "payment_method_id, transfer_source_id, transfer_destination_id, note, attachment_path, recurring_code, created_at_ms, updated_at_ms) " +
                "SELECT id, 'book-rumah', type, amount_idr, transaction_day, " +
                "(CAST(strftime('%H', created_at_ms/1000, 'unixepoch', 'localtime') AS INTEGER)*60 + CAST(strftime('%M', created_at_ms/1000, 'unixepoch', 'localtime') AS INTEGER)), " +
                "category_code, NULL, NULL, NULL, note, NULL, NULL, created_at_ms, updated_at_ms FROM transactions"
        )
        db.execSQL("DROP TABLE transactions")
        db.execSQL("ALTER TABLE transactions_new RENAME TO transactions")
    }
}
