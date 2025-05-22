package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.service.activity.client.AddRequestsActivity
import com.example.service.activity.worker.AllRequestsActivity
import com.example.service.dao.UserDao
import com.example.service.database.AppDatabase
import com.example.service.enums.Role
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var userDao: UserDao
    private lateinit var phone: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = AppDatabase.getDatabase(this)
        userDao = db.userDao()

        phone = findViewById<EditText>(R.id.ePhone)

        lifecycleScope.launch {
            userDao.count()
            db.requisitionDao().count()
        }
        checkRole(this, phone)
    }
    private fun checkRole(context: Context, phone: EditText){
        findViewById<Button>(R.id.bSignIn).setOnClickListener{
            val phoneString = phone.text.toString()
            if(phoneString.isEmpty() || phoneString.isBlank()){
                Toast.makeText(context, "Поле с номером пустое!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val isUserExists: Boolean = userDao.isUserExists(phoneString)
                if(isUserExists){
                    val role: Role = userDao.getRoleByPhone(phoneString)
                    SessionManager.clearSession(context)

                    if(role == Role.CLIENT){
                        SessionManager.saveClientId(context, userDao.getUserIdByPhone(phoneString))
                    }
                    else if(role == Role.WORKER){
                        SessionManager.saveClientId(context, userDao.getUserIdByPhone(phoneString))
                    }
                    when(role) {
                        Role.CLIENT -> startActivity(Intent(context, AddRequestsActivity::class.java))
                        Role.WORKER -> startActivity(Intent(context, AllRequestsActivity::class.java))
                    }
                }
                else{
                    Toast.makeText(context, "Неверно введен номер!", Toast.LENGTH_SHORT).show()
                    return@launch
                }
            }
        }
    }
}