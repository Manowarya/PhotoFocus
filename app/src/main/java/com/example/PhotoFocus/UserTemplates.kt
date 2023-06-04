package com.example.PhotoFocus

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout

class UserTemplates : AppCompatActivity() {
    private var templatesQuestion: LinearLayout? = null
    private var createTemplates: LinearLayout? = null

    private var nameTemplates: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_templates)

        templatesQuestion = findViewById(R.id.templatesQuestion)
        createTemplates = findViewById(R.id.createTemplates)

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
            val intent = Intent(this, GalleryActivity::class.java)
            intent.putExtra("screen", "authorization")
            startActivity(intent)
        }
    }
}