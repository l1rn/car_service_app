package com.example.service.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.service.enums.Role
import com.example.service.models.User

@Dao
interface UserDao {
    @Insert
    suspend fun insertAll(users: List<User>)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int

    @Query("SELECT EXISTS(SELECT * FROM users WHERE phone = :phone)")
    suspend fun isUserExists(phone: String): Boolean

    @Query("SELECT role FROM users WHERE phone = :phone")
    suspend fun getRoleByPhone(phone: String): Role

    @Query("SELECT userId FROM users WHERE phone = :phone")
    suspend fun getUserIdByPhone(phone: String): Int

    @Query("SELECT * FROM users WHERE userId = :id")
    suspend fun getUserById(id: Int): User
}