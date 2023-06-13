package com.example.PhotoFocus

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class EditImageModel(private val activity: EditImageActivity) {

    var bitmap: Bitmap? = null
    var dstBitmap: Bitmap? = null
    var originalWidth = 0
    var originalHeight = 0

    fun loadImage(imagePath: String) {
        Glide.with(activity)
            .asBitmap()
            .load(imagePath)
            .apply(RequestOptions().centerInside())
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    activity.showImage(resource)
                    bitmap = resource
                    dstBitmap = bitmap?.copy(bitmap?.config, true)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })

    }

  /*  private fun scaleBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val scaleFactor = Float.min(
            maxWidth.toFloat() / bitmap.width,
            maxHeight.toFloat() / bitmap.height
        )

        val newWidth = (bitmap.width * scaleFactor).toInt()
        val newHeight = (bitmap.height * scaleFactor).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }*/

    fun saveImageToGallery(bitmap: Bitmap) {
        val imageName = "photofocus_${System.currentTimeMillis()}.jpg"
        val dateTaken = System.currentTimeMillis()
        var fos: OutputStream? = null
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, imageName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    put(MediaStore.Images.Media.DATE_ADDED, dateTaken / 1000)
                    put(MediaStore.Images.Media.DATE_TAKEN, dateTaken)
                }
                val resolver = activity.contentResolver
                val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let {
                    resolver.openOutputStream(it)
                }
            }
        }
        else {
            val imagesDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDirectory, imageName)
            fos = FileOutputStream(image)
            MediaScannerConnection.scanFile(activity, arrayOf(image.absolutePath), null, null)
        }
        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
    }

    private external fun myBlur(bitmapIn: Bitmap, bitmapOut: Bitmap, sigma: Float)
    private external fun myNoise(bitmapIn: Bitmap, bitmapOut: Bitmap, sigma: Float)
    private external fun myTone(bitmapIn: Bitmap, bitmapOut: Bitmap, sigma: Float)
    private external fun myExposition(bitmapIn: Bitmap, bitmapOut: Bitmap, sigma: Float)
    private external fun myContrast(bitmapIn: Bitmap, bitmapOut: Bitmap, sigma: Float)
    private external fun myBright(bitmapIn: Bitmap, bitmapOut: Bitmap, sigma: Float)
    private external fun mySaturation(bitmapIn: Bitmap, bitmapOut: Bitmap, sigma: Float)
    private external fun myVignette(bitmapIn: Bitmap, bitmapOut: Bitmap, sigma: Float)
    private external fun myAutocorrect(bitmapIn: Bitmap, bitmapOut: Bitmap)

    fun myAutocorrect() {
        myAutocorrect(dstBitmap!!, dstBitmap!!)
    }

    var toneText: Float? = null
    fun applyEffects(tone: Float, saturation: Float, bright: Float, exposition: Float, contrast: Float, blur: Float, noise: Float, vignette: Float): Bitmap {
        val tempBitmap = bitmap!!.copy(Bitmap.Config.ARGB_8888, true)

        toneText = noise

        val shouldApplyTone = tone != 100.0F
        val shouldApplySaturation = saturation != 100.0F
        val shouldApplyBright = bright != 100.0F
        val shouldApplyExposition = exposition != 100.0F
        val shouldApplyContrast = contrast != 100.0F
        val shouldApplyBlur = blur != 0.0F
        val shouldApplyNoise = noise != 0.0F
        val shouldApplyVignette = vignette != 0.0F

        if (shouldApplyTone) {
            myTone(tempBitmap, tempBitmap, tone)
        }
        if (shouldApplySaturation) {
            mySaturation(tempBitmap, tempBitmap, (saturation - 100) / 10)
        }
        if (shouldApplyBright) {
            myBright(tempBitmap, tempBitmap, (bright - 100)/10)
        }
        if (shouldApplyExposition) {
            myExposition(tempBitmap, tempBitmap, exposition / 10)
        }
        if (shouldApplyContrast) {
            myContrast(tempBitmap, tempBitmap, contrast / 10)
        }
        if (shouldApplyBlur) {
            myBlur(tempBitmap, tempBitmap, blur / 10)
        }
        if (shouldApplyNoise) {
            myNoise(tempBitmap, tempBitmap, noise / 10)
        }
        if (shouldApplyVignette) {
            myVignette(tempBitmap, tempBitmap, vignette / 10)
        }
        dstBitmap = tempBitmap
        return tempBitmap
    }
}