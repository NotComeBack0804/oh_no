package com.easyaccounting.data.repository

import com.easyaccounting.data.dao.IncomeDao
import com.easyaccounting.data.entity.Income
import kotlinx.coroutines.flow.Flow

class IncomeRepository(private val incomeDao: IncomeDao) {

    fun getAllIncomes(): Flow<List<Income>> = incomeDao.getAllIncomes()

    fun getIncomesByDateRange(startDate: Long, endDate: Long): Flow<List<Income>> =
        incomeDao.getIncomesByDateRange(startDate, endDate)

    fun searchIncomes(keyword: String): Flow<List<Income>> = incomeDao.searchIncomes(keyword)

    suspend fun getIncomeById(id: Long): Income? = incomeDao.getIncomeById(id)

    fun getIncomeByIdFlow(id: Long): Flow<Income?> = incomeDao.getIncomeByIdFlow(id)

    suspend fun insertIncome(income: Income): Long = incomeDao.insert(income)

    suspend fun updateIncome(income: Income) = incomeDao.update(income)

    suspend fun deleteIncome(income: Income) = incomeDao.delete(income)

    fun getTotalIncomeByDateRange(startDate: Long, endDate: Long): Flow<Double?> =
        incomeDao.getTotalIncomeByDateRange(startDate, endDate)

    fun getTopIncomesByAmount(startDate: Long, endDate: Long, limit: Int): Flow<List<Income>> =
        incomeDao.getTopIncomesByAmount(startDate, endDate, limit)

    suspend fun getIncomesByDateRangeSync(startDate: Long, endDate: Long): List<Income> =
        incomeDao.getIncomesByDateRangeSync(startDate, endDate)
}
