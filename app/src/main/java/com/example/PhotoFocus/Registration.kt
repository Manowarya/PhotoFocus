package com.example.PhotoFocus

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.PhotoFocus.databinding.ActivityRegistrationBinding

class Registration : AppCompatActivity() {
    private lateinit var binding: ActivityRegistrationBinding

    private var email_text: String = ""
    private var password_text: String = ""
    private var password2_text: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
    }

    private fun setListeners() {
        binding.btnGetCode.setOnClickListener {
            email_text = binding.emailRegistration.text.toString()
            password_text = binding.passwordRegistration.text.toString()
            password2_text = binding.password2Registration.text.toString()

            if (password_text != password2_text) {
                Toast.makeText(this, "Пароли должны совпадать", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, EnterCode::class.java)
                startActivity(intent)
            }
        }
    }
}