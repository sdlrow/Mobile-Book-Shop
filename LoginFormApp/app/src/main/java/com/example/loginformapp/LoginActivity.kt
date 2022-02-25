package com.example.loginformapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.loginformapp.model.MyResponse
import com.example.loginformapp.retrofit.IreCAPTCHA
import com.example.loginformapp.retrofit.RetrofitClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.safetynet.SafetyNet
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_login.*
import maes.tech.intentanim.CustomIntent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginActivity : AppCompatActivity() {

    companion object{
        private val SAFETY_NET_SITE_KEY = "6LckzH4eAAAAAJKu2Dk0sSb3cAmThUHOH28niCvP"

    }

    private val api: IreCAPTCHA
        get() = RetrofitClient.getClient("http://10.0.2.2/server_validate/").create(IreCAPTCHA::class.java);
    lateinit var myService:IreCAPTCHA
    private lateinit var database: DatabaseReference
    private var reCaptchaStatus: Boolean = false
    val PREFS_NAME = "MyPrefsFile"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        var reCaptchaStatusRegister: Boolean = false
        val message = intent.getSerializableExtra("key").toString()
        if(message != "null"){
            var messageN = message.drop(1).dropLast(1)
            val filtered = " "
            val stringArray = messageN.split (",")
            Log.d("WER",messageN)
            reCaptchaStatusRegister = stringArray[1].filterNot { filtered.indexOf(it) > -1 }.toBoolean()
            if(stringArray[0].isNotBlank()) {
                Toast.makeText(baseContext, stringArray[0], Toast.LENGTH_SHORT).show()
            }
            Log.d("WER",stringArray[0])
            Log.d("WER",reCaptchaStatusRegister.toString())
        }


        noAccount.setOnClickListener {
            val navTo = startActivity(Intent(this, RegisterActivity::class.java))
            CustomIntent.customType(this, "bottom-to-up")
        }
        resetPassword.setOnClickListener {
            val navTo = startActivity(Intent(this, ResetPasswordActivity::class.java))
            CustomIntent.customType(this, "up-to-bottom")
        }



        ButtonSingIn.setOnClickListener {
            val username = LOusername.text.toString().trim()
            val password = LOpassword.text.toString().trim()


            if (TextUtils.isEmpty(username)) {
                fieldError(LOusername)
                LOusername?.error = "Fulfill username field"
            } else {
                clearError(LOusername)
            }
            if (TextUtils.isEmpty(password)) {
                fieldError(LOpassword)
                LOpassword?.error = "Fulfill Password field"
            } else {
                clearError(LOpassword)
            }

            if (!isValidLength(password) && password.isNotBlank()){
                fieldError(LOpassword)
                LOpassword?.error = "Password must contain at least 6 characters"
            }

            if (password.isNotBlank() && username.isNotBlank() && isValidLength(password)){
                Log.d("WER23",reCaptchaStatus.toString())
                Log.d("WER24",reCaptchaStatusRegister.toString())


                var preferences: SharedPreferences = getSharedPreferences("UserDataBook", Context.MODE_PRIVATE)
                var checkStatus = preferences.getBoolean("remember", false)

//                reCaptchaStatus = true
                if (!(reCaptchaStatusRegister || reCaptchaStatus)) {
                    SafetyNet.getClient(this@LoginActivity)
                        .verifyWithRecaptcha(SAFETY_NET_SITE_KEY)
                        .addOnSuccessListener { response ->
                            if (response.tokenResult!!.isNotEmpty())
                                verifyTokenOnServer(response.tokenResult)
                            FirebaseApp.initializeApp(this)
                            var check: Boolean = false
                            database = FirebaseDatabase.getInstance().getReference("user/login")
                            database.child(username).get().addOnSuccessListener {
                                if (it.exists()) {
                                    val usernameLogin = it.child("username").value
                                    val hashPass = it.child("password").value.toString()
                                    val result = BCrypt.verifyer().verify(password.toCharArray(), hashPass)
                                    Log.d("HUETA", hashPass)
                                    Log.d("HUETA", result.toString())
                                    if(result.verified){
                                        Toast.makeText(baseContext, "Login Successfully", Toast.LENGTH_LONG).show()
                                        val navTo = startActivity(Intent(this, AccountInfoActivity::class.java))
                                        CustomIntent.customType(this, "fadein-to-fadeout")
                                        Log.d("checkStatus", checkStatus.toString())
                                        if(checkStatus){
                                            var editor = preferences.edit()
                                            editor.putString("username", username)
                                            editor.putString("hashPassword", hashPass)
                                            editor.apply()
                                            Log.d("CheckStatus", preferences.getBoolean("remember", false).toString())
                                            Log.d("CheckStatus", preferences.getString("username", "").toString())
                                            Log.d("CheckStatus", preferences.getString("hashPassword", "").toString())
                                        }
                                    }
                                    else{
                                        Toast.makeText(baseContext, "Wrong Password was entered", Toast.LENGTH_LONG).show()
                                        fieldError(LOpassword)
                                        LOpassword?.error = "Wrong Password"
                                    }
                                } else {
                                    Toast.makeText(baseContext, "User not found", Toast.LENGTH_LONG).show()
                                    fieldError(LOusername)
                                    LOusername?.error = "User doesn't exists"
                                    fieldError(LOpassword)
                                    LOpassword?.error = "User doesn't exists"
                                }
                            }

                                .addOnFailureListener {
                                    check = false
                                    Log.d("checkAccountOut", check.toString())
                                }


                        }
                        .addOnFailureListener { e ->
                            if (e is com.google.android.gms.common.api.ApiException) {
                                android.util.Log.d(
                                    "EDMTERROR",
                                    "Error: " + com.google.android.gms.common.api.CommonStatusCodes.getStatusCodeString(
                                        e.statusCode
                                    )
                                )
                            } else {
                                android.util.Log.d("EDMTERROR", "Unknown Error has occured")
                            }
                        }
                }
                else{
                    FirebaseApp.initializeApp(this)
                    var check: Boolean = false
                    database = FirebaseDatabase.getInstance().getReference("user/login")
                    database.child(username).get().addOnSuccessListener {
                        if (it.exists()) {
                            val usernameLogin = it.child("username").value
                            val hashPass = it.child("password").value.toString()
                            val result = BCrypt.verifyer().verify(password.toCharArray(), hashPass)
                            if(result.verified){
                                Toast.makeText(baseContext, "Login Successfully", Toast.LENGTH_LONG).show()
                                val navTo = startActivity(Intent(this, AccountInfoActivity::class.java))
                                CustomIntent.customType(this, "fadein-to-fadeout")
                                Log.d("checkStatus1", preferences.getBoolean("remember", false).toString())
                                if(checkStatus){
                                    Log.d("checkStatus1", "ZASHEL")
                                    var editor = preferences.edit()
                                    editor.putString("username", username)
                                    editor.putString("hashPassword", hashPass)
                                    editor.apply()
                                    Log.d("checkStatus1", preferences.getBoolean("remember", false).toString())
                                    Log.d("checkStatus1", preferences.getString("username", "").toString())
                                    Log.d("checkStatus1", preferences.getString("hashPassword", "").toString())
                                }


                            }
                            else{
                                Toast.makeText(baseContext, "Wrong Password was entered", Toast.LENGTH_LONG).show()
                                fieldError(LOpassword)
                                LOpassword?.error = "Wrong Password"
                            }
                        } else {
                            Toast.makeText(baseContext, "User not found", Toast.LENGTH_LONG).show()
                            fieldError(LOusername)
                            LOusername?.error = "User doesn't exists"
                            fieldError(LOpassword)
                            LOpassword?.error = "User doesn't exists"
                        }
                    }

                        .addOnFailureListener {
                            check = false
                            Log.d("checkAccountOut", check.toString())
                        }
                }





            }

        }



       rememberMe.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked) {
                var preferences: SharedPreferences = getSharedPreferences("UserDataBook", Context.MODE_PRIVATE)
                var editor = preferences.edit()
                editor.putBoolean("remember", true)
                editor.apply()
            }
           else{
                var preferences: SharedPreferences = getSharedPreferences("UserDataBook", Context.MODE_PRIVATE)
                var editor = preferences.edit()
                editor.putBoolean("remember", false)
                editor.putString("username", "")
                editor.putString("hashPassword", "")
                editor.apply()
            }
        }







        myService = api

        button_reCAPTCHA.setOnClickListener{
            val captchaRegister = intent.getBooleanExtra(reCaptchaStatus.toString(), false)
            Log.d("captchaRegister", captchaRegister.toString())

            if(reCaptchaStatusRegister || reCaptchaStatus ){
                reCaptchaStatus = true
                Toast.makeText(this@LoginActivity, "reCaptcha Verified", Toast.LENGTH_SHORT)
                    .show()
            }
            else{

                SafetyNet.getClient(this@LoginActivity)
                    .verifyWithRecaptcha(SAFETY_NET_SITE_KEY)
                    .addOnSuccessListener { response ->
                        if(response.tokenResult!!.isNotEmpty())
                            verifyTokenOnServer(response.tokenResult)
                    }
                    .addOnFailureListener {  e->
                        if (e is ApiException){
                            Log.d("EDMTERROR", "Error: "+CommonStatusCodes.getStatusCodeString(e.statusCode))
                        }
                        else{
                            Log.d("EDMTERROR", "Unknown Error has occured")
                        }
                    }
            }

        }
    }

    override fun onPause() {
        super.onPause()
        clearError(LOusername)
        clearError(LOpassword)

    }

    private fun clearError(RE: EditText) {
        val pL: Int = RE.paddingLeft
        val pT: Int = RE.paddingTop
        val pR: Int = RE.paddingRight
        val pB: Int = RE.paddingBottom
        RE.error = null
        RE.clearFocus()
        RE.background = getDrawable(R.drawable.login_edit_back)
        RE.setPadding(pL, pT, pR, pB)
        RE.setHintTextColor(Color.parseColor("#4D5367"))

    }
    private fun fieldError(RE: EditText) {
        val pL: Int = RE.paddingLeft
        val pT: Int = RE.paddingTop
        val pR: Int = RE.paddingRight
        val pB: Int = RE.paddingBottom
        RE.background = getDrawable(R.drawable.caution)
        RE.setPadding(pL, pT, pR, pB)
        RE.setHintTextColor(Color.parseColor("#FFFFFF"))
        RE.clearFocus()
    }


    private fun verifyTokenOnServer(response: String?) {
        val dialog = SpotsDialog(this@LoginActivity)
        dialog.show()
        dialog.setMessage("Please Wait a little more...")
        myService.validate(response!!).enqueue(object: Callback<MyResponse> {
            override fun onFailure(call: Call<MyResponse>?, t: Throwable) {
                dialog.dismiss()
                Log.d("EDMTERROR", t!!.message.toString())
            }

            override fun onResponse(call: Call<MyResponse>?, response: Response<MyResponse>?) {
                dialog.dismiss()
                if(response!!.body()!!.success) {
                    reCaptchaStatus = true
                    Toast.makeText(this@LoginActivity, "reCaptcha Verified", Toast.LENGTH_SHORT)
                        .show()
                }
                else {
                    Toast.makeText(this@LoginActivity, response.body()!!.message, Toast.LENGTH_SHORT)
                        .show()
                }
            }

        })

    }

    fun isValidLength(password: String?) : Boolean {
        return if (password != null) {
            password.length > 5
        } else {
            false
        }
    }

}