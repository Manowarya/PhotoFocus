package com.example.PhotoFocus

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.GridLayoutManager

class GalleryActivity : AppCompatActivity() {
    private var imageRecycler:RecyclerView?=null
    private var allPictures:ArrayList<Image>?=null

    private var signIn: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        imageRecycler=findViewById(R.id.image_recycler)

        imageRecycler?.layoutManager=GridLayoutManager(this, 3)
        imageRecycler?.setHasFixedSize(true)

        val permission: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Manifest.permission.READ_EXTERNAL_STORAGE
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 101)
        }
        val screen = intent.getStringExtra("screen")
        val id = intent.getStringExtra("id")

        allPictures = ArrayList()
        if(allPictures!!.isEmpty()){
            allPictures=getAllImages()

            imageRecycler?.adapter=GalleryAdapter(this, allPictures!!, screen!!, id)
        }
        signIn = findViewById(R.id.btnSignInGallery)
        if (screen == "authorization") {
            signIn!!.visibility = View.GONE

        }
        signIn!!.setOnClickListener {
            val intent = Intent(this, Authorization::class.java)
            startActivity(intent)
        }
    }

    private fun getAllImages(): ArrayList<Image>? {
        val images = ArrayList<Image>()
        val allImageUri = MediaStore.Images.Media.getContentUri("external")
        val projection = arrayOf(
            MediaStore.Images.ImageColumns.DATA,
            MediaStore.Images.Media.DISPLAY_NAME
        )
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
        val cursor = this@GalleryActivity.contentResolver.query(
            allImageUri,
            projection,
            null,
            null,
            sortOrder
        )
        try {
            cursor?.moveToFirst()
            do {
                val image = Image()
                image.imagePath =
                    cursor?.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                images.add(image)
            } while (cursor?.moveToNext() == true)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        return images
    }

}