package com.lemedebug.skillreview.utilities

import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File

interface UploadImageService {
    //Base = https://xxx.xxx.xxx/upload/
    @Multipart
    @POST(".")
    fun getUploadStatus(@Part("file") image: File): Call<UploadResponseData>
}