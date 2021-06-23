package com.lemedebug.skillreview.utilities

import android.Manifest

object Constants{
    const val TAG = "SkillReview"
    const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    const val REQUEST_CODE_PERMISSIONS = 10
    val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE)
    const val REQUEST_CODE_UPLOAD_IMAGE = 101
    const val SELECTED_IMAGE = "IMAGE_SELECTED"
}