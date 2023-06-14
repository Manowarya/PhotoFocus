package com.example.PhotoFocus

import android.content.Intent
import android.database.Observable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.PhotoFocus.databinding.ActivityAuthorizationBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Authorization : AppCompatActivity() {
    private lateinit var binding: ActivityAuthorizationBinding

    private val retrofitService: RetrofitService = RetrofitService()

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
            authorization(email_text, password_text)
        }
        binding.textRegister.setOnClickListener {
            val intent = Intent(this, Registration::class.java)
            startActivity(intent)
        }
    }

    fun authorization(email: String, password: String){
        val jsonObject = JSONObject()
        jsonObject.put("email", email)
        jsonObject.put("password", password)

        val body = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            jsonObject.toString()
        )
        retrofitService.retrofit.authorization(body).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    val id = response.body().toString().replace("\n", "")
                    val bundle = Bundle()
                    val intent = Intent(this@Authorization, GalleryActivity::class.java)
                    bundle.putString("id", id)
                    bundle.putString("screen", "authorization")
                    intent.putExtras(bundle)
                    startActivity(intent)
                }
                if (response.code() == 400) {
                    Toast.makeText(this@Authorization,
                        "Проблема соединения",
                        Toast.LENGTH_SHORT).show();
                    return
                }
                if (response.code() == 401) {
                    Toast.makeText(this@Authorization,
                        "Пользователя с такой почтой не существует",
                        Toast.LENGTH_SHORT).show();
                    return
                }
                if (response.code() == 409) {
                    Toast.makeText(this@Authorization,
                        "Неверный пароль",
                        Toast.LENGTH_SHORT).show();
                    return
                }
            }
            override fun onFailure(call: Call<String>?, t: Throwable?) {}
        })
    }
}