package com.example.PhotoFocus

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.*
import android.os.AsyncTask
import android.os.Bundle
import android.text.*
import android.view.GestureDetector
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.drawToBitmap
import com.example.PhotoFocus.databinding.EditImageBinding
import com.google.gson.annotations.SerializedName
import com.yandex.metrica.YandexMetrica
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Float.max

class EditImageActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener, TextWatcher {
    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }
    private var dstBitmap: Bitmap? = null

    private lateinit var editImageBinding: EditImageBinding

    private val retrofitService: RetrofitService = RetrofitService()

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
    private val screenStack = mutableListOf<String>()
    private var toolsLayout: HorizontalScrollView? = null
    private var saveBtn: Button? = null
    private var correctionTools: ConstraintLayout? = null
    private var cropTools: ConstraintLayout? = null
    private var textBtnsLayout: ConstraintLayout? = null
    private var templatesBtnsLayout: ConstraintLayout? = null
    private var color: TextView? = null
    private var cropping: TextView? = null
    private var rotation: TextView? = null
    private var colorText: TextView? = null
    private var sysTemplates: TextView? = null
    private var userTemplates: TextView? = null

    private var colorLinearLayout: LinearLayout? = null
    private var colorTextLayout: LinearLayout? = null
    private var fontsTextLayout: LinearLayout? = null
    private var templatesSysLayout: LinearLayout? = null

    private lateinit var imagePreview: ImageView
    private lateinit var editText: EditText

    var screen : String? = null
    var id : String? = null
    var nameChangeTemplates: String? = null

    private lateinit var editImageModel: EditImageModel
    private lateinit var textModel: TextModel
    private lateinit var editImageController: EditImageController

    private var tone: Float = 100.0F
    private var saturation: Float = 100.0F
    private var bright: Float = 100.0F
    private var exposition: Float = 100.0F
    private var contrast: Float = 100.0F
    private var blur: Float = 0.0F
    private var noise: Float = 0.0F
    private var vignette: Float = 0.0F
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editImageBinding = EditImageBinding.inflate(layoutInflater)
        supportActionBar?.hide()
        setContentView(editImageBinding.root)

        editImageModel = EditImageModel(this)

        val imagePath = intent.getStringExtra("path")

        imagePreview = findViewById(R.id.imagePreview)

        editImageModel.loadImage(imagePath.toString())

        textModel = TextModel()

        imagePreview.visibility = View.VISIBLE

        YandexMetrica.reportEvent(MetricEventNames.VISITED_EDIT_SCREEN)

        toolsLayout = findViewById(R.id.toolsLayout)

        val cropView = editImageBinding.cropImageView

        screen = intent.getStringExtra("screen")
        id = intent.getStringExtra("id")
        editImageBinding.cropBtn.setOnClickListener {
            YandexMetrica.reportEvent(MetricEventNames.STARTED_EDIT_IMAGE)
            screenStack.add("crop")
            crop()
            toolsLayout!!.visibility = View.GONE

        }
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

        setDefaultSeekBar()

        editImageBinding.correctionBtn.setOnClickListener {
            YandexMetrica.reportEvent(MetricEventNames.STARTED_EDIT_IMAGE)
            screenStack.add("correction")
            toolsLayout!!.visibility = View.GONE
            correction()
        }

        val autocorrectionBtn = findViewById<TextView>(R.id.autocorrectionBtn)

        if (screen == "guest") {
            val iconDrawable = ContextCompat.getDrawable(this, R.drawable.ic_hexagon)

            val color = ContextCompat.getColor(this, R.color.light_gray)
            val wrappedDrawable = DrawableCompat.wrap(iconDrawable!!.mutate())
            DrawableCompat.setTint(wrappedDrawable, color)
            autocorrectionBtn.setTextColor(ContextCompat.getColor(applicationContext, R.color.light_gray))
            editImageBinding.templatesBtn.setTextColor(ContextCompat.getColor(applicationContext, R.color.light_gray))

            autocorrectionBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(null, wrappedDrawable, null, null)
            editImageBinding.templatesBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(null, wrappedDrawable, null, null)
            autocorrectionBtn.setOnClickListener {
                showAuthorizationDialog()
            }
            editImageBinding.templatesBtn.setOnClickListener {
                showAuthorizationDialog()
            }
        } else {
            autocorrectionBtn.setOnClickListener {
                YandexMetrica.reportEvent(MetricEventNames.APPLY_AUTOCORR)
                editImageController.onAutocorrectClicked()
            }
            editImageBinding.templatesBtn.setOnClickListener {
                YandexMetrica.reportEvent(MetricEventNames.APPLY_TEMPLATES)
                screenStack.add("templates")
                toolsLayout!!.visibility = View.GONE
                templates()
            }
        }

        editText = findViewById(R.id.editText)
        editImageBinding.textBtn.setOnClickListener {
            YandexMetrica.reportEvent(MetricEventNames.STARTED_EDIT_IMAGE)
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

        saveBtn = findViewById(R.id.saveBtn)

        editImageController = EditImageController(this, editImageModel)

        saveBtn?.setOnClickListener {
            val combinedBitmap = textModel.combineImageAndText(dstBitmap!!, imagePreview, editText)
            editImageController.onSaveButtonClicked(combinedBitmap!!)
        }
    }

    class Templates(
        @SerializedName("templates")
        var list: List<Template>
    )
    class Template (
        val name:       String,

        @SerializedName("user_id")
        val userId:     Int,
        val tone:       Float,
        val saturation: Float,
        val bright:     Float,
        val exposition: Float,
        val contrast:   Float,
        val blur:       Float,
        val noise:      Float,
        val vignette:   Float
    )

    var viewTemplates: MutableList<ImageView>? = null
  @SuppressLint("SuspiciousIndentation")
  private fun templates() {
        saveBtn!!.visibility=View.GONE
        templatesBtnsLayout = findViewById(R.id.templatesBtnsLayout)
        templatesBtnsLayout!!.visibility=View.VISIBLE

        sysTemplates = findViewById(R.id.sysTemplates)
        userTemplates = findViewById(R.id.userTemplates)
        templatesSysLayout = findViewById(R.id.templatesSysLayout)
        val templatesUserLayout = findViewById<LinearLayout>(R.id.templatesUserLayout)

        viewTemplates = mutableListOf()

          CoroutineScope(Dispatchers.IO).launch {
              val response: Response<Templates> = retrofitService.retrofit.getTemplate(id.toString())
              val templates = response.body()?.list
              withContext(Dispatchers.Main) {
                  if (response.isSuccessful && templates != null) {
                      for(x in templates.indices) {
                          val viewTemplateId = resources.getIdentifier("userTemplates_${x+1}", "id", packageName)
                          viewTemplates!!.add(findViewById(viewTemplateId))

                          setTextToSmallImageView(viewTemplates!![x], ResourcesCompat.getFont(this@EditImageActivity, R.font.nevduplenysh_regular), templates[x].name)
                          viewTemplates!![x].setOnClickListener {
                              nameChangeTemplates = templates[x].name
                              setDefaultSeekBar()
                              toneSeekBar?.progress = templates[x].tone.toInt()
                              editTextTone?.setText((templates[x].tone.toInt()).toString())
                              saturationSeekBar?.progress = templates[x].saturation.toInt()
                              editTextSaturation?.setText((templates[x].saturation.toInt()).toString())
                              brightSeekBar?.progress = templates[x].bright.toInt()
                              editTextBright?.setText((templates[x].bright.toInt()).toString())
                              expositionSeekBar?.progress = templates[x].exposition.toInt()
                              editTextExposition?.setText((templates[x].exposition.toInt()).toString())
                              contrastSeekBar?.progress = templates[x].contrast.toInt()
                              editTextContrast?.setText((templates[x].contrast.toInt()).toString())
                              blurSeekBar?.progress = (templates[x].blur).toInt()
                              editTextBlur?.setText(templates[x].blur.toInt().toString())
                              noiseSeekBar?.progress = (templates[x].noise).toInt()
                              editTextNoise?.setText(templates[x].noise.toInt().toString())
                              vignetteSeekBar?.progress = (templates[x].vignette).toInt()
                              editTextVignette?.setText((templates[x].vignette).toInt().toString())
                              updateCorrectionParametrs()
                              ApplyEffectsTask(tone, saturation, bright, exposition, contrast, blur,  noise, vignette).execute()
                          }
                          viewTemplates!![x].setOnLongClickListener {
                              showDeleteDialog(templates[x].name, x)
                              true
                          }
                      }
                  } else {
                      viewTemplates!!.add(findViewById(R.id.userTemplates_1))
                      viewTemplates!!.add(findViewById(R.id.userTemplates_2))
                      viewTemplates!!.add(findViewById(R.id.userTemplates_3))
                      viewTemplates!!.add(findViewById(R.id.userTemplates_4))
                      viewTemplates!!.add(findViewById(R.id.userTemplates_5))
                      viewTemplates!!.add(findViewById(R.id.userTemplates_6))
                  }
              }
          }

        val sysTemplates_1 = findViewById<ImageView>(R.id.sysTemplates_1)
        val sysTemplates_2 = findViewById<ImageView>(R.id.sysTemplates_2)
        val sysTemplates_3 = findViewById<ImageView>(R.id.sysTemplates_3)

        handleTextViewClick(sysTemplates!!)
        linearLayoutVisible(templatesSysLayout!!)

        sysTemplates!!.setOnClickListener {
            handleTextViewClick(sysTemplates!!)
            linearLayoutVisible(templatesSysLayout!!)
        }
        userTemplates!!.setOnClickListener {
            handleTextViewClick(userTemplates!!)
            linearLayoutVisible(templatesUserLayout!!)
            Toast.makeText(this, "Для удаления нажмите и удерживайте нужный шаблон", Toast.LENGTH_SHORT).show()
        }
        setTextToSmallImageView(sysTemplates_1, ResourcesCompat.getFont(this, R.font.nevduplenysh_regular), "Оригинал")
        setTextToSmallImageView(sysTemplates_2, ResourcesCompat.getFont(this, R.font.nevduplenysh_regular), "Ч/Б")
        setTextToSmallImageView(sysTemplates_3, ResourcesCompat.getFont(this, R.font.nevduplenysh_regular), "Контраст")
        sysTemplates_1.setOnClickListener {
            setDefaultSeekBar()
            updateCorrectionParametrs()
            ApplyEffectsTask(tone, saturation, bright, exposition, contrast, blur,  noise, vignette).execute()
        }
        sysTemplates_2.setOnClickListener {
            setDefaultSeekBar()
            saturationSeekBar?.progress = 0
            editTextSaturation?.setText((-100).toString())
            brightSeekBar?.progress = 0
            editTextBright?.setText((-100).toString())
            updateCorrectionParametrs()
            ApplyEffectsTask(tone, saturation, bright, exposition, contrast, blur,  noise, vignette).execute()
        }
        sysTemplates_3.setOnClickListener {
            setDefaultSeekBar()
            brightSeekBar?.progress = 20
            editTextBright?.setText((-80).toString())
            contrastSeekBar?.progress = 130
            editTextContrast?.setText((30).toString())
            updateCorrectionParametrs()
            ApplyEffectsTask(tone, saturation, bright, exposition, contrast, blur,  noise, vignette).execute()
        }
        backBtn?.setOnClickListener{
            onBackPressed()
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun myText() {
        saveBtn!!.visibility=View.GONE
        textBtnsLayout = findViewById(R.id.textBtnsLayout)
        textBtnsLayout!!.visibility=View.VISIBLE
        editText.visibility= View.VISIBLE

        val fonts = findViewById<TextView>(R.id.fonts)
        colorText = findViewById(R.id.colorText)
        fontsTextLayout = findViewById(R.id.fontsTextLayout)
        colorTextLayout = findViewById(R.id.colorLinearLayout)
        val colorToolsHSV = findViewById<HorizontalScrollView>(R.id.colorTextHSV)

        val textColorWhite = findViewById<ImageView>(R.id.textColorWhite)
        val textColorBlack = findViewById<ImageView>(R.id.textColorBlack)
        val textColorRed = findViewById<ImageView>(R.id.textColorRed)
        val textColorGreen = findViewById<ImageView>(R.id.textColorGreen)
        val textColorBlue = findViewById<ImageView>(R.id.textColorBlue)
        val textColorPurple = findViewById<ImageView>(R.id.textColorPurple)

        val textFont_1 = findViewById<ImageView>(R.id.textFont_1)
        val textFont_2 = findViewById<ImageView>(R.id.textFont_2)
        val textFont_3 = findViewById<ImageView>(R.id.textFont_3)

        handleTextViewClick(colorText!!)
        linearLayoutVisible(colorTextLayout!!)

        editText.setOnTouchListener { _, _ ->
            editText.requestFocus()
            false
        }
        imagePreview.setOnTouchListener { _, motionEvent ->
            textModel.handleTextTouch(editText, imagePreview, motionEvent)
        }

        colorText!!.setOnClickListener {
            handleTextViewClick(colorText!!)
            linearLayoutVisible(colorTextLayout!!)
            colorToolsHSV.visibility=View.VISIBLE
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

        val typeface_1 = ResourcesCompat.getFont(this, R.font.nevduplenysh_regular)
        setTextToSmallImageView(textFont_1, typeface_1, "Abcd")
        val typeface_2 = ResourcesCompat.getFont(this, R.font.srbija)
        setTextToSmallImageView(textFont_2, typeface_2, "Abcd")
        val typeface_3 = ResourcesCompat.getFont(this, R.font.mplus)
        setTextToSmallImageView(textFont_3, typeface_3, "Abcd")
        fonts.setOnClickListener {
            handleTextViewClick(fonts)
            linearLayoutVisible(fontsTextLayout!!)
            colorTextLayout!!.visibility=View.GONE
            colorTextLayout!!.visibility = View.GONE
            colorToolsHSV.visibility=View.GONE
        }
        textFont_1.setOnClickListener {
            editText.typeface = typeface_1
        }
        textFont_2.setOnClickListener {
            editText.typeface = typeface_2
        }
        textFont_3.setOnClickListener {
            editText.typeface = typeface_3
        }
        backBtn?.setOnClickListener{
            onBackPressed()
        }
    }
    private fun setTextToSmallImageView(image: ImageView, typeface: Typeface?, text: String) { //text  не больше 8 символов
        val bitmapFont = Bitmap.createBitmap(65, 65, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmapFont)

        val paint = Paint()
        paint.typeface = typeface
        paint.textSize = 25f
        paint.color = Color.BLACK

        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)

        val textWidth = bounds.width()
        val textHeight = bounds.height()

        val x = (bitmapFont.width - textWidth) / 2f
        val y = (bitmapFont.height + textHeight) / 2f

        val textLines = text.split("\n")
        var yOffset = 0f

        for (line in textLines) {
            canvas.drawText(line, x, y + yOffset, paint)
            yOffset += textHeight
        }

        image.setImageBitmap(bitmapFont)
    }

    private fun crop() {
        cropTools = findViewById(R.id.cropBtnsLayout)
        cropping = findViewById(R.id.cropping)

        val fixcropping = findViewById<TextView>(R.id.fixcropping)
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
        editImageBinding.cropImageView.setImageBitmap(dstBitmap)

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
        backBtn?.setOnClickListener{
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

        selectedLinearLayout = colorLinearLayout
        handleTextViewClick(color!!)
        linearLayoutVisible(colorLinearLayout!!)
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

        backBtn?.setOnClickListener{
            onBackPressed()
        }
    }
    private fun setDefaultSeekBar() {
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
    }
    private fun updateCorrectionParametrs(){
        tone = max(0.0F, toneSeekBar!!.progress / 1F)
        saturation = max(0.0F, saturationSeekBar!!.progress / 1F)
        bright = max(0.0F, brightSeekBar!!.progress / 1F)
        exposition = max(0.0F, expositionSeekBar!!.progress / 1F)
        contrast = max(0.0F, contrastSeekBar!!.progress / 1F)
        blur = max(0.0F, blurSeekBar!!.progress / 1F)
        noise = max(0.0F, noiseSeekBar!!.progress / 1F)
        vignette = max(0.0F, vignetteSeekBar!!.progress / 1F)
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

    @SuppressLint("StaticFieldLeak")
    private inner class ApplyEffectsTask(
        private val tone: Float,
        private val saturation: Float,
        private val bright: Float,
        private val exposition: Float,
        private val contrast: Float,
        private val blur: Float,
        private val noise: Float,
        private val vignette: Float
    ) : AsyncTask<Void, Void, Bitmap>() {
        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg params: Void): Bitmap {
            return editImageModel.applyEffects(
                tone,
                saturation,
                bright,
                exposition,
                contrast,
                blur,
                noise,
                vignette
            )
        }
        @Deprecated("Deprecated in Java")
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
            }
            saturationSeekBar -> {
                editTextSaturation!!.setText((seekBar!!.progress - 100).toString())
            }
            brightSeekBar -> {
                editTextBright!!.setText((seekBar!!.progress - 100).toString())
            }
            expositionSeekBar -> {
                editTextExposition!!.setText((seekBar!!.progress - 100).toString())
            }
            contrastSeekBar -> {
                editTextContrast!!.setText((seekBar!!.progress - 100).toString())
            }
            blurSeekBar -> {
                editTextBlur!!.setText((seekBar!!.progress).toString())
            }
            noiseSeekBar -> {
                editTextNoise!!.setText((seekBar!!.progress).toString())
            }
            vignetteSeekBar -> {
                editTextVignette!!.setText((seekBar!!.progress).toString())
            }
        }
        updateCorrectionParametrs()
        ApplyEffectsTask(tone, saturation, bright, exposition, contrast, blur,  noise, vignette).execute()
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
                }
                editTextSaturation!!.text -> {
                    saturationSeekBar!!.progress = value + 100
                }
                editTextBright!!.text -> {
                    brightSeekBar!!.progress = value + 100
                }
                editTextExposition!!.text -> {
                    expositionSeekBar!!.progress = value + 100
                }
                editTextContrast!!.text -> {
                    contrastSeekBar!!.progress = value + 100
                }
                editTextBlur!!.text -> {
                    blurSeekBar!!.progress = value
                }
                editTextNoise!!.text -> {
                    noiseSeekBar!!.progress = value
                }
                editTextVignette!!.text -> {
                    vignetteSeekBar!!.progress = value
                }
            }
            updateCorrectionParametrs()
            ApplyEffectsTask(tone, saturation, bright, exposition, contrast, blur,  noise, vignette).execute()
        }
    }
    private fun showExitConfirmationDialog() {
        val dialogView = layoutInflater.inflate(R.layout.back_dialog, null)
        val alertDialogBuilder = AlertDialog.Builder(this, R.style.DialogStyle)
        alertDialogBuilder.setView(dialogView)

        val dialog = alertDialogBuilder.create()

        val saveBtn = dialogView.findViewById<Button>(R.id.dialog_saveBtn)
        val dontSaveBtn = dialogView.findViewById<Button>(R.id.dialog_dontSaveBtn)

        saveBtn.setOnClickListener {
            //dstBitmap = scaleBitmap(dstBitmap!!, originalWidth, originalHeight)
            val combinedBitmap = textModel.combineImageAndText(dstBitmap!!, imagePreview, editText)
            //combinedBitmap = scaleBitmap(combinedBitmap!!, originalWidth, originalHeight)
            editImageController.onSaveButtonClicked(combinedBitmap!!)
            val intent = Intent(this, GalleryActivity::class.java)
            startActivity(intent)
            dialog.dismiss()
            finish()
        }

        dontSaveBtn.setOnClickListener {
            dialog.dismiss()
            finish()
        }
        dialog.show()
    }

    private fun showAuthorizationDialog() {
        val dialogView = layoutInflater.inflate(R.layout.authorization_dialog, null)
        val alertDialogBuilder = AlertDialog.Builder(this, R.style.DialogStyle)
        alertDialogBuilder.setView(dialogView)

        val dialog = alertDialogBuilder.create()

        val authorization = dialogView.findViewById<Button>(R.id.btn_authorization)
        val later = dialogView.findViewById<Button>(R.id.btn_later)

        authorization.setOnClickListener {
            val intent = Intent(this, Authorization::class.java)
            startActivity(intent)
            dialog.dismiss()
            finish()
        }

        later.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    fun deleteTemplate(name: String, id: Int){
        val jsonObject = JSONObject()
        jsonObject.put("name", name)
        jsonObject.put("user_id", id)

        val body = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            jsonObject.toString()
        )
        retrofitService.retrofit.deleteTemplate(body).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.code() == 201) {
                    Toast.makeText(this@EditImageActivity,
                        "Шаблон удален",
                        Toast.LENGTH_SHORT).show();
                    return
                }
                if (response.code() == 502) {
                    Toast.makeText(this@EditImageActivity,
                        "Ошибка сервера, попробуйте позже",
                        Toast.LENGTH_SHORT).show();
                    return
                }
            }
            override fun onFailure(call: Call<String>, t: Throwable) {}
        })
    }

    private fun showDeleteDialog(name: String, x: Int) {
        val dialogView = layoutInflater.inflate(R.layout.delete_dialog, null)
        val alertDialogBuilder = AlertDialog.Builder(this, R.style.DialogStyle)
        alertDialogBuilder.setView(dialogView)

        val dialog = alertDialogBuilder.create()

        val yes = dialogView.findViewById<Button>(R.id.dialog_yes)
        val no = dialogView.findViewById<Button>(R.id.dialog_no)

        yes.setOnClickListener {
            deleteTemplate(name, id!!.toInt())
            for (i in x until viewTemplates!!.size - 1) {
                viewTemplates!![i].setImageBitmap(viewTemplates!![i + 1].drawToBitmap())
            }
            viewTemplates!![viewTemplates!!.size - 1].setImageResource(0)
            dialog.dismiss()
        }

        no.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    @Deprecated("Deprecated in Java")
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
                    dstBitmap = editImageBinding.cropImageView.getCroppedImage()!!
                    editImageBinding.imagePreview.setImageBitmap(dstBitmap)
                    editImageModel.bitmap = editImageBinding.cropImageView.getCroppedImage()!!
                    editImageModel.dstBitmap = editImageBinding.cropImageView.getCroppedImage()!!
                    editImageBinding.cropImageView.clearImage()
                }
                "correction" -> {
                    handleTextViewClick(color!!)
                    linearLayoutVisible(colorLinearLayout!!)
                    saveBtn!!.visibility = View.VISIBLE
                    toolsLayout!!.visibility=View.VISIBLE
                    correctionTools!!.visibility = View.GONE
                }
                "text" -> {
                    handleTextViewClick(colorText!!)
                    linearLayoutVisible(colorTextLayout!!)
                    saveBtn!!.visibility = View.VISIBLE
                    toolsLayout!!.visibility=View.VISIBLE
                    textBtnsLayout!!.visibility = View.GONE
                    if (editText.text.toString().isEmpty())
                        editText.visibility = View.GONE
                    editText.clearFocus()
                }
                "templates" -> {
                    handleTextViewClick(sysTemplates!!)
                    linearLayoutVisible(templatesSysLayout!!)
                    saveBtn!!.visibility = View.VISIBLE
                    toolsLayout!!.visibility=View.VISIBLE
                    templatesBtnsLayout!!.visibility = View.GONE
                }
            }
        } else {
            showExitConfirmationDialog()
        }
    }
    fun navigateToGallery() {
        val intent: Intent?
        if (screen == "authorization") {
            intent = Intent(this, UserTemplates::class.java)
            val bundle = Bundle()
            bundle.putString("id", id)
            bundle.putFloat("tone", tone-100)
            bundle.putFloat("saturation", saturation-100)
            bundle.putFloat("bright", bright-100)
            bundle.putFloat("exposition", exposition-100)
            bundle.putFloat("contrast", contrast-100)
            bundle.putFloat("blur", blur)
            bundle.putFloat("noise", noise)
            bundle.putFloat("vignette", vignette)
            bundle.putString("nameChangeTemplates", nameChangeTemplates)
            intent.putExtras(bundle)
        } else {
            intent = Intent(this, GalleryActivity::class.java)
            intent.putExtra("screen", screen)
        }
        startActivity(intent)
    }
    fun showMessage(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }
    fun showImage(bitmap: Bitmap) {
        imagePreview.setImageBitmap(bitmap)
        dstBitmap =  bitmap.copy(Bitmap.Config.ARGB_8888, true)
    }

}

