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
        val cropOriginal = findViewById<TextView>(R.id.cropOriginal)
        val crop1_1 = findViewById<TextView>(R.id.crop1_1)
        val crop1_2 = findViewById<TextView>(R.id.crop1_2)
        val crop16_9 = findViewById<TextView>(R.id.crop16_9)
        val crop4_3 = findViewById<TextView>(R.id.crop4_3)
        val crop3_1 = findViewById<TextView>(R.id.crop3_1)

        val rotationLayout = findViewById<LinearLayout>(R.id.rotationLayout)
        val leftRotation = findViewById<TextView>(R.id.left)
        val rightRotation = findViewById<TextView>(R.id.right)
        val flipHor = findViewById<TextView>(R.id.hor)
        val flipVert = findViewById<TextView>(R.id.vert)
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
            cropOriginal.setOnClickListener {
                editImageBinding.cropImageView.setFixedAspectRatio(false)
            }
            crop1_1.setOnClickListener {
                editImageBinding.cropImageView.setAspectRatio(1,1)
            }
            crop1_2.setOnClickListener {
                editImageBinding.cropImageView.setAspectRatio(1,2)
            }
            crop16_9.setOnClickListener {
                editImageBinding.cropImageView.setAspectRatio(16,9)
            }
            crop4_3.setOnClickListener {
                editImageBinding.cropImageView.setAspectRatio(4,3)
            }
            crop3_1.setOnClickListener {
                editImageBinding.cropImageView.setAspectRatio(3,1)
            }
            cropping.setOnClickListener {
                editImageBinding.cropImageView.setFixedAspectRatio(false)
                fixCropLayout.visibility=View.GONE
                rotationLayout.visibility=View.GONE
                cropping.setTextColor(resources.getColor(R.color.button))
                rotation.setTextColor(resources.getColor(R.color.white))
                fixcropping.setTextColor(resources.getColor(R.color.white))
            }
            rotation.setOnClickListener {
                fixCropLayout.visibility=View.GONE
                rotationLayout.visibility=View.VISIBLE
                rotation.setTextColor(resources.getColor(R.color.button))
                cropping.setTextColor(resources.getColor(R.color.white))
                fixcropping.setTextColor(resources.getColor(R.color.white))
                leftRotation.setOnClickListener {
                    editImageBinding.cropImageView.rotateImage(-90)
                }
                rightRotation.setOnClickListener {
                    editImageBinding.cropImageView.rotateImage(90)
                }
                flipHor.setOnClickListener {
                    editImageBinding.cropImageView.flipImageHorizontally()
                }
                flipVert.setOnClickListener {
                    editImageBinding.cropImageView.flipImageVertically()
                }
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