package com.lemedebug.skillreview

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.hardware.camera2.CameraCharacteristics
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.*
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.core.*
import androidx.camera.core.ImageCapture.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.lemedebug.skillreview.utilities.Constants.FILENAME_FORMAT
import com.lemedebug.skillreview.utilities.Constants.REQUEST_CODE_PERMISSIONS
import com.lemedebug.skillreview.utilities.Constants.REQUEST_CODE_UPLOAD_IMAGE
import com.lemedebug.skillreview.utilities.Constants.REQUIRED_PERMISSIONS
import com.lemedebug.skillreview.utilities.Constants.SELECTED_IMAGE
import com.lemedebug.skillreview.utilities.Constants.TAG
import com.lemedebug.skillreview.utilities.ImageViewModel
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

/**
 * @author Mansimran Singh
 * Skill Review Assessment - Code Corp.
 * Assignment 1 Coverage
 */

class MainActivity : AppCompatActivity() {
    // Initializers
    private var cameraCharacteristics : CameraCharacteristics? = null
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var imageCapture: ImageCapture? = null
    private var linearZoom = 0f
    private var cameraControl: CameraControl? = null
    private var cameraInfo: CameraInfo? = null
    private var defaultSize: Size = Size(3840,2160)
    private var imageAnalysis: ImageAnalysis = ImageAnalysis.Builder().apply {
        setImageQueueDepth(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
    }.build()
    private var bitmap:Bitmap? = null

    // Late Initializers
    private lateinit var cameraExecutor: ExecutorService

    /**
     * onCreate Method
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check viewModel for already clicked image
        val viewModel = ViewModelProvider(this).get(ImageViewModel::class.java)
        if (viewModel.image != null){
            bitmap = viewModel.image!!
        }

        // Remove the status bar at top
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        // Request permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        //set slider value to 0f
        sliderZoom.value = 0f

        // Set up the listener for take photo button
        btnCapture.setOnClickListener { takePhoto() }

        // Set up the listener for toggle flash button
        btnFlash.setOnClickListener { toggleFlash() }

        // Set up the listener for toggle camera button
        btnSwitchCam.setOnClickListener { toggleCamera() }

        // Set up the listener for cancel button
        btnCancel.setOnClickListener {
            cameraLayout.visibility = View.VISIBLE
            previewLayout.visibility = View.GONE
            val viewModel = ViewModelProvider(this).get(ImageViewModel::class.java)
            viewModel.image = null
            bitmap = null
            Log.e(TAG,viewModel.image.toString())
        }

        // Set up the listener for Save button
        btnSave.setOnClickListener { saveMediaToStorage() }

        // Set up the listener for choose pic to upload button
        btnChoosePicToUpload.setOnClickListener { openGalleryForImage() }

        // Set up the listener for toggle resolution button
        btnResolutionSettings.setOnClickListener { toggleResolution() }

        // Set up the listener for slider zoom
        sliderZoom.addOnChangeListener { _, value, _ ->
            cameraControl?.setLinearZoom(value)
        }

        // initialize thread for camera
        cameraExecutor = Executors.newSingleThreadExecutor()
    }


    /**
     * saveMediaToStorage()
     * This method saves the image to storage
     */
    private fun saveMediaToStorage() {
        // Generating a file name
        val filename = SimpleDateFormat(FILENAME_FORMAT, Locale.US
        ).format(System.currentTimeMillis()) + ".jpg"

        // Output stream
        var fos: OutputStream? = null

        // For devices running android >= Q
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // getting the contentResolver
            this.contentResolver?.also { resolver ->

                // Content resolver will process the content values
                val contentValues = ContentValues().apply {

                    // putting file information in content values
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                // Inserting the contentValues to
                // contentResolver and getting the Uri
                val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                // Opening an output stream with the Uri that we got
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            // These for devices running on android < Q
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }

        fos?.use {
            // Finally writing the bitmap to the output stream that we opened
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(this , "Saved Successfully" , Toast.LENGTH_SHORT).show()
        }

        // At the end UI visibility handling
        cameraLayout.visibility = View.VISIBLE
        previewLayout.visibility = View.GONE
        val viewModel = ViewModelProvider(this).get(ImageViewModel::class.java)
        viewModel.image = null
        bitmap = null
    }

    /**
     * toggleResolution()
     * This method is used to change resolution on preview
     */
    private fun toggleResolution() {
        //int variable to get the position in array
        var selectedResolution = 0

        //Get list of output sizes available
        val streamConfigurationMap = cameraCharacteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val outputSizes = streamConfigurationMap?.getOutputSizes(SurfaceHolder::class.java)

        //If output sizes list is not null
        if (outputSizes != null) {
            //Convert output sizes list to array
            val arr = ArrayList<String>()
            outputSizes.forEach { arr.add(it.toString()) }
            val outputArray = arr.toTypedArray()

            //Build a dialog to show all the available sizes for selection
            val builder = AlertDialog.Builder(this@MainActivity)
            builder.setIcon(android.R.drawable.ic_menu_help)
            builder.setTitle("Select Resolution")

            //Set the local int variable to selected position
            builder.setSingleChoiceItems(outputArray,-1) { _, which -> selectedResolution = which }

            //Do nothing on cancel
            builder.setNeutralButton("Cancel"){ _, _ -> }

            //Set the defaultSize to selected size and start the camera
            builder.setPositiveButton("Select"){ _, _ ->
                //Log.e(TAG,"Starting camera resolution: "+outputSizes[selectedResolution].toString())
                defaultSize = outputSizes[selectedResolution]
                startCamera()
            }

            // create the dialog and show it
            val dialog = builder.create()
            dialog.show()
        }
    }

    /**
     * takePhoto()
     * This method is used to capture an image as bitmap
     */
    private fun takePhoto() {
        //Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        //Take picture call with OnImageCapturedCallback on ImageCapture
        imageCapture.takePicture(ContextCompat.getMainExecutor(this), object :
            ImageCapture.OnImageCapturedCallback() {

            //onCaptureSuccess Set the bitmap value to captured image
            @SuppressLint("UnsafeOptInUsageError")
            override fun onCaptureSuccess(image: ImageProxy) {
                //get bitmap from previewView
                if (viewFinder.bitmap != null){
                    bitmap = viewFinder.bitmap!!
                }
                //close the image
                image.close()
                //handle UI Visibility
                cameraLayout.visibility = View.GONE
                previewLayout.visibility = View.VISIBLE
                //set bitmap to image
                previewImage.setImageBitmap(bitmap)
            }

            //onError print in log
            override fun onError(exc: ImageCaptureException) {
                Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
            }
        })
    }

    /**
     * startCamera()
     * This method starts the camera,
     * Binds lifecycle of camera to lifecycle of main activity,
     * Sets the preview with camera
     */
    @SuppressLint("RestrictedApi", "UnsafeOptInUsageError")
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            // Preview Setup
            val preview = Preview.Builder().apply {
                setTargetResolution(defaultSize)    // resolution size
            }
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }
            // set size of image
            imageCapture = Builder().apply {
                setTargetResolution(defaultSize)
            }
            .build()
            // set size of preview view
            viewFinderLayout.layoutParams = RelativeLayout.LayoutParams(defaultSize.width,defaultSize.height)
            tvCurrentResolution.text = "$defaultSize"   // update text view with size of preview

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()
                // Bind use cases to camera
               val camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture,imageAnalysis)
                cameraControl = camera.cameraControl
                cameraInfo = camera.cameraInfo
                cameraCharacteristics = Camera2CameraInfo.extractCameraCharacteristics(camera.cameraInfo)
            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    /**
     * allPermissionsGranted()
     * This method checks if all permissions are granted
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * onDestroy()
     * Overriding onDestroy to shutdown running camera thread at closure,
     * Keep unsaved clicked image in viewModel
     */
    override fun onDestroy() {
        super.onDestroy()
        // if camera executor thread is running, shut it down
        if (!cameraExecutor.isShutdown){
            cameraExecutor.shutdown()
        }
        // update viewModel if bitmap is not null
        val viewModel = ViewModelProvider(this).get(ImageViewModel::class.java)
        if (bitmap!=null){
            viewModel.image = bitmap
        }
    }

