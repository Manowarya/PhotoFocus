package com.example.PhotoFocus

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.PhotoFocus.databinding.EditImageBinding
import java.lang.Float.max
import kotlin.concurrent.thread

class EditImageActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener {
    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }
    var bitmap: Bitmap? = null
    var dstBitmap: Bitmap? = null

    private lateinit var editImageBinding: EditImageBinding

    private var selectedTextView: TextView? = null
    private var selectedLinearLayout: LinearLayout? = null

    private var toneSeekBar: SeekBar? = null
    private var saturationSeekBar: SeekBar? = null
    private var brightSeekBar: SeekBar? = null
    private var expositionSeekBar: SeekBar? = null
    private var blurSeekBar: SeekBar? = null
    private var noiseSeekBar: SeekBar? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editImageBinding = EditImageBinding.inflate(layoutInflater)
        supportActionBar?.hide()
        setContentView(editImageBinding.root)

        val extras = intent.extras ?: return
        val uriString = extras.getString(MainActivity.KEY_IMAGE_URI)
        val view = findViewById<ImageView>(R.id.imagePreview)
        view.setImageURI(Uri.parse(uriString))

        val toolsLayout = findViewById<HorizontalScrollView>(R.id.toolsLayout)
        editImageBinding.cropBtn.setOnClickListener {
            toolsLayout.visibility=View.GONE
            crop(toolsLayout)
        }

        view.visibility = View.VISIBLE

        bitmap = (view.drawable as BitmapDrawable).bitmap

        dstBitmap = bitmap!!.copy(bitmap!!.config, true)

        editImageBinding.correctionBtn.setOnClickListener {
            toolsLayout.visibility=View.GONE
            correction()
        }
    }

    private fun crop(toolsLayout: HorizontalScrollView) {
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

        saveBtn.visibility = View.GONE
        cropTools.visibility = View.VISIBLE
        editImageBinding.cropImageView.setImageBitmap(bitmap)
        editImageBinding.imagePreview.setImageResource(0)

        selectedLinearLayout = fixCropLayout
        handleTextViewClick(fixcropping)

        fixcropping.setOnClickListener {
            handleTextViewClick(fixcropping)
            linearLayoutVisible(fixCropLayout)
            cropOriginal.setOnClickListener {
                editImageBinding.cropImageView.setFixedAspectRatio(false)
            }
            crop1_1.setOnClickListener {
                editImageBinding.cropImageView.setAspectRatio(1, 1)
            }
            crop1_2.setOnClickListener {
                editImageBinding.cropImageView.setAspectRatio(1, 2)
            }
            crop16_9.setOnClickListener {
                editImageBinding.cropImageView.setAspectRatio(16, 9)
            }
            crop4_3.setOnClickListener {
                editImageBinding.cropImageView.setAspectRatio(4, 3)
            }
            crop3_1.setOnClickListener {
                editImageBinding.cropImageView.setAspectRatio(3, 1)
            }
        }

        cropping.setOnClickListener {
            handleTextViewClick(cropping)
            editImageBinding.cropImageView.setFixedAspectRatio(false)
            fixCropLayout.visibility = View.GONE
            rotationLayout.visibility = View.GONE
        }
        rotation.setOnClickListener {
            handleTextViewClick(rotation)
            linearLayoutVisible(rotationLayout)
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
            toolsLayout.visibility=View.VISIBLE
            rotation.setTextColor(resources.getColor(R.color.white))
            cropping.setTextColor(resources.getColor(R.color.white))
            cropTools.visibility = View.GONE
            bitmap = editImageBinding.cropImageView.getCroppedImage()!!
            editImageBinding.imagePreview.setImageBitmap(bitmap)
            editImageBinding.cropImageView.clearImage()
        }
    }

    private fun correction() {
        val correctionTools = findViewById<ConstraintLayout>(R.id.correctionBtnsLayout)
        val colorLinearLayout = findViewById<LinearLayout>(R.id.colorLinearLayout)
        val lightLinearLayout = findViewById<LinearLayout>(R.id.lightLinearLayout)
        val blurLinearLayout = findViewById<LinearLayout>(R.id.blurLinearLayout)
        val noiseLinearLayout = findViewById<LinearLayout>(R.id.noiseLinearLayout)
        val vignetteLinearLayout = findViewById<LinearLayout>(R.id.vignetteLinearLayout)

        val color = findViewById<TextView>(R.id.color)
        val light = findViewById<TextView>(R.id.light)
        val noise = findViewById<TextView>(R.id.noise)
        val blur = findViewById<TextView>(R.id.blur)
        val vignette = findViewById<TextView>(R.id.vignette)

        toneSeekBar = findViewById(R.id.toneSeekBar)
        saturationSeekBar = findViewById(R.id.saturationSeekBar)
        brightSeekBar = findViewById(R.id.brightSeekBar)
        expositionSeekBar = findViewById(R.id.expositionSeekBar)
        noiseSeekBar = findViewById(R.id.noiseSeekBar)
        blurSeekBar = findViewById(R.id.blurSeekBar)

        brightSeekBar!!.setOnSeekBarChangeListener(this)
        saturationSeekBar!!.setOnSeekBarChangeListener(this)
        toneSeekBar!!.setOnSeekBarChangeListener(this)
        expositionSeekBar!!.setOnSeekBarChangeListener(this)
        expositionSeekBar!!.setMax(20)
        expositionSeekBar!!.setProgress(10)
        blurSeekBar!!.setOnSeekBarChangeListener(this)
        noiseSeekBar!!.setOnSeekBarChangeListener(this)

        selectedLinearLayout = colorLinearLayout
        handleTextViewClick(color)

        color.setOnClickListener{
            handleTextViewClick(color)
            linearLayoutVisible(colorLinearLayout)

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

        correctionTools.visibility = View.VISIBLE

    }
    private fun handleTextViewClick(textView: TextView) {
        selectedTextView?.setTextColor(resources.getColor(R.color.white))
        textView.setTextColor(resources.getColor(R.color.button))
        selectedTextView = textView
    }
    private fun linearLayoutVisible(linearLayout: LinearLayout) {
        selectedLinearLayout?.visibility=View.GONE
        linearLayout.visibility=View.VISIBLE
        selectedLinearLayout = linearLayout
    }

    external fun myBlur(bitmapIn: Bitmap, bitmapOut: Bitmap, sigma: Float)
    external fun myNoise(bitmapIn: Bitmap, bitmapOut: Bitmap, sigma: Float)
    external fun myTone(bitmapIn: Bitmap, bitmapOut: Bitmap, sigma: Float)
    external fun myExposition(bitmapIn: Bitmap, bitmapOut: Bitmap, sigma: Float)
    external fun myBright(bitmapIn: Bitmap, bitmapOut: Bitmap, sigma: Float)
    external fun mySaturation(bitmapIn: Bitmap, bitmapOut: Bitmap, sigma: Float)

    private var tone: Float = 0.0F
    private var saturation: Float = 1.0F
    private var bright: Float = 0.0F
    private var exposition: Float = 0.0F
    private var blur: Float = 0.0F
    private var noise: Float = 0.0F
    fun applyEffects() {
        val tempBitmap = bitmap!!.copy(Bitmap.Config.ARGB_8888, true)
        tone = max(0.1F, toneSeekBar!!.progress / 10F)
        saturation = max(1.0F, saturationSeekBar!!.progress / 10F)
        bright = max(0.1F, brightSeekBar!!.progress / 10F)
        exposition = max(0.1F, expositionSeekBar!!.progress / 10F)
        blur = max(0.1F, blurSeekBar!!.progress / 10F)
        noise =  max(0.1F, noiseSeekBar!!.progress / 10F)
        myBright(tempBitmap, tempBitmap, bright)
        mySaturation(tempBitmap, tempBitmap, saturation)
        myExposition(tempBitmap, tempBitmap, exposition)
        myBlur(tempBitmap, tempBitmap, blur)
        myNoise(tempBitmap, tempBitmap, noise)
        myTone(tempBitmap, tempBitmap, tone)

        dstBitmap = tempBitmap

        editImageBinding.imagePreview.setImageBitmap(dstBitmap)
    }
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
    }
    override fun onStartTrackingTouch(p0: SeekBar?) {}

    override fun onStopTrackingTouch(p0: SeekBar?) {
        thread {
            applyEffects()
        }
    }
}

