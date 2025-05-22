package com.example.service

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.core.content.edit

object SessionManager {
    private const val CLIENT_ID = "client_id"
    private const val WORKER_ID = "worker_id"
    private const val PREFS = "AppPrefs"

    fun saveClientId(context: Context, id: Int){
        context.getSharedPreferences(PREFS, MODE_PRIVATE).edit(){
            putInt(CLIENT_ID, id)
            remove(WORKER_ID)
            apply()
        }
    }

    fun saveWorkerId(context: Context, id: Int){
        context.getSharedPreferences(PREFS, MODE_PRIVATE).edit(){
            putInt(WORKER_ID, id)
            remove(CLIENT_ID)
            apply()
        }
    }

    fun clearSession(context: Context){
        context.getSharedPreferences(PREFS, MODE_PRIVATE).edit(){
            remove(CLIENT_ID)
            remove(WORKER_ID)
            apply()
        }
    }

    fun getClientId(context: Context): Int{
        return context.getSharedPreferences(PREFS, MODE_PRIVATE).getInt(CLIENT_ID, -1)
    }
    fun getWorkerId(context: Context): Int{
        return context.getSharedPreferences(PREFS, MODE_PRIVATE).getInt(WORKER_ID, -1)
    }
}