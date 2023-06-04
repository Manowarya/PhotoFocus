package com.example.PhotoFocus

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.PhotoFocus.databinding.ActivityAuthorizationBinding

class Authorization : AppCompatActivity() {
    private lateinit var binding: ActivityAuthorizationBinding

    private var email_text: String = ""
    private var password_text: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authorization)
        binding = ActivityAuthorizationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
    }

    private fun setListeners() {
        binding.btnSignIn.setOnClickListener {
            email_text = binding.emailSignIn.text.toString()
            password_text = binding.passwordSignIn.text.toString()
            val intent = Intent(this, GalleryActivity::class.java)
            intent.putExtra("screen", "authorization")
            startActivity(intent)
        }
        binding.textRegister.setOnClickListener {
            val intent = Intent(this, Registration::class.java)
            startActivity(intent)
        }
    }
}