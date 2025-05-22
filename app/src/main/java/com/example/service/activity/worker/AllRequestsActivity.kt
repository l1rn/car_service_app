package com.example.service.activity.worker

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.service.R
import com.example.service.database.AppDatabase
import com.example.service.enums.Status
import com.example.service.models.Requisition
import kotlinx.coroutines.launch

class AllRequestsActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var adapter: AllRequestsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_all_requests)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = AppDatabase.getDatabase(this)
        adapter = AllRequestsAdapter(this)

        val listView = findViewById<ListView>(R.id.lWithAllRequests)

        loadAllRequests()
        backButton()

        listView.adapter = adapter
    }

    private fun loadAllRequests(){
        lifecycleScope.launch {
            val requests = db.requisitionDao().getAllRequests()
            adapter.submitList(requests)
        }
    }

    private fun backButton(){
        findViewById<Button>(R.id.bBackFromWorker).setOnClickListener {
            finish()
        }
    }

    inner class AllRequestsAdapter(context: Context): BaseAdapter(){
        private val inflater = LayoutInflater.from(context)
        private var allRequests = emptyList<Requisition>()

        fun submitList(newList: List<Requisition>){
            allRequests = newList
            notifyDataSetChanged()
        }

        override fun getCount(): Int = allRequests.size

        override fun getItem(p0: Int): Requisition = allRequests[p0]

        override fun getItemId(p0: Int): Long = allRequests[p0].requestId.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: inflater.inflate(R.layout.request_card, parent, false)
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

            view.findViewById<Button>(R.id.bMakeOpen).setOnClickListener {
                lifecycleScope.launch {
                    db.requisitionDao().setStatusById(status = Status.OPEN, id = item.requestId)
                    loadAllRequests()
                }
            }

            view.findViewById<Button>(R.id.bMakeInProgress).setOnClickListener {
                lifecycleScope.launch {
                    db.requisitionDao().setStatusById(status = Status.IN_PROGRESS, id = item.requestId)
                    loadAllRequests()
                }
            }
            view.findViewById<Button>(R.id.bMakeReady).setOnClickListener {
                lifecycleScope.launch {
                    db.requisitionDao().setStatusById(status = Status.READY, id = item.requestId)
                    loadAllRequests()
                }
            }
            view.findViewById<Button>(R.id.bMakeClose).setOnClickListener {
                lifecycleScope.launch {
                    db.requisitionDao().setStatusById(status = Status.CLOSE, id = item.requestId)
                    loadAllRequests()
                }
            }


            return view
        }

    }
}