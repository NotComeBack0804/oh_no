package com.easyaccounting.data.repository

import com.easyaccounting.data.dao.AccountDao
import com.easyaccounting.data.entity.Account
import kotlinx.coroutines.flow.Flow

class AccountRepository(private val accountDao: AccountDao) {

    fun getAllAccounts(): Flow<List<Account>> = accountDao.getAllAccounts()

    suspend fun getAllAccountsSync(): List<Account> = accountDao.getAllAccountsSync()

    suspend fun getAccountById(id: Long): Account? = accountDao.getAccountById(id)

    suspend fun insertAccount(account: Account): Long = accountDao.insert(account)

    suspend fun updateAccount(account: Account) = accountDao.update(account)

    suspend fun deleteAccount(account: Account) = accountDao.delete(account)

    suspend fun updateBalance(accountId: Long, amount: Double) =
        accountDao.updateBalance(accountId, amount)

    suspend fun getAccountCount(): Int = accountDao.getAccountCount()
}
