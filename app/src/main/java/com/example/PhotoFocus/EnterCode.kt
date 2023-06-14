package com.example.PhotoFocus

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.PhotoFocus.databinding.ActivityEnterCodeBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EnterCode : AppCompatActivity() {
    private lateinit var binding: ActivityEnterCodeBinding

    private val retrofitService: RetrofitService = RetrofitService()

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
            val email : String = intent.getStringExtra("email").toString()
            code_text = binding.codeRegistraion.text.toString()
            sendCode(email, code_text)
        }
    }

    fun sendCode(email: String, code: String) {
        val jsonObject = JSONObject()
        jsonObject.put("email", email)
        jsonObject.put("code", code)

        val body = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            jsonObject.toString()
        )

        retrofitService.retrofit.sendCode(body).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>?, response: Response<String>) {
                if (response.isSuccessful) {
                    val intent = Intent(this@EnterCode, Authorization::class.java)
                    startActivity(intent)
                    return
                }
                if (response.code() == 500) {
                    Toast.makeText(this@EnterCode,
                        "Проблема сервера, попробуйте позже",
                        Toast.LENGTH_SHORT).show();
                    return
                }
                if (response.code() == 408) {
                    Toast.makeText(this@EnterCode,
                        "Неверный код, попробуйте заново",
                        Toast.LENGTH_SHORT).show();
                    return
                }
            }
            override fun onFailure(call: Call<String>?, t: Throwable?) {}
        })
    }

}