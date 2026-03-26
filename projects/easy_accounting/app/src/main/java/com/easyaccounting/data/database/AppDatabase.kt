package com.easyaccounting.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.easyaccounting.data.dao.AccountDao
import com.easyaccounting.data.dao.AiChatMessageDao
import com.easyaccounting.data.dao.BillDao
import com.easyaccounting.data.dao.CategoryDao
import com.easyaccounting.data.dao.IncomeDao
import com.easyaccounting.data.dao.PendingRecordDao
import com.easyaccounting.data.entity.Account
import com.easyaccounting.data.entity.AccountType
import com.easyaccounting.data.entity.AiChatMessage
import com.easyaccounting.data.entity.Bill
import com.easyaccounting.data.entity.Category
import com.easyaccounting.data.entity.CategoryType
import com.easyaccounting.data.entity.Income
import com.easyaccounting.data.entity.PendingRecord
import com.easyaccounting.util.SecurityUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [
        Bill::class,
        Income::class,
        Category::class,
        Account::class,
        PendingRecord::class,
        AiChatMessage::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun billDao(): BillDao
    abstract fun incomeDao(): IncomeDao
    abstract fun categoryDao(): CategoryDao
    abstract fun accountDao(): AccountDao
    abstract fun pendingRecordDao(): PendingRecordDao
    abstract fun aiChatMessageDao(): AiChatMessageDao

    companion object {
        private const val DATABASE_NAME = "easy_accounting.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `ai_chat_messages` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `conversationId` TEXT NOT NULL,
                        `role` TEXT NOT NULL,
                        `content` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `intentType` TEXT NOT NULL,
                        `ledgerAmount` REAL,
                        `ledgerCategory` TEXT,
                        `ledgerSummary` TEXT,
                        `linkedBillId` INTEGER,
                        `linkedIncomeId` INTEGER,
                        `needsClarification` INTEGER NOT NULL,
                        `sourceLabel` TEXT
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_ai_chat_messages_conversationId` ON `ai_chat_messages` (`conversationId`)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_ai_chat_messages_linkedBillId` ON `ai_chat_messages` (`linkedBillId`)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_ai_chat_messages_linkedIncomeId` ON `ai_chat_messages` (`linkedIncomeId`)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_ai_chat_messages_createdAt` ON `ai_chat_messages` (`createdAt`)"
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            val passphrase = SecurityUtils.getOrCreateDatabaseKey(context)
            val factory = SupportFactory(passphrase)

            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .openHelperFactory(factory)
                .addMigrations(MIGRATION_2_3)
                .addCallback(DatabaseCallback(context))
                .fallbackToDestructiveMigration()
                .build()
        }

        private class DatabaseCallback(
            private val context: Context
        ) : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    populateDefaultData(getInstance(context))
                }
            }

            private suspend fun populateDefaultData(database: AppDatabase) {
                val expenseCategories = listOf(
                    Category(name = "餐饮", icon = "ic_food", type = CategoryType.EXPENSE),
                    Category(name = "交通", icon = "ic_transport", type = CategoryType.EXPENSE),
                    Category(name = "购物", icon = "ic_shopping", type = CategoryType.EXPENSE),
                    Category(name = "娱乐", icon = "ic_entertainment", type = CategoryType.EXPENSE),
                    Category(name = "医疗", icon = "ic_medical", type = CategoryType.EXPENSE),
                    Category(name = "教育", icon = "ic_education", type = CategoryType.EXPENSE),
                    Category(name = "其他", icon = "ic_other", type = CategoryType.EXPENSE)
                )
                database.categoryDao().insertAll(expenseCategories)

                val incomeCategories = listOf(
                    Category(name = "工资", icon = "ic_salary", type = CategoryType.INCOME),
                    Category(name = "奖金", icon = "ic_bonus", type = CategoryType.INCOME),
                    Category(name = "投资收益", icon = "ic_investment", type = CategoryType.INCOME),
                    Category(name = "其他", icon = "ic_other", type = CategoryType.INCOME)
                )
                database.categoryDao().insertAll(incomeCategories)

                val accounts = listOf(
                    Account(name = "支付宝", type = AccountType.ALIPAY),
                    Account(name = "微信", type = AccountType.WECHAT),
                    Account(name = "银行卡", type = AccountType.BANK_CARD),
                    Account(name = "现金", type = AccountType.CASH)
                )
                database.accountDao().insertAll(accounts)
            }
        }
    }
}
