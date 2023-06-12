package com.example.PhotoFocus

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserTemplates : AppCompatActivity() {
    private var templatesQuestion: LinearLayout? = null
    private var createTemplates: LinearLayout? = null

    private var nameTemplates: EditText? = null

    private val retrofitService: RetrofitService = RetrofitService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_templates)

        templatesQuestion = findViewById(R.id.templatesQuestion)
        createTemplates = findViewById(R.id.createTemplates)

        val id = intent.getStringExtra("id").toString().replace("\n", "")

        val tone = intent.getFloatExtra("tone", 0.0F)
        val saturation = intent.getFloatExtra("saturation", 0.0F)
        val bright = intent.getFloatExtra("bright", 0.0F)
        val exposition = intent.getFloatExtra("exposition", 0.0F)
        val contrast = intent.getFloatExtra("contrast", 0.0F)
        val blur = intent.getFloatExtra("blur", 0.0F)
        val noise = intent.getFloatExtra("noise", 0.0F)
        val vignette = intent.getFloatExtra("vignette", 0.0F)

        nameTemplates = findViewById(R.id.nameTemplates)
        val btnYes = findViewById<Button>(R.id.btnYes)
        val btnNo = findViewById<Button>(R.id.btnNo)
        val btnSave = findViewById<Button>(R.id.btnCreateTemplates)

        btnYes.setOnClickListener {
            templatesQuestion?.visibility = View.GONE
            createTemplates?.visibility = View.VISIBLE
        }

        btnNo.setOnClickListener {
            val intent = Intent(this, GalleryActivity::class.java)
            intent.putExtra("screen", "authorization")
            startActivity(intent)
        }
        btnSave.setOnClickListener {
            var name = nameTemplates?.text.toString()
            saveTemplate(name, id, tone, saturation, bright, exposition, contrast, blur, noise, vignette)
        }
    }

    fun saveTemplate(
        name: String,
        userId: String,
        tone: Float,
        saturation: Float,
        bright: Float,
        exposition: Float,
        contrast: Float,
        blur: Float,
        noise: Float,
        vignette: Float
    ) {
        val jsonObject = JSONObject()
        jsonObject.put("name", name)
        jsonObject.put("user_id", userId.toInt())
        jsonObject.put("tone", tone)
        jsonObject.put("saturation", saturation)
        jsonObject.put("bright", bright)
        jsonObject.put("exposition", exposition)
        jsonObject.put("contrast", contrast)
        jsonObject.put("blur", blur)
        jsonObject.put("noise", noise)
        jsonObject.put("vignette", vignette)

        val body = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            jsonObject.toString()
        )

        retrofitService.retrofit.saveTemplate(body).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.code() == 201) {
                    val bundle = Bundle()
                    val intent = Intent(this@UserTemplates, GalleryActivity::class.java)
                    bundle.putString("id", userId)
                    bundle.putString("screen", "authorization")
                    intent.putExtras(bundle)
                    startActivity(intent)
                }
                if (response.code() == 502) {
                    Toast.makeText(this@UserTemplates,
                        "Ошибка сервера, попробуйте позже",
                        Toast.LENGTH_SHORT).show();
                    return
                }
            }
            override fun onFailure(call: Call<String>?, t: Throwable?) {}
        })
    }
}