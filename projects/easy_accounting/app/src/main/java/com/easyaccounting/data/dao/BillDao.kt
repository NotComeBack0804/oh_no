package com.easyaccounting.data.dao

import androidx.room.*
import com.easyaccounting.data.entity.Bill
import com.easyaccounting.data.entity.BillWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bill: Bill): Long

    @Update
    suspend fun update(bill: Bill)

    @Delete
    suspend fun delete(bill: Bill)

    @Query("SELECT * FROM bills ORDER BY date DESC, createdAt DESC")
    fun getAllBills(): Flow<List<Bill>>

    @Query("SELECT * FROM bills WHERE id = :id")
    suspend fun getBillById(id: Long): Bill?

    @Query("SELECT * FROM bills WHERE id = :id")
    fun getBillByIdFlow(id: Long): Flow<Bill?>

    @Query("SELECT * FROM bills WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getBillsByDateRange(startDate: Long, endDate: Long): Flow<List<Bill>>

    @Query("SELECT * FROM bills WHERE categoryId = :categoryId ORDER BY date DESC")
    fun getBillsByCategory(categoryId: Long): Flow<List<Bill>>

    @Query("SELECT * FROM bills WHERE remark LIKE '%' || :keyword || '%' ORDER BY date DESC")
    fun searchBills(keyword: String): Flow<List<Bill>>

    @Query("SELECT * FROM bills WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    suspend fun getBillsByDateRangeSync(startDate: Long, endDate: Long): List<Bill>

    @Query("SELECT SUM(amount) FROM bills WHERE date >= :startDate AND date <= :endDate")
    fun getTotalExpenseByDateRange(startDate: Long, endDate: Long): Flow<Double?>

    @Query("SELECT * FROM bills WHERE date >= :startDate AND date <= :endDate ORDER BY amount DESC LIMIT :limit")
    fun getTopBillsByAmount(startDate: Long, endDate: Long, limit: Int): Flow<List<Bill>>

    // JOIN query: get bills with category names for display
    @Query("""
        SELECT b.id, b.amount, b.categoryId, c.name AS categoryName,
               b.date, b.remark, b.accountId, b.createdAt
        FROM bills b
        LEFT JOIN categories c ON b.categoryId = c.id
        WHERE b.date >= :startDate AND b.date <= :endDate
        ORDER BY b.date DESC
    """)
    fun getBillsWithCategoryByDateRange(startDate: Long, endDate: Long): Flow<List<BillWithCategory>>

    // JOIN query: search bills with category names
    @Query("""
        SELECT b.id, b.amount, b.categoryId, c.name AS categoryName,
               b.date, b.remark, b.accountId, b.createdAt
        FROM bills b
        LEFT JOIN categories c ON b.categoryId = c.id
        WHERE b.remark LIKE '%' || :keyword || '%'
        ORDER BY b.date DESC
    """)
    fun searchBillsWithCategory(keyword: String): Flow<List<BillWithCategory>>

    // JOIN query: all bills with category names
    @Query("""
        SELECT b.id, b.amount, b.categoryId, c.name AS categoryName,
               b.date, b.remark, b.accountId, b.createdAt
        FROM bills b
        LEFT JOIN categories c ON b.categoryId = c.id
        ORDER BY b.date DESC
    """)
    fun getAllBillsWithCategory(): Flow<List<BillWithCategory>>
}
