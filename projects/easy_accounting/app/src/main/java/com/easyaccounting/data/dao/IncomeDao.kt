package com.easyaccounting.data.dao

import androidx.room.*
import com.easyaccounting.data.entity.Income
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(income: Income): Long

    @Update
    suspend fun update(income: Income)

    @Delete
    suspend fun delete(income: Income)

    @Query("SELECT * FROM incomes ORDER BY date DESC, createdAt DESC")
    fun getAllIncomes(): Flow<List<Income>>

    @Query("SELECT * FROM incomes WHERE id = :id")
    suspend fun getIncomeById(id: Long): Income?

    @Query("SELECT * FROM incomes WHERE id = :id")
    fun getIncomeByIdFlow(id: Long): Flow<Income?>

    @Query("SELECT * FROM incomes WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getIncomesByDateRange(startDate: Long, endDate: Long): Flow<List<Income>>

    @Query("SELECT * FROM incomes WHERE remark LIKE '%' || :keyword || '%' ORDER BY date DESC")
    fun searchIncomes(keyword: String): Flow<List<Income>>

    @Query("SELECT * FROM incomes WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    suspend fun getIncomesByDateRangeSync(startDate: Long, endDate: Long): List<Income>

    @Query("SELECT SUM(amount) FROM incomes WHERE date >= :startDate AND date <= :endDate")
    fun getTotalIncomeByDateRange(startDate: Long, endDate: Long): Flow<Double?>

    @Query("SELECT * FROM incomes WHERE date >= :startDate AND date <= :endDate ORDER BY amount DESC LIMIT :limit")
    fun getTopIncomesByAmount(startDate: Long, endDate: Long, limit: Int): Flow<List<Income>>
}
