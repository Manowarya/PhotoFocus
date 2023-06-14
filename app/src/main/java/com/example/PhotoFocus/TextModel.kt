package com.example.PhotoFocus

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.MotionEvent
import android.widget.EditText
import android.widget.ImageView

class TextModel {
    private var isMoving = false
    private var previousX = 0f
    private var previousY = 0f

    fun handleTextTouch(
        editText: EditText,
        imagePreview: ImageView,
        motionEvent: MotionEvent
    ): Boolean {
        val imageRect = Rect()
        imagePreview.getGlobalVisibleRect(imageRect)

        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                isMoving = true
                previousX = motionEvent.rawX
                previousY = motionEvent.rawY
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isMoving) {
                    val dx = motionEvent.rawX - previousX
                    val dy = motionEvent.rawY - previousY
                    val newX = editText.x + dx
                    val newY = editText.y + dy

                    val newRect = Rect(
                        newX.toInt(),
                        newY.toInt(),
                        (newX + editText.width).toInt(),
                        (newY + editText.height).toInt()
                    )

                    val imageDrawable = imagePreview.drawable
                    val imageWidth = imageDrawable.intrinsicWidth
                    val imageHeight = imageDrawable.intrinsicHeight

                    val imageViewWidth = imagePreview.width
                    val imageViewHeight = imagePreview.height

                    val scaleFactorX = imageViewWidth.toFloat() / imageWidth.toFloat()
                    val scaleFactorY = imageViewHeight.toFloat() / imageHeight.toFloat()

                    val scale = if (scaleFactorX > scaleFactorY) scaleFactorY else scaleFactorX

                    val scaledImageWidth = (imageWidth * scale).toInt()
                    val scaledImageHeight = (imageHeight * scale).toInt()

                    val imageRect = Rect(
                        (imagePreview.x + (imageViewWidth - scaledImageWidth) / 2).toInt(),
                        (imagePreview.y + (imageViewHeight - scaledImageHeight) / 2).toInt(),
                        (imagePreview.x + (imageViewWidth + scaledImageWidth) / 2).toInt(),
                        (imagePreview.y + (imageViewHeight + scaledImageHeight) / 2).toInt()
                    )

                    if (imageRect.contains(newRect)) {
                        editText.x = newX
                        editText.y = newY
                        previousX = motionEvent.rawX
                        previousY = motionEvent.rawY
                    }
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                isMoving = false
                return true
            }
            else -> return false
        }
    }

    fun combineImageAndText(bitmap: Bitmap, imagePreview: ImageView, editText: EditText): Bitmap? {
        val resultBitmap = bitmap.copy(bitmap.config, true)
        val canvas = Canvas(resultBitmap)

        val text = editText.text.toString()
        val textSize = editText.textSize
        val textColor = editText.currentTextColor
        val textPaddingLeft = editText.paddingLeft
        val textPaddingTop = editText.paddingTop
        val textFont = editText.typeface

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.textSize = textSize
        paint.color = textColor
        paint.typeface = textFont

        val textBounds = Rect()
        paint.getTextBounds(text, 0, text.length, textBounds)

        val x = editText.x - textPaddingLeft
        val y = editText.y - textPaddingTop + textBounds.height()

        val imageDrawable = imagePreview.drawable
        val imageWidth = imageDrawable.intrinsicWidth
        val imageHeight = imageDrawable.intrinsicHeight

        val imageViewWidth = imagePreview.width
        val imageViewHeight = imagePreview.height

        val scaleFactorX = imageViewWidth.toFloat() / imageWidth.toFloat()
        val scaleFactorY = imageViewHeight.toFloat() / imageHeight.toFloat()

        val scale = if (scaleFactorX > scaleFactorY) scaleFactorY else scaleFactorX

        val scaledImageWidth = (imageWidth * scale).toInt()
        val scaledImageHeight = (imageHeight * scale).toInt()

        val imageRect = Rect(
            (imagePreview.x + (imageViewWidth - scaledImageWidth) / 2).toInt(),
            (imagePreview.y + (imageViewHeight - scaledImageHeight) / 2).toInt(),
            (imagePreview.x + (imageViewWidth + scaledImageWidth) / 2).toInt(),
            (imagePreview.y + (imageViewHeight + scaledImageHeight) / 2).toInt()
        )

        val adjustedX = (x - imageRect.left) / scale
        val adjustedY = (y - imageRect.top) / scale

        val adjustedTextSize = textSize / scale
        val adjustedPaddingLeft = textPaddingLeft / scale
        val adjustedPaddingTop = textPaddingTop / scale

        val adjustedPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        adjustedPaint.textSize = adjustedTextSize
        adjustedPaint.color = textColor
        adjustedPaint.typeface = textFont

        val adjustedTextBounds = Rect()
        adjustedPaint.getTextBounds(text, 0, text.length, adjustedTextBounds)

        val adjustedXWithPadding = adjustedX + adjustedPaddingLeft
        val adjustedYWithPadding = adjustedY + adjustedPaddingTop + adjustedTextBounds.height()

        val adjustedYCorrection = adjustedTextBounds.height() - adjustedPaint.fontMetrics.descent
        val adjustedYWithPaddingCorrection = adjustedYWithPadding - adjustedYCorrection

        canvas.drawText(text, adjustedXWithPadding, adjustedYWithPaddingCorrection, adjustedPaint)
        return resultBitmap
    }
}