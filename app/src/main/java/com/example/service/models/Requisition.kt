package com.example.service.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.service.enums.Status

@Entity(tableName = "requests")
data class Requisition(
    @PrimaryKey(autoGenerate = true) val requestId: Int = 0,
    val date: String,
    val userId: Int,
    val reason: String,
    val status: Status = Status.OPEN,
)
