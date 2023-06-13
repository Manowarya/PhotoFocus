package com.example.PhotoFocus

import android.graphics.Bitmap

class EditImageController(private val activity: EditImageActivity, private val model: EditImageModel) {
    fun onSaveButtonClicked(bitmap: Bitmap) {
        //val dstBitmap = model.dstBitmap
            model.saveImageToGallery(bitmap)
            activity.showMessage("Success")
            activity.navigateToGallery()

    }
    fun onAutocorrectClicked() {
        val bitmap = model.dstBitmap
        model.myAutocorrect()
        activity.showImage(bitmap!!)
    }
}