package com.example.PhotoFocus

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.GridLayoutManager

class GalleryActivity : AppCompatActivity() {

    private var imageRecycler:RecyclerView?=null
    private var allPictures:ArrayList<Image>?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        imageRecycler=findViewById(R.id.image_recycler)

        imageRecycler?.layoutManager=GridLayoutManager(this, 3)
        imageRecycler?.setHasFixedSize(true)

        val permission: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 101)
        }
        allPictures = ArrayList()
        if(allPictures!!.isEmpty()){
            allPictures=getAllImages()

            imageRecycler?.adapter=GalleryAdapter(this, allPictures!!)
        }
    }

    private fun getAllImages(): ArrayList<Image>? {
        val images=ArrayList<Image>()
        val allImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.ImageColumns.DATA, MediaStore.Images.Media.DISPLAY_NAME)
        val cursor=this@GalleryActivity.contentResolver.query(allImageUri, projection, null, null, null)
        try {
            cursor!!.moveToFirst()
            do {
                val image=Image()
                image.imagePath=cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                images.add(image)
            }while (cursor.moveToNext())
            cursor.close()
        }catch (e:java.lang.Exception){
            e.printStackTrace()
        }
        return images
    }
}