package com.example.service.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.service.enums.Role

@Entity(tableName = "users")
data class User(
    @PrimaryKey val userId: Int = 1,
    val name: String,
    val phone: String,
    val role: Role,
)
