package com.example.PhotoFocus

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
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
        val cropping = findViewById<TextView>(R.id.cropping)
        val backBtn = findViewById<Button>(R.id.backBtn)
        val saveBtn = findViewById<Button>(R.id.saveBtn)
        val fixcropping = findViewById<TextView>(R.id.fixcropping)
        val fixCropLayout = findViewById<LinearLayout>(R.id.fixCropLayout)
        val rotationLayout = findViewById<LinearLayout>(R.id.rotationLayout)
        val rotation = findViewById<TextView>(R.id.rotation)

        editImageBinding.cropBtn.setOnClickListener{
            saveBtn.visibility = View.GONE
            cropTools.visibility = View.VISIBLE
            editImageBinding.cropImageView.setImageBitmap(bitmap)
            editImageBinding.imagePreview.setImageResource(0)
            fixcropping.setTextColor(resources.getColor(R.color.button))
            fixCropLayout.visibility=View.VISIBLE
            rotationLayout.visibility=View.GONE
            cropping.setTextColor(resources.getColor(R.color.white))
            rotation.setTextColor(resources.getColor(R.color.white))
            cropping.setOnClickListener {
                editImageBinding.cropImageView.setFixedAspectRatio(false)
                fixCropLayout.visibility=View.GONE
                rotationLayout.visibility=View.GONE
                cropping.setTextColor(resources.getColor(R.color.button))
                rotation.setTextColor(resources.getColor(R.color.white))
                fixcropping.setTextColor(resources.getColor(R.color.white))
            }
            backBtn.setOnClickListener{
                saveBtn.visibility = View.VISIBLE
                rotation.setTextColor(resources.getColor(R.color.white))
                cropping.setTextColor(resources.getColor(R.color.white))
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