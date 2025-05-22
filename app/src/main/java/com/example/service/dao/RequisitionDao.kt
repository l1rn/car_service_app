package com.example.service.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.service.enums.Status
import com.example.service.models.Requisition

@Dao
interface RequisitionDao {
    @Insert
    suspend fun insert(request: Requisition)

    @Insert
    suspend fun insertAll(requests: List<Requisition>)

    @Query("SELECT COUNT(*) FROM requests")
    suspend fun count(): Int

    @Query("SELECT * FROM requests WHERE userId = :id")
    suspend fun getAllRequestsById(id: Int): List<Requisition>

    @Query("SELECT * FROM requests")
    suspend fun getAllRequests(): List<Requisition>

    @Query("UPDATE requests SET status = :status WHERE requestId = :id")
    suspend fun setStatusById(status: Status, id: Int)
}