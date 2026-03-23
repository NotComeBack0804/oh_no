package com.easyaccounting.data.dao

import androidx.room.*
import com.easyaccounting.data.entity.PendingRecord
import com.easyaccounting.data.entity.PendingStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pendingRecord: PendingRecord): Long

    @Update
    suspend fun update(pendingRecord: PendingRecord)

    @Delete
    suspend fun delete(pendingRecord: PendingRecord)

    @Query("SELECT * FROM pending_records WHERE status = :status ORDER BY createdAt DESC")
    fun getPendingRecordsByStatus(status: PendingStatus = PendingStatus.PENDING): Flow<List<PendingRecord>>

    @Query("SELECT * FROM pending_records ORDER BY createdAt DESC")
    fun getAllPendingRecords(): Flow<List<PendingRecord>>

    @Query("SELECT * FROM pending_records WHERE id = :id")
    suspend fun getPendingRecordById(id: Long): PendingRecord?

    @Query("SELECT * FROM pending_records WHERE status = :status ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestPendingRecord(status: PendingStatus = PendingStatus.PENDING): PendingRecord?

    @Query("UPDATE pending_records SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: PendingStatus)

    @Query("DELETE FROM pending_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM pending_records WHERE status = :status")
    suspend fun deleteByStatus(status: PendingStatus = PendingStatus.PENDING)

    @Query("SELECT COUNT(*) FROM pending_records WHERE status = :status")
    fun getPendingCount(status: PendingStatus = PendingStatus.PENDING): Flow<Int>
}
