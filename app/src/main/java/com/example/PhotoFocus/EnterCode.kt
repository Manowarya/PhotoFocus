package com.example.PhotoFocus

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import com.example.PhotoFocus.databinding.ActivityEnterCodeBinding

class EnterCode : AppCompatActivity() {
    private lateinit var binding: ActivityEnterCodeBinding

    private var code_text: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_code)
        binding = ActivityEnterCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
    }
    private fun setListeners() {
        binding.btnRegister.setOnClickListener {
            code_text = binding.codeRegistraion.text.toString()
            val intent = Intent(this, Authorization::class.java)
            startActivity(intent)
        }
    }
}