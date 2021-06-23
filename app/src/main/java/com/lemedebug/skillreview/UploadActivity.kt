package com.lemedebug.skillreview

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.lemedebug.skillreview.utilities.Constants
import com.lemedebug.skillreview.utilities.Constants.SELECTED_IMAGE
import com.lemedebug.skillreview.utilities.Constants.TAG
import com.lemedebug.skillreview.utilities.UploadImageService
import com.lemedebug.skillreview.utilities.UploadResponseData
import com.lemedebug.skillreview.utilities.Utils
import kotlinx.android.synthetic.main.activity_upload.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

/**
 * @author Mansimran Singh
 * Skill Review Assessment - Code Corp.
 * Assignment 2 Coverage
 */
class UploadActivity : Utils() {

    //Initializers
    private var imageData: Uri? = null

    /**
     * onCreate Method
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        //Set up action bar
        setupActionBar()

        //Get selected image from intent
        imageData = Uri.parse(intent.getStringExtra(SELECTED_IMAGE))
        if (imageData!=null){
            ivSelectedImage.setImageURI(imageData)
        }

        // Set up the listener for upload button
        btnUpload.setOnClickListener { retrofitCall(imageData.toString()) }

        // Set up the listener for select image button
        btnSelectImage.setOnClickListener { openGalleryForImage() }
    }

    /**
     * setupActionBar()
     * A function for actionBar Setup.
     */
    private fun setupActionBar() {
        setSupportActionBar(toolbarUpload)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) //set back button
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)
        supportActionBar?.title = "UPLOAD IMAGE" // Setting an title in the action bar.
        toolbarUpload.setNavigationOnClickListener { onBackPressed() }
    }

    /**
     * retrofitCall()
     * This method is used to execute the post method via retrofit and,
     * to visualize the success or failure on response
     */
    private fun retrofitCall(imageData:String){
        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        //Retrofit builder
        val baseUrl = "https://xxx.xxx.xxx/upload/"
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())     //using GsonConverter
            .build()
        //Get UploadImageService Interface
        val uploadImageAPI = retrofit.create(UploadImageService::class.java)
        //Execute the post method and callback on response data
        uploadImageAPI.getUploadStatus(File(imageData)).enqueue(object : Callback<UploadResponseData>{
            //onResponse of callback
            override fun onResponse( call: Call<UploadResponseData>, response: Response<UploadResponseData>) {
                //Log.d(TAG,"OnResponse: $response")
                hideProgressDialog()    // Hide Progress Bar
                //Show SnackBar on success or failure or invalid response
                when(response.code()){
                    200 -> { showErrorSnackBar("SUCCESS",false) }
                    404 -> { showErrorSnackBar("FAILED", true) }
                    else -> { showErrorSnackBar("Invalid Response Found", true) }
                }
                return
            }
            //onFailure print the exception
            override fun onFailure(call: Call<UploadResponseData>, t: Throwable) {
                hideProgressDialog()
                Log.d(TAG,"OnFailure: $t")
            }
        })
    }

    /**
     * openGalleryForImage()
     * This method is just to create intent to pick an image for uploading
     */
    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, Constants.REQUEST_CODE_UPLOAD_IMAGE)
    }

    /**
     * onActivityResult
     * overriding onActivityResult to get the result of picked image update UI
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.REQUEST_CODE_UPLOAD_IMAGE){
            if (data != null){
                imageData = data.data
            }
            ivSelectedImage.setImageURI(imageData)
        }
    }

}