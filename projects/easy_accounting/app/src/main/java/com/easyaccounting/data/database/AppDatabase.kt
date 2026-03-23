package com.easyaccounting.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.easyaccounting.data.dao.AccountDao
import com.easyaccounting.data.dao.BillDao
import com.easyaccounting.data.dao.CategoryDao
import com.easyaccounting.data.dao.IncomeDao
import com.easyaccounting.data.entity.Account
import com.easyaccounting.data.entity.AccountType
import com.easyaccounting.data.entity.Bill
import com.easyaccounting.data.entity.Category
import com.easyaccounting.data.entity.CategoryType
import com.easyaccounting.data.entity.Income
import com.easyaccounting.util.SecurityUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [Bill::class, Income::class, Category::class, Account::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun billDao(): BillDao
    abstract fun incomeDao(): IncomeDao
    abstract fun categoryDao(): CategoryDao
    abstract fun accountDao(): AccountDao

    companion object {
        private const val DATABASE_NAME = "easy_accounting.db"
        // 数据库加密密钥通过 Android Keystore 安全存储，不再硬编码
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            // 从 Keystore 安全获取数据库加密密钥
            val passphrase = SecurityUtils.getOrCreateDatabaseKey(context)
            val factory = SupportFactory(passphrase)

            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .openHelperFactory(factory) // 启用数据库加密
                .addCallback(DatabaseCallback())
                .fallbackToDestructiveMigration()
                .build()
        }

        /**
         * 数据库创建时的回调，用于初始化默认数据
         */
        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDefaultData(database)
                    }
                }
            }

            private suspend fun populateDefaultData(database: AppDatabase) {
                // 初始化默认支出分类
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

                // 初始化默认收入分类
                val incomeCategories = listOf(
                    Category(name = "工资", icon = "ic_salary", type = CategoryType.INCOME),
                    Category(name = "奖金", icon = "ic_bonus", type = CategoryType.INCOME),
                    Category(name = "投资收益", icon = "ic_investment", type = CategoryType.INCOME),
                    Category(name = "其他", icon = "ic_other", type = CategoryType.INCOME)
                )
                database.categoryDao().insertAll(incomeCategories)

                // 初始化默认账户
                val defaultAccounts = listOf(
                    Account(name = "支付宝", type = AccountType.ALIPAY),
                    Account(name = "微信", type = AccountType.WECHAT),
                    Account(name = "银行卡", type = AccountType.BANK_CARD),
                    Account(name = "现金", type = AccountType.CASH)
                )
                database.accountDao().insertAll(defaultAccounts)
            }
        }
    }
}
