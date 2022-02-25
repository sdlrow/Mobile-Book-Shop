package com.example.loginformapp.retrofit


import com.example.loginformapp.model.MyResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.Call

interface IreCAPTCHA {
    @FormUrlEncoded
    @POST("google_recaptcha.php")
    fun validate(@Field("recaptcha-response") response: String): Call<MyResponse>

}