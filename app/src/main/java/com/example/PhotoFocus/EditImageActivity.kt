package com.example.PhotoFocus

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.PhotoFocus.databinding.EditImageBinding
import org.w3c.dom.Text
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.Float.max
import java.lang.Float.min

class EditImageActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener, TextWatcher {
    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }
    private var bitmap: Bitmap? = null
    private var dstBitmap: Bitmap? = null

    private lateinit var editImageBinding: EditImageBinding

    private var selectedTextView: TextView? = null
    private var selectedLinearLayout: LinearLayout? = null

    private var toneSeekBar: SeekBar? = null
    private var saturationSeekBar: SeekBar? = null
    private var brightSeekBar: SeekBar? = null
    private var expositionSeekBar: SeekBar? = null
    private var contrastSeekBar: SeekBar? = null
    private var blurSeekBar: SeekBar? = null
    private var noiseSeekBar: SeekBar? = null
    private var vignetteSeekBar: SeekBar? = null

    private var editTextTone: EditText? = null
    private var editTextSaturation: EditText? = null
    private var editTextBright: EditText? = null
    private var editTextExposition: EditText? = null
    private var editTextContrast: EditText? = null
    private var editTextBlur: EditText? = null
    private var editTextNoise: EditText? = null
    private var editTextVignette: EditText? = null

    private var backBtn: Button? = null
    val screenStack = mutableListOf<String>()
    private var toolsLayout: HorizontalScrollView? = null
    private var saveBtn: Button? = null
    private var correctionTools: ConstraintLayout? = null
    private var cropTools: ConstraintLayout? = null
    private var color: TextView? = null
    private var cropping: TextView? = null
    private var rotation: TextView? = null
    private var colorLinearLayout: LinearLayout? = null

    private lateinit var imagePreview: ImageView
    private lateinit var editText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editImageBinding = EditImageBinding.inflate(layoutInflater)
        supportActionBar?.hide()
        setContentView(editImageBinding.root)

        val imagePath = intent.getStringExtra("path")

        imagePreview = findViewById(R.id.imagePreview)

        imagePreview.setImageURI(Uri.parse(imagePath))

        imagePreview.visibility = View.VISIBLE

        bitmap = (imagePreview.drawable as BitmapDrawable).bitmap

        dstBitmap = bitmap!!.copy(bitmap!!.config, true)

        toolsLayout = findViewById(R.id.toolsLayout)


        editImageBinding.cropBtn.setOnClickListener {
            screenStack.add("crop")
            toolsLayout!!.visibility = View.GONE
            crop()
        }

        editImageBinding.correctionBtn.setOnClickListener {
            screenStack.add("correction")
            toolsLayout!!.visibility = View.GONE
            correction()
        }

        val autocorrectionBtn = findViewById<TextView>(R.id.autocorrectionBtn)
        autocorrectionBtn.setOnClickListener {
            myAutocorrect(bitmap!!, dstBitmap!!)
            editImageBinding.imagePreview.setImageBitmap(dstBitmap)
        }

        editImageBinding.textBtn.setOnClickListener {
            screenStack.add("text")
            toolsLayout!!.visibility = View.GONE
            myText()
        }

        backBtn = findViewById(R.id.backBtn)
        backBtn!!.setOnClickListener {
            onBackPressed()
        }

        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
            1
        )
        editText = findViewById(R.id.editText)
        saveBtn = findViewById(R.id.saveBtn)

        saveBtn?.setOnClickListener {
            val combinedBitmap = combineImageAndText(imagePreview.drawable, editText.text.toString())
            if (combinedBitmap != null) {
                saveImageToGallery(combinedBitmap)
            }
            onBackPressed()
            val intent = Intent(this, GalleryActivity::class.java)
            startActivity(intent)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun myText() {

        val textBtnsLayout = findViewById<ConstraintLayout>(R.id.textBtnsLayout)
        textBtnsLayout.visibility=View.VISIBLE
        editText.visibility= View.VISIBLE

        val fonts = findViewById<TextView>(R.id.fonts)
        val colorText = findViewById<TextView>(R.id.colorText)
        val fontsTextLayout = findViewById<LinearLayout>(R.id.fontsTextLayout)
        val colorTextLayout = findViewById<LinearLayout>(R.id.colorLinearLayout)
        val colorToolsLayout = findViewById<HorizontalScrollView>(R.id.colorToolsLayout)

        val textColorWhite = findViewById<ImageView>(R.id.textColorWhite)
        val textColorBlack = findViewById<ImageView>(R.id.textColorBlack)
        val textColorRed = findViewById<ImageView>(R.id.textColorRed)
        val textColorGreen = findViewById<ImageView>(R.id.textColorGreen)
        val textColorBlue = findViewById<ImageView>(R.id.textColorBlue)
        val textColorPurple = findViewById<ImageView>(R.id.textColorPurple)

        val textFont_1 = findViewById<ImageView>(R.id.textFont_1)
        val textFont_2 = findViewById<ImageView>(R.id.textFont_2)
        val textFont_3 = findViewById<ImageView>(R.id.textFont_3)

        var isMoving = false
        var previousX = 0f
        var previousY = 0f

        handleTextViewClick(colorText)
        linearLayoutVisible(colorTextLayout)

        editText.setOnTouchListener { _, motionEvent ->
            editText.requestFocus()
            false
        }
        imagePreview.setOnTouchListener { view, motionEvent ->
            val imageRect = Rect()
            imagePreview.getGlobalVisibleRect(imageRect)

            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    isMoving = true
                    previousX = motionEvent.rawX
                    previousY = motionEvent.rawY
                    true
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
                    true
                }
                MotionEvent.ACTION_UP -> {
                    isMoving = false
                    true
                }
                else -> false
            }
        }
        colorText.setOnClickListener {
            handleTextViewClick(colorText)
            linearLayoutVisible(colorTextLayout)
            colorToolsLayout.visibility = View.VISIBLE
        }
        textColorWhite.setOnClickListener{
            editText.setTextColor(ContextCompat.getColor(applicationContext, R.color.white))
        }
        textColorBlack.setOnClickListener {
            editText.setTextColor(ContextCompat.getColor(applicationContext, R.color.black))
        }
        textColorRed.setOnClickListener {
            editText.setTextColor(ContextCompat.getColor(applicationContext, R.color.red))
        }
        textColorGreen.setOnClickListener {
            editText.setTextColor(ContextCompat.getColor(applicationContext, R.color.green))
        }
        textColorBlue.setOnClickListener {
            editText.setTextColor(ContextCompat.getColor(applicationContext, R.color.blue))
        }
        textColorPurple.setOnClickListener {
            editText.setTextColor(ContextCompat.getColor(applicationContext, R.color.purple_200))
        }
        val typeface = ResourcesCompat.getFont(this, R.font.nevduplenysh_regular)
        fonts.setOnClickListener {

            val bitmap = Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val paint = Paint()
            paint.typeface = typeface
            paint.textSize = 48f
            paint.color = Color.BLACK

            canvas.drawText("Abcd", 25f, 120f, paint)

            textFont_1.setImageBitmap(bitmap)

            handleTextViewClick(fonts)
            linearLayoutVisible(fontsTextLayout!!)
            colorToolsLayout.visibility = View.GONE
        }
        textFont_1.setOnClickListener {
            editText.typeface = typeface
        }
    }
    fun combineImageAndText(imageDrawable: Drawable, text: String): Bitmap? {
        val imageBitmap = (imageDrawable as BitmapDrawable).bitmap

        val scaleFactorX = imageBitmap.width.toFloat() / imagePreview.width.toFloat()
        val scaleFactorY = imageBitmap.height.toFloat() / imagePreview.height.toFloat()
        val scale = if (scaleFactorX > scaleFactorY) scaleFactorY else scaleFactorX

        val textX = (editText.x - imagePreview.x) * scale + (imageBitmap.width - imagePreview.width * scale) / 2f
        val textY = (editText.y - imagePreview.y) * scale + (imageBitmap.height - imagePreview.height * scale) / 2f + editText.textSize / 2f

        val combinedBitmap = Bitmap.createBitmap(imageBitmap.width, imageBitmap.height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(combinedBitmap)
        canvas.drawBitmap(imageBitmap, 0f, 0f, null)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint.color = editText.currentTextColor
        textPaint.textSize = editText.textSize * scale
        textPaint.setTypeface(editText.typeface)

        canvas.drawText(text, textX, textY, textPaint)

        return combinedBitmap
    }

    fun saveImageToGallery(bitmap: Bitmap) {
        val imageName = "photofocus_${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q) {
            this.contentResolver?.also {resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, imageName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
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
        }
        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
        }
    }

    private fun crop() {
        cropTools = findViewById(R.id.cropBtnsLayout)
        cropping = findViewById(R.id.cropping)

        val   fixcropping = findViewById<TextView>(R.id.fixcropping)
        val fixCropLayout = findViewById<LinearLayout>(R.id.fixCropLayout)
        val cropOriginal = findViewById<TextView>(R.id.cropOriginal)
        val crop11 = findViewById<TextView>(R.id.crop1_1)
        val crop12 = findViewById<TextView>(R.id.crop1_2)
        val crop169 = findViewById<TextView>(R.id.crop16_9)
        val crop43 = findViewById<TextView>(R.id.crop4_3)
        val crop31 = findViewById<TextView>(R.id.crop3_1)

        val rotationLayout = findViewById<LinearLayout>(R.id.rotationLayout)
        val leftRotation = findViewById<TextView>(R.id.left)
        val rightRotation = findViewById<TextView>(R.id.right)
        val flipHor = findViewById<TextView>(R.id.hor)
        val flipVert = findViewById<TextView>(R.id.vert)
        rotation = findViewById(R.id.rotation)

        saveBtn!!.visibility = View.GONE
        cropTools!!.visibility = View.VISIBLE
        editImageBinding.cropImageView.setImageBitmap(bitmap)
        editImageBinding.imagePreview.setImageResource(0)

        handleTextViewClick(fixcropping)

        fixcropping.setOnClickListener {
            handleTextViewClick(fixcropping)
            linearLayoutVisible(fixCropLayout)
        }
        cropOriginal.setOnClickListener {
            editImageBinding.cropImageView.setFixedAspectRatio(false)
        }
        crop11.setOnClickListener {
            editImageBinding.cropImageView.setAspectRatio(1, 1)
        }
        crop12.setOnClickListener {
            editImageBinding.cropImageView.setAspectRatio(1, 2)
        }
        crop169.setOnClickListener {
            editImageBinding.cropImageView.setAspectRatio(16, 9)
        }
        crop43.setOnClickListener {
            editImageBinding.cropImageView.setAspectRatio(4, 3)
        }
        crop31.setOnClickListener {
            editImageBinding.cropImageView.setAspectRatio(3, 1)
        }

        cropping!!.setOnClickListener {
            handleTextViewClick(cropping!!)
            editImageBinding.cropImageView.setFixedAspectRatio(false)
            fixCropLayout.visibility = View.GONE
            rotationLayout.visibility = View.GONE
        }
        rotation!!.setOnClickListener {
            handleTextViewClick(rotation!!)
            linearLayoutVisible(rotationLayout)
        }
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
        backBtn!!.setOnClickListener{
            onBackPressed()
        }
    }

    private fun correction() {
        correctionTools = findViewById(R.id.correctionBtnsLayout)
        colorLinearLayout = findViewById(R.id.colorLinearLayout)
        val lightLinearLayout = findViewById<LinearLayout>(R.id.lightLinearLayout)
        val blurLinearLayout = findViewById<LinearLayout>(R.id.blurLinearLayout)
        val noiseLinearLayout = findViewById<LinearLayout>(R.id.noiseLinearLayout)
        val vignetteLinearLayout = findViewById<LinearLayout>(R.id.vignetteLinearLayout)

        color = findViewById(R.id.color)
        val light = findViewById<TextView>(R.id.light)
        val noise = findViewById<TextView>(R.id.noise)
        val blur = findViewById<TextView>(R.id.blur)
        val vignette = findViewById<TextView>(R.id.vignette)

        editTextTone = findViewById(R.id.editTextTone)
        editTextSaturation = findViewById(R.id.editTextSaturation)
        editTextBright = findViewById(R.id.editTextBright)
        editTextExposition = findViewById(R.id.editTextExposition)
        editTextContrast = findViewById(R.id.editTextContrast)
        editTextBlur = findViewById(R.id.editTextBlur)
        editTextNoise = findViewById(R.id.editTextNoise)
        editTextVignette = findViewById(R.id.editTextVignette)

        editTextTone!!.addTextChangedListener(this)
        editTextSaturation!!.addTextChangedListener(this)
        editTextBright!!.addTextChangedListener(this)
        editTextExposition!!.addTextChangedListener(this)
        editTextContrast!!.addTextChangedListener(this)
        editTextBlur!!.addTextChangedListener(this)
        editTextNoise!!.addTextChangedListener(this)
        editTextVignette!!.addTextChangedListener(this)

        if (toneSeekBar == null)
            toneSeekBar = findViewById(R.id.toneSeekBar)
        if (saturationSeekBar == null)
            saturationSeekBar = findViewById(R.id.saturationSeekBar)
        if (brightSeekBar == null)
            brightSeekBar = findViewById(R.id.brightSeekBar)
        if (expositionSeekBar == null)
            expositionSeekBar = findViewById(R.id.expositionSeekBar)
        if (contrastSeekBar == null)
            contrastSeekBar = findViewById(R.id.contrastSeekBar)
        if (noiseSeekBar == null)
            noiseSeekBar = findViewById(R.id.noiseSeekBar)
        if (blurSeekBar == null)
            blurSeekBar = findViewById(R.id.blurSeekBar)
        if (vignetteSeekBar == null)
            vignetteSeekBar = findViewById(R.id.vignetteSeekBar)

        toneSeekBar!!.setOnSeekBarChangeListener(this)
        toneSeekBar!!.max = 200
        toneSeekBar!!.progress = 100

        saturationSeekBar!!.setOnSeekBarChangeListener(this)
        saturationSeekBar!!.max = 200
        saturationSeekBar!!.progress = 100

        brightSeekBar!!.setOnSeekBarChangeListener(this)
        brightSeekBar!!.max = 200
        brightSeekBar!!.progress = 100

        expositionSeekBar!!.setOnSeekBarChangeListener(this)
        expositionSeekBar!!.max = 200
        expositionSeekBar!!.progress = 100

        contrastSeekBar!!.setOnSeekBarChangeListener(this)
        contrastSeekBar!!.max = 200
        contrastSeekBar!!.progress = 100

        blurSeekBar!!.setOnSeekBarChangeListener(this)
        blurSeekBar!!.max = 100
        blurSeekBar!!.progress = 0

        noiseSeekBar!!.setOnSeekBarChangeListener(this)
        noiseSeekBar!!.max = 100
        noiseSeekBar!!.progress = 0

        vignetteSeekBar!!.setOnSeekBarChangeListener(this)
        vignetteSeekBar!!.max = 100
        vignetteSeekBar!!.progress = 0

        selectedLinearLayout = colorLinearLayout
        handleTextViewClick(color!!)
        saveBtn!!.visibility = View.GONE

        color!!.setOnClickListener{
            handleTextViewClick(color!!)
            linearLayoutVisible(colorLinearLayout!!)
        }
        light.setOnClickListener{
            handleTextViewClick(light)
            linearLayoutVisible(lightLinearLayout)

        }
        noise.setOnClickListener {
            handleTextViewClick(noise)
            linearLayoutVisible(noiseLinearLayout)
        }
        blur.setOnClickListener {
            handleTextViewClick(blur)
            linearLayoutVisible(blurLinearLayout)

        }
        vignette.setOnClickListener{
            handleTextViewClick(vignette)
            linearLayoutVisible(vignetteLinearLayout)
        }

        correctionTools!!.visibility = View.VISIBLE

        backBtn!!.setOnClickListener{
            onBackPressed()
        }
    }
    private fun handleTextViewClick(textView: TextView) {
        selectedTextView?.setTextColor(ContextCompat.getColor(applicationContext, R.color.white))
        textView.setTextColor(ContextCompat.getColor(applicationContext, R.color.button))
        selectedTextView = textView
    }
    private fun linearLayoutVisible(linearLayout: LinearLayout) {
        selectedLinearLayout?.visibility=View.GONE
        linearLayout.visibility=View.VISIBLE
        selectedLinearLayout = linearLayout
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

    private var tone: Float = 0.0F
    private var saturation: Float = 1.0F
    private var bright: Float = 0.0F
    private var exposition: Float = 0.0F
    private var contrast: Float = 0.0F
    private var blur: Float = 0.0F
    private var noise: Float = 0.0F
    private var vignette: Float = 0.0F

    private var shouldApplyTone: Boolean = false
    private var shouldApplySaturation: Boolean = false
    private var shouldApplyBright: Boolean = false
    private var shouldApplyExposition: Boolean = false
    private var shouldApplyContrast: Boolean = false
    private var shouldApplyBlur: Boolean = false
    private var shouldApplyNoise: Boolean = false
    private var shouldApplyVignette: Boolean = false
    private inner class ApplyEffectsTask( ) : AsyncTask<Void, Void, Bitmap>() {
        override fun doInBackground(vararg params: Void): Bitmap {
            val tempBitmap = bitmap!!.copy(Bitmap.Config.ARGB_8888, true)

            tone = max(0.1F, toneSeekBar!!.progress / 10F)
            saturation = max(1.0F, saturationSeekBar!!.progress / 10F)
            bright = max(0.1F, brightSeekBar!!.progress / 10F)
            exposition = max(0.1F, expositionSeekBar!!.progress / 10F)
            contrast = max(0.1F, contrastSeekBar!!.progress / 10F)
            blur = max(0.1F, blurSeekBar!!.progress / 1F)
            noise = max(0.1F, noiseSeekBar!!.progress / 10F)
            vignette = max(0.1F, vignetteSeekBar!!.progress / 10F)

            if (shouldApplyTone) {
                myTone(tempBitmap, tempBitmap, tone - 10F)
            }
            if (shouldApplySaturation) {
                mySaturation(tempBitmap, tempBitmap, saturation - 10F)
            }
            if (shouldApplyBright) {
                myBright(tempBitmap, tempBitmap, bright - 10F)
            }
            if (shouldApplyExposition) {
                myExposition(tempBitmap, tempBitmap, exposition - 10F)
            }
            if (shouldApplyContrast) {
                myContrast(tempBitmap, tempBitmap, contrast - 10F)
            }
            if (shouldApplyBlur) {
                myBlur(tempBitmap, tempBitmap, blur)
            }
            if (shouldApplyNoise){
                myNoise(tempBitmap, tempBitmap, noise)
            }
            if (shouldApplyVignette) {
                myVignette(tempBitmap, tempBitmap, vignette)
            }

            return tempBitmap
        }
        override fun onPostExecute(result: Bitmap) {
            dstBitmap = result
            editImageBinding.imagePreview.setImageBitmap(dstBitmap)
        }
    }
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

    }
    override fun onStartTrackingTouch(p0: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        when (seekBar) {
            toneSeekBar -> {
                editTextTone!!.setText((seekBar!!.progress - 100).toString())
                shouldApplyTone = true

            }
            saturationSeekBar -> {
                editTextSaturation!!.setText((seekBar!!.progress - 100).toString())
                shouldApplySaturation = true
            }
            brightSeekBar -> {
                editTextBright!!.setText((seekBar!!.progress - 100).toString())
                shouldApplyBright = true
            }
            expositionSeekBar -> {
                editTextExposition!!.setText((seekBar!!.progress - 100).toString())
                shouldApplyExposition = true
            }
            contrastSeekBar -> {
                editTextContrast!!.setText((seekBar!!.progress - 100).toString())
                shouldApplyContrast = true
            }
            blurSeekBar -> {
                editTextBlur!!.setText((seekBar!!.progress).toString())
                shouldApplyBlur = true
            }
            noiseSeekBar -> {
                editTextNoise!!.setText((seekBar!!.progress).toString())
                shouldApplyNoise = true
            }
            vignetteSeekBar -> {
                editTextVignette!!.setText((seekBar!!.progress).toString())
                shouldApplyVignette = true
            }
        }
        ApplyEffectsTask().execute()
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }
    override fun afterTextChanged(p0: Editable?) {
        p0?.let {
            val value = p0.toString().toIntOrNull() ?: 0
            when (p0) {
                editTextTone!!.text -> {
                    toneSeekBar!!.progress = value + 100
                    shouldApplyTone = true
                }
                editTextSaturation!!.text -> {
                    saturationSeekBar!!.progress = value + 100
                    shouldApplySaturation = true
                }
                editTextBright!!.text -> {
                    brightSeekBar!!.progress = value + 100
                    shouldApplyBright = true
                }
                editTextExposition!!.text -> {
                    expositionSeekBar!!.progress = value + 100
                    shouldApplyExposition = true
                }
                editTextContrast!!.text -> {
                    contrastSeekBar!!.progress = value + 100
                    shouldApplyContrast = true
                }
                editTextBlur!!.text -> {
                    blurSeekBar!!.progress = value
                    shouldApplyBlur = true
                }
                editTextNoise!!.text -> {
                    noiseSeekBar!!.progress = value
                    shouldApplyNoise = true
                }
                editTextVignette!!.text -> {
                    vignetteSeekBar!!.progress = value
                    shouldApplyVignette = true
                }
            }
            ApplyEffectsTask().execute()
        }
    }
    override fun onBackPressed() {
        if (screenStack.isNotEmpty()) {
            val currentScreen = screenStack.removeAt(screenStack.size - 1)
            when (currentScreen) {
                "crop" -> {
                    saveBtn!!.visibility = View.VISIBLE
                    toolsLayout!!.visibility=View.VISIBLE
                    rotation!!.setTextColor(ContextCompat.getColor(applicationContext, R.color.white))
                    cropping!!.setTextColor(ContextCompat.getColor(applicationContext, R.color.white))
                    cropTools!!.visibility = View.GONE
                    bitmap = editImageBinding.cropImageView.getCroppedImage()!!
                    editImageBinding.imagePreview.setImageBitmap(bitmap)
                    editImageBinding.cropImageView.clearImage()
                }
                "correction" -> {
                    handleTextViewClick(color!!)
                    linearLayoutVisible(colorLinearLayout!!)
                    saveBtn!!.visibility = View.VISIBLE
                    toolsLayout!!.visibility=View.VISIBLE
                    correctionTools!!.visibility = View.GONE
                }
            }
        } else {
            editImageBinding.imagePreview.setImageResource(0)
            finish()
        }
    }
}

