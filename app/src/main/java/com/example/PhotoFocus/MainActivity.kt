package com.example.PhotoFocus

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import com.example.PhotoFocus.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    var srcBitmap: Bitmap? = null
    var dstBitmap: Bitmap? = null

    companion object {
        val IMAGE_REQUEST_CODE = 100
    }

    private lateinit var button: Button
    private lateinit var imageView: ImageView

    private lateinit var binding: ActivityMainBinding

    private fun pickImageFromGalley() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        getResult.launch(intent)
    }

    private val getResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        {
            if (it.resultCode == Activity.RESULT_OK) {
                imageView.setImageURI(it.data?.data)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        srcBitmap = BitmapFactory.decodeResource(this.resources, R.drawable.zag)
        dstBitmap = srcBitmap!!.copy(srcBitmap!!.config, true)

        binding.imageView.setImageBitmap(dstBitmap)

        button = findViewById(R.id.btnLoad)
        imageView = findViewById(R.id.imageView)

        binding.btnLoad.setOnClickListener{
                pickImageFromGalley();
        }

//        binding.sldSigma.setOnSeekBarChangeListener(this)

//        // Example of a call to a native method
//        binding.sampleText.text = stringFromJNI()
    }

    /*fun doBlur(){
        val sigma = max(0.1F, binding.sldSigma.progress / 10F)
        this.myBlur(srcBitmap!!, dstBitmap!!, sigma)
    }*/

    /*fun btnFlip_click(view: View){
        myFlip(srcBitmap!!, srcBitmap!!)
        doBlur()
    }*/

    /**
     * A native method that is implemented by the 'opencv2' native library,
     * which is packaged with this application.
     */
    /*external fun stringFromJNI(): String
    external fun myFlip(bitmapIn: Bitmap, bitmapOut: Bitmap)
    external fun myBlur(bitmapIn: Bitmap, bitmapOut: Bitmap, sigma: Float)*/

    /*override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
        doBlur()
    }

    override fun onStartTrackingTouch(p0: SeekBar?) {
    }

    override fun onStopTrackingTouch(p0: SeekBar?) {
    }*/
}