package com.example.PhotoFocus

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.canhub.cropper.*
import com.example.PhotoFocus.databinding.EditImageBinding

class EditImageActivity : AppCompatActivity() {

    private lateinit var editImageBinding: EditImageBinding

    private lateinit var bitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        editImageBinding = EditImageBinding.inflate(layoutInflater)

        supportActionBar?.hide()

        displayImagePreview()
        setContentView(editImageBinding.root)

        crop()
    }

    private fun crop() {
        val cropTools = findViewById<ConstraintLayout>(R.id.cropBtnsLayout)
        val cropping = findViewById<TextView>(R.id.croping)
        val backBtn = findViewById<Button>(R.id.backBtn)

        editImageBinding.cropBtn.setOnClickListener{
            cropTools.visibility = View.VISIBLE
            cropping.setOnClickListener {
                editImageBinding.cropImageView.setImageBitmap(bitmap)
            }
            backBtn.setOnClickListener{
                cropTools.visibility = View.GONE
                bitmap = editImageBinding.cropImageView.getCroppedImage()!!
                editImageBinding.imagePreview.setImageBitmap(bitmap)
                editImageBinding.cropImageView.clearImage()
            }
        }
    }

    private fun displayImagePreview() {
        if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra(MainActivity.KEY_IMAGE_URI, Uri::class.java)?.let { imageUri ->
                val inputStream = contentResolver.openInputStream(imageUri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                editImageBinding.imagePreview.setImageBitmap(bitmap)
                editImageBinding.imagePreview.visibility = View.VISIBLE
            }
        }else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<Uri>(MainActivity.KEY_IMAGE_URI).let { imageUri ->
                val inputStream = imageUri?.let { contentResolver.openInputStream(it) }
                bitmap = BitmapFactory.decodeStream(inputStream)
                editImageBinding.imagePreview.setImageBitmap(bitmap)
                editImageBinding.imagePreview.visibility = View.VISIBLE
            }
        }
    }
}