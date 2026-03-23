package com.easyaccounting.data.repository

import com.easyaccounting.data.dao.PendingRecordDao
import com.easyaccounting.data.entity.PendingRecord
import com.easyaccounting.data.entity.PendingStatus
import kotlinx.coroutines.flow.Flow

class PendingRecordRepository(private val pendingRecordDao: PendingRecordDao) {

    fun getAllPendingRecords(): Flow<List<PendingRecord>> = pendingRecordDao.getAllPendingRecords()

    fun getPendingRecordsByStatus(status: PendingStatus = PendingStatus.PENDING): Flow<List<PendingRecord>> =
        pendingRecordDao.getPendingRecordsByStatus(status)

    fun getPendingCount(status: PendingStatus = PendingStatus.PENDING): Flow<Int> =
        pendingRecordDao.getPendingCount(status)

    suspend fun getPendingRecordById(id: Long): PendingRecord? =
        pendingRecordDao.getPendingRecordById(id)

    suspend fun getLatestPendingRecord(): PendingRecord? =
        pendingRecordDao.getLatestPendingRecord()

    suspend fun insert(pendingRecord: PendingRecord): Long =
        pendingRecordDao.insert(pendingRecord)

    suspend fun update(pendingRecord: PendingRecord) =
        pendingRecordDao.update(pendingRecord)

    suspend fun delete(pendingRecord: PendingRecord) =
        pendingRecordDao.delete(pendingRecord)

    suspend fun updateStatus(id: Long, status: PendingStatus) =
        pendingRecordDao.updateStatus(id, status)

    suspend fun deleteById(id: Long) =
        pendingRecordDao.deleteById(id)

    suspend fun deletePendingRecords() =
        pendingRecordDao.deleteByStatus(PendingStatus.PENDING)
}
