package com.easyaccounting

import android.app.Application
import com.easyaccounting.data.database.AppDatabase
import com.easyaccounting.data.repository.AccountRepository
import com.easyaccounting.data.repository.BillRepository
import com.easyaccounting.data.repository.CategoryRepository
import com.easyaccounting.data.repository.IncomeRepository
import com.easyaccounting.data.repository.PendingRecordRepository

class EasyAccountingApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    val billRepository: BillRepository by lazy {
        BillRepository(database.billDao(), database.categoryDao())
    }

    val incomeRepository: IncomeRepository by lazy {
        IncomeRepository(database.incomeDao())
    }

    val categoryRepository: CategoryRepository by lazy {
        CategoryRepository(database.categoryDao())
    }

    val accountRepository: AccountRepository by lazy {
        AccountRepository(database.accountDao())
    }

    val pendingRecordRepository: PendingRecordRepository by lazy {
        PendingRecordRepository(database.pendingRecordDao())
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: EasyAccountingApp
            private set
    }
}