    /**
     * onResume()
     * Overriding onResume to load unsaved clicked image from viewModel
     */
    override fun onResume() {
        super.onResume()
        //show clicked image if bitmap is not null and activity is re-opened
        val viewModel = ViewModelProvider(this).get(ImageViewModel::class.java)
        if (viewModel.image != null){
            cameraLayout.visibility = View.GONE
            previewLayout.visibility = View.VISIBLE
            previewImage.setImageBitmap(viewModel.image)
        }
    }

    /**
     * onRequestPermissionsResult
     * If denied permissions close the app,
     * If allowed start the camera
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    /**
     * onKeyDown
     * Overriding it to manage camera Zoom with up and down keys
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (linearZoom <= 0.9) {
                    linearZoom += 0.1f
                }
                cameraControl?.setLinearZoom(linearZoom)
                sliderZoom.value = linearZoom
                true
            }            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (linearZoom >= 0.1) {
                    linearZoom -= 0.1f
                }
                cameraControl?.setLinearZoom(linearZoom)
                sliderZoom.value = linearZoom
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    /**
     * toggleFlash()
     * This method is used to toggle Flash Mode
     */
    private fun toggleFlash() {
        when(imageCapture?.flashMode){
            FLASH_MODE_OFF -> {
                imageCapture?.flashMode = FLASH_MODE_ON
                btnFlash.setImageResource(R.drawable.ic_baseline_flash_on_24)
            }
            FLASH_MODE_ON -> {
                imageCapture?.flashMode = FLASH_MODE_AUTO
                btnFlash.setImageResource(R.drawable.ic_baseline_flash_auto_24)
            }
            else -> {
                imageCapture?.flashMode = FLASH_MODE_OFF
                btnFlash.setImageResource(R.drawable.ic_baseline_flash_off_24)
            }
        }
    }


    /**
     * toggleCamera()
     * This method is used to toggle Camera (Front and Back only)
     */
    @SuppressLint("RestrictedApi")
    private fun toggleCamera(){
        cameraSelector = if (cameraSelector.lensFacing == CameraSelector.LENS_FACING_BACK){
            CameraSelector.DEFAULT_FRONT_CAMERA
        }else{
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        startCamera()
    }

    /**
     * openGalleryForImage()
     * This method is just to create intent to pick an image for uploading
     */
    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_UPLOAD_IMAGE)
    }

    /**
     * onActivityResult
     * overriding onActivityResult to get the result of picked image and launch upload activity
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_UPLOAD_IMAGE){
            val intent = Intent(this@MainActivity,UploadActivity::class.java)
            intent.putExtra(SELECTED_IMAGE,data?.data.toString())
            startActivity(intent)
        }
    }


}