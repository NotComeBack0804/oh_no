package com.easyaccounting.data.repository

import com.easyaccounting.data.dao.BillDao
import com.easyaccounting.data.dao.CategoryDao
import com.easyaccounting.data.entity.Bill
import com.easyaccounting.data.entity.BillWithCategory
import com.easyaccounting.data.entity.Category
import kotlinx.coroutines.flow.Flow

class BillRepository(
    private val billDao: BillDao,
    private val categoryDao: CategoryDao
) {
    fun getAllBills(): Flow<List<Bill>> = billDao.getAllBills()

    fun getAllBillsWithCategory(): Flow<List<BillWithCategory>> = billDao.getAllBillsWithCategory()

    fun getBillsByDateRange(startDate: Long, endDate: Long): Flow<List<Bill>> =
        billDao.getBillsByDateRange(startDate, endDate)

    fun getBillsWithCategoryByDateRange(startDate: Long, endDate: Long): Flow<List<BillWithCategory>> =
        billDao.getBillsWithCategoryByDateRange(startDate, endDate)

    fun getBillsByCategory(categoryId: Long): Flow<List<Bill>> =
        billDao.getBillsByCategory(categoryId)

    fun searchBills(keyword: String): Flow<List<Bill>> = billDao.searchBills(keyword)

    fun searchBillsWithCategory(keyword: String): Flow<List<BillWithCategory>> =
        billDao.searchBillsWithCategory(keyword)

    suspend fun getBillById(id: Long): Bill? = billDao.getBillById(id)

    fun getBillByIdFlow(id: Long): Flow<Bill?> = billDao.getBillByIdFlow(id)

    suspend fun insertBill(bill: Bill): Long = billDao.insert(bill)

    suspend fun updateBill(bill: Bill) = billDao.update(bill)

    suspend fun deleteBill(bill: Bill) = billDao.delete(bill)

    fun getTotalExpenseByDateRange(startDate: Long, endDate: Long): Flow<Double?> =
        billDao.getTotalExpenseByDateRange(startDate, endDate)

    fun getTopBillsByAmount(startDate: Long, endDate: Long, limit: Int): Flow<List<Bill>> =
        billDao.getTopBillsByAmount(startDate, endDate, limit)

    suspend fun getBillsByDateRangeSync(startDate: Long, endDate: Long): List<Bill> =
        billDao.getBillsByDateRangeSync(startDate, endDate)
}
