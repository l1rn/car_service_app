package com.example.service.activity.client

import android.app.DatePickerDialog
import android.content.Context
import android.icu.util.Calendar
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setPadding
import androidx.lifecycle.lifecycleScope
import com.example.service.R
import com.example.service.SessionManager
import com.example.service.database.AppDatabase
import com.example.service.enums.Status
import com.example.service.models.Requisition
import com.example.service.models.User
import kotlinx.coroutines.launch

class AddRequestsActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var adapter: AddRequestsAdapter
    private var clientId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_requests)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        db = AppDatabase.getDatabase(this)
        clientId = SessionManager.getClientId(this)
        if(clientId == -1){
            Toast.makeText(this, "Возникла ошибка", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        adapter = AddRequestsAdapter(this)
        val listView = findViewById<ListView>(R.id.lAllRequestsFromClient)

        loadAllRequests()
        backFromActivity()
        addButton()

        listView.adapter = adapter
    }

    private fun loadAllRequests(){
        lifecycleScope.launch {
            val requests = db.requisitionDao().getAllRequestsById(clientId)
            adapter.submitList(requests)
        }
    }

    private fun backFromActivity(){
        findViewById<Button>(R.id.bBackButtonFromAdd).setOnClickListener{
            finish()
        }
    }

    private fun addButton(){
        findViewById<Button>(R.id.bButtonAddRequest).setOnClickListener {
            val container = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(32, 16, 32, 16)
            }

            val dataEditExit = EditText(this).apply {
                hint = "Введите дату"
                isFocusable = false
                setOnClickListener {
                    showDatePickerDialog(this)
                }
            }
            val reasonEditText = EditText(this).apply {
                hint = "Введите причину"
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            container.addView(dataEditExit)
            container.addView(reasonEditText)

            val alertDialog = AlertDialog.Builder(this)
                .setTitle("Причина / Дата")
                .setView(container)
                .setPositiveButton("Потвердить") { dialog, _ ->
                    val date = dataEditExit.text.toString()
                    val reason = reasonEditText.text.toString()

                    if(reason.isEmpty()){
                        Toast.makeText(this, "Напишите причину", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    if(date.isEmpty()){
                        Toast.makeText(this, "Выберите дату", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    val requisition = Requisition(
                        userId = clientId,
                        date = date,
                        reason = reason,
                        status = Status.OPEN
                    )
                    lifecycleScope.launch {
                        db.requisitionDao().insert(requisition)
                    }
                    Toast.makeText(this, "Заявка успешно создана!", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Отмена") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
            alertDialog.show()
        }
    }

    inner class AddRequestsAdapter(context: Context): BaseAdapter(){
        private val inflater = LayoutInflater.from(context)
        private var clientRequests = emptyList<Requisition>()

        fun submitList(newList: List<Requisition>){
            clientRequests = newList
            notifyDataSetChanged()
        }

        override fun getCount(): Int = clientRequests.size

        override fun getItem(p0: Int): Requisition = clientRequests[p0]

        override fun getItemId(p0: Int): Long = clientRequests[p0].requestId.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: inflater.inflate(R.layout.request_cliend_card, parent, false)
            val item = getItem(position)

            lifecycleScope.launch {
                view.findViewById<TextView>(R.id.tClientName).text =
                    db.userDao().getUserById(item.userId).name
                view.findViewById<TextView>(R.id.tClientPhone).text =
                    db.userDao().getUserById(item.userId).phone
            }

            view.findViewById<TextView>(R.id.tClientDate).text =
                item.date

            val status: String = when(item.status){
                Status.OPEN -> "🟢 Открыто"
                Status.CLOSE -> "🔴 Закрыто"
                Status.READY -> "🔵 Готово"
                Status.IN_PROGRESS -> "🟡 В работе"
            }

            view.findViewById<TextView>(R.id.tClientStatus).text =
                status

            view.findViewById<TextView>(R.id.tClientReason).text =
                item.reason
            return view
        }

    }

    private fun showDatePickerDialog(editText: EditText){
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            {
                _, year, month, day ->
                val selectedDate = "$day.${month+1}.$year"
                editText.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}