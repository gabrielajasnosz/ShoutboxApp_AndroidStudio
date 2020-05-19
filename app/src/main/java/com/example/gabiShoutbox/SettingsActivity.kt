package com.example.gabiShoutbox

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity


class SettingsActivity : AppCompatActivity() {

    lateinit var loginInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        loginInput = findViewById(R.id.loginInput);
        loginInput.setText(loadData())
    }

    private fun loadData(): String {
        val sharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
        return sharedPreferences.getString("login", "")!!
    }

    fun saveData(view: View) {
        val sharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val login = loginInput.text.toString()
        editor.putString("login", login)
        editor.apply()
        finish()
    }


}
