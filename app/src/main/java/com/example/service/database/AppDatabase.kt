package com.example.service.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.service.dao.RequisitionDao
import com.example.service.dao.UserDao
import com.example.service.enums.Role
import com.example.service.enums.Status
import com.example.service.models.Requisition
import com.example.service.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        User::class,
        Requisition::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase(){
    abstract fun userDao(): UserDao
    abstract fun requisitionDao(): RequisitionDao

    companion object{
        @Volatile
        private var INSTANCE: AppDatabase ?= null

        fun getDatabase(context: Context): AppDatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "service_db"
                )
                .addCallback(DatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback(){
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                val database = getDatabase(context)
                initDefaultDatabase(database)
            }
        }
        private suspend fun initDefaultDatabase(db: AppDatabase){
            if(db.userDao().count() == 0){
                val users = listOf(
                    // clients
                    User(userId = 2, name = "Попов Роман Романович", phone = "8999777666", role = Role.CLIENT),
                    User(userId = 3, name = "Иванова Анна Сергеевна", phone = "8999888777", role = Role.CLIENT),
                    User(userId = 4, name = "Смирнов Дмитрий Игоревич", phone = "8999555444", role = Role.CLIENT),
                    User(userId = 5, name = "Кузнецова Елена Викторовна", phone = "8999333222", role = Role.CLIENT),
                    User(userId = 6, name = "Петров Алексей Николаевич", phone = "8999111000", role = Role.CLIENT),
                    User(userId = 7, name = "Васильева Ольга Дмитриевна", phone = "8999444333", role = Role.CLIENT),
                    // workers
                    User(userId = 8, name = "Соколов Артём Владимирович", phone = "8998666555", role = Role.WORKER),
                    User(userId = 9, name = "Морозова Наталья Александровна", phone = "8998777444", role = Role.WORKER),
                    User(userId = 10, name = "Лебедев Иван Петрович", phone = "8998123456", role = Role.WORKER),
                    User(userId = 11, name = "Григорьева Мария Олеговна", phone = "8998234567", role = Role.WORKER),
                    User(userId = 12, name = "Федоров Павел Денисович", phone = "8998345678", role = Role.WORKER)
                )
                db.userDao().insertAll(users)
            }
            if(db.requisitionDao().count() == 0){
                val requests = listOf(
                    Requisition(requestId = 2, userId = 2, status = Status.OPEN, date = "22.10.2025", reason = "Движок сломан"),
                    Requisition(requestId = 3, userId = 2, status = Status.READY, date = "23.10.2025", reason = "Коробка"),
                    Requisition(requestId = 4, userId = 2, status = Status.IN_PROGRESS, date = "24.10.2025", reason = "Шины"),
                    Requisition(requestId = 5, userId = 2, status = Status.CLOSE, date = "25.10.2025", reason = "Стекло"),
                )
                db.requisitionDao().insertAll(requests)
            }
        }
    }
}