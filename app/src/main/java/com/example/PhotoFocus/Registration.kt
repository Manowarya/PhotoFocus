package com.example.PhotoFocus

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.PhotoFocus.databinding.ActivityRegistrationBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Registration : AppCompatActivity() {
    private lateinit var binding: ActivityRegistrationBinding

    private val retrofitService: RetrofitService = RetrofitService()

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
            } else if (password_text == "") {
                Toast.makeText(this, "Введите пароль", Toast.LENGTH_SHORT).show()
            }
            else {
                registerUser(email_text, password_text)
            }
        }
    }

    private fun registerUser(email: String, password: String) {
        val jsonObject = JSONObject()
        jsonObject.put("email", email)
        jsonObject.put("password", password)

        val body = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            jsonObject.toString()
        )

        retrofitService.retrofit.registerUser(body).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    val intent = Intent(this@Registration, EnterCode::class.java)
                    intent.putExtra("email", email)
                    startActivity(intent)
                }
                else if (response.code() == 400){
                    Toast.makeText(this@Registration,
                        "Проблема соединения",
                        Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (response.code() == 409){
                    Toast.makeText(this@Registration,
                        "Пользователь с такой почтой уже существует",
                        Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (response.code() == 500){
                    Toast.makeText(this@Registration,
                        "Проблема при отправке кода",
                        Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            override fun onFailure(call: Call<String>?, t: Throwable?) {}
        })
    }
}