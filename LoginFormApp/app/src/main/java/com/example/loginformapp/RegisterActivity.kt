package com.example.loginformapp


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.loginformapp.model.Address
import com.example.loginformapp.model.MyResponse
import com.example.loginformapp.model.Payment
import com.example.loginformapp.model.User
import com.example.loginformapp.retrofit.IreCAPTCHA
import com.example.loginformapp.retrofit.RetrofitClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.safetynet.SafetyNet
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_reset_password.*
import maes.tech.intentanim.CustomIntent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RegisterActivity : AppCompatActivity() {

    companion object{
        private val SAFETY_NET_SITE_KEY = "6LckzH4eAAAAAJKu2Dk0sSb3cAmThUHOH28niCvP"

    }

    private val api: IreCAPTCHA
        get() = RetrofitClient.getClient("http://10.0.2.2/server_validate/").create(IreCAPTCHA::class.java)
    lateinit var myService: IreCAPTCHA

    private var reCaptchaStatus: Boolean = false
    private var message = ""
    private lateinit var database: DatabaseReference
    private var progressBar: ProgressBar? = null
    private lateinit var mAuth: FirebaseAuth

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        var preferences: SharedPreferences = getSharedPreferences("UserDataBook", Context.MODE_PRIVATE)
        Log.d("checkStatus1", "---------")
        Log.d("checkStatus1", preferences.getBoolean("remember", false).toString())
        Log.d("checkStatus1", preferences.getString("username", "").toString())
        Log.d("checkStatus1", preferences.getString("hashPassword", "").toString())


        progressBar = findViewById<ProgressBar>(R.id.progressBar1) as ProgressBar
        AlreadyAccount.setOnClickListener {
            val navTo =Intent(this, LoginActivity::class.java)

            var stringValue: ArrayList<String> = ArrayList()
            stringValue.add(message)
            stringValue.add(reCaptchaStatus.toString())
            Log.d("WERFROM", stringValue[0].toString())
            Log.d("WERFROM", stringValue[1].toString())
            navTo.putExtra("key", stringValue)
//            navTo.putExtra((LoginActivity.captchaFromRegisrer).toString(), reCaptchaStatus.toString())
            startActivity(navTo)
            CustomIntent.customType(this, "up-to-bottom")
        }

        ButtonSingUp.setOnClickListener {
            val username = REusername.text.toString().trim()
            val email = REemail.text.toString().trim()
            val last_name = RElast_name.text.toString().trim()
            val first_name = REfirst_name.text.toString().trim()
            val password = REpassword.text.toString().trim()
            val confirm_password = REconfirm_password.text.toString().trim()

            if (TextUtils.isEmpty(username)){
                fieldError(REusername)
                REusername?.error = "Fulfill username field"
            }
            else{
                clearError(REusername)
            }
            if (TextUtils.isEmpty(email)){
                fieldError(REemail)
                REemail?.error = "Fulfill email field"
            }
            else{
                clearError(REemail)
            }

            if (TextUtils.isEmpty(last_name)){
                fieldError(RElast_name)
                RElast_name?.error = "Fulfill Last Name field"
            }
            else{
                clearError(RElast_name)
            }
            if (TextUtils.isEmpty(first_name)){
                fieldError(REfirst_name)
                REfirst_name?.error = "Fulfill First Name field"
            }
            else{
                clearError(REfirst_name)
            }
            if (TextUtils.isEmpty(password)){
                fieldError(REpassword)
                REpassword?.error = "Fulfill Password field"
            }
            else{
                clearError(REpassword)
            }
            if (TextUtils.isEmpty(confirm_password)){
                fieldError(REconfirm_password)
                REconfirm_password?.error = "Fulfill Confirm Password field"
            }
            else{
                clearError(REconfirm_password)
            }
            if (!isValidEmail(email) && email.isNotBlank()){
                fieldError(REemail)
                REemail?.error = "Invalid Email Address"
            }

            if (!isValidLength(confirm_password) && !isValidLength(password) && password.isNotBlank() && confirm_password.isNotBlank()){
                fieldError(REconfirm_password)
                REconfirm_password?.error = "Password must contain at least 6 characters"
                fieldError(REpassword)
                REpassword?.error = "Password must contain at least 6 characters"
            }


            if ((password != confirm_password) && password.isNotBlank() && confirm_password.isNotBlank()){
                fieldError(REconfirm_password)
                REconfirm_password?.error = "Password not match"
                fieldError(REpassword)
                REpassword?.error = "Password not match"
            }
            else if (isValidEmail(email) && isValidLength(password) && isValidLength(confirm_password) && password.isNotBlank() && confirm_password.isNotBlank() && username.isNotBlank() && email.isNotBlank() && last_name.isNotBlank() && first_name.isNotBlank()){

                clearError(REpassword)
                clearError(REconfirm_password)

//

//                reCaptchaStatus = true

//

                if(!reCaptchaStatus){
                    com.google.android.gms.safetynet.SafetyNet.getClient(this@RegisterActivity)
                        .verifyWithRecaptcha(com.example.loginformapp.RegisterActivity.Companion.SAFETY_NET_SITE_KEY)
                        .addOnSuccessListener { response ->
                            if(response.tokenResult!!.isNotEmpty())
                                verifyTokenOnServer(response.tokenResult)
                            val passHash = BCrypt.withDefaults().hashToString(12, password.toCharArray())
                            Log.d("register", passHash)
                            val data = User(username, email, last_name, first_name, passHash)
                            FirebaseApp.initializeApp(this)
                            var check: Boolean = false
                            database = FirebaseDatabase.getInstance().getReference("user/login")
                            database.child(username).get().addOnSuccessListener {
                                if(it.exists()){
                                    Toast.makeText(baseContext, "This Username is already Registered", Toast.LENGTH_LONG).show()
                                }
                                else{

                                    mAuth = FirebaseAuth.getInstance()
                                    progressBar!!.visibility = View.VISIBLE
                                    mAuth.createUserWithEmailAndPassword(email, password)
                                        .addOnCompleteListener (this) { task ->
                                            if (task.isSuccessful) {
                                                Log.d("mAuth", "SUCCESS")
                                                val passHash = BCrypt.withDefaults().hashToString(12, password.toCharArray())
                                                Log.d("register", passHash)
                                                val data = User(username, email, last_name, first_name, passHash)
                                                FirebaseApp.initializeApp(this)
                                                var check: Boolean = false
                                                database = FirebaseDatabase.getInstance().getReference("user/login")
                                                database.child(username).get().addOnSuccessListener {
                                                    if (it.exists()) {
                                                        Toast.makeText(
                                                            baseContext,
                                                            "This Username is already Registered",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                        progressBar!!.visibility = View.GONE
                                                    } else {
                                                        Log.d("WTF", "WTF")
                                                        FirebaseDatabase.getInstance()
                                                            .getReference("user/login/$username").setValue(data)
                                                            .addOnSuccessListener {
                                                                val navTo =Intent(this, LoginActivity::class.java)
                                                                reCaptchaStatus = true
                                                                message = "Account Created Successfully!"

                                                                Log.d("Address", "address")
                                                                val data_address = Address("", "", "","", "", "")
                                                                FirebaseDatabase.getInstance()
                                                                    .getReference("user/address/$username").setValue(data_address)
                                                                    .addOnSuccessListener {
                                                                        Log.d("Address", "address_reg")
                                                                    }
                                                                Log.d("Payment", "payment")
                                                                val data_payment = Payment("", "", "","", "")
                                                                FirebaseDatabase.getInstance()
                                                                    .getReference("user/payment/$username").setValue(data_payment)
                                                                    .addOnSuccessListener {
                                                                        Log.d("Payment", "payment_reg")
                                                                    }


                                                                var stringValue: ArrayList<String> = ArrayList()
                                                                stringValue.add(message)
                                                                stringValue.add(reCaptchaStatus.toString())
                                                                Log.d("WER2", stringValue[0].toString())
                                                                Log.d("WER2", stringValue[1].toString())
                                                                progressBar!!.visibility = View.GONE
                                                                navTo.putExtra("key", stringValue)
//                                                                  navTo.putExtra((LoginActivity.captchaFromRegisrer).toString(), reCaptchaStatus.toString())
                                                                startActivity(navTo)
                                                                CustomIntent.customType(this, "up-to-bottom")
//                                                                  Toast.makeText(baseContext, "Account Created Successfully!", Toast.LENGTH_SHORT).show()
                                                            }
                                                            .addOnFailureListener {
                                                                Toast.makeText(
                                                                    baseContext,
                                                                    "Cannot Register Account!",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                                progressBar!!.visibility = View.GONE
                                                            }


                                                    }
                                                }

                                                    .addOnFailureListener {
                                                        check = false
                                                        Log.d("checkAccountOut", check.toString())
                                                    }

                                            } else {
                                                Toast.makeText(baseContext, "This Email already Registered", Toast.LENGTH_SHORT).show()
                                                Log.d("mAuth", "This Email already Registered")
                                                progressBar!!.visibility = View.GONE
                                            }
                                        }

                                }
                            }

                                .addOnFailureListener {
                                    check = false
                                    Log.d("checkAccountOut", check.toString())
                                }

                        }
                        .addOnFailureListener {  e->
                            if (e is com.google.android.gms.common.api.ApiException){
                                android.util.Log.d("EDMTERROR", "Error: "+ com.google.android.gms.common.api.CommonStatusCodes.getStatusCodeString(e.statusCode))
                            }
                            else{
                                android.util.Log.d("EDMTERROR", "Unknown Error has occured")
                            }
                        }
                }
                if(password == confirm_password && reCaptchaStatus) {


                    mAuth = FirebaseAuth.getInstance()
                    progressBar!!.visibility = View.VISIBLE
                    mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener (this) { task ->
                            if (task.isSuccessful) {
                                Log.d("mAuth", "SUCCESS")
                                val passHash = BCrypt.withDefaults().hashToString(12, password.toCharArray())
                                Log.d("register", passHash)
                                val data = User(username, email, last_name, first_name, passHash)
                                FirebaseApp.initializeApp(this)
                                var check: Boolean = false
                                database = FirebaseDatabase.getInstance().getReference("user/login")
                                database.child(username).get().addOnSuccessListener {
                                    if (it.exists()) {
                                        Toast.makeText(
                                            baseContext,
                                            "This Username is already Registered",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        progressBar!!.visibility = View.GONE
                                    } else {
                                        Log.d("WTF", "WTF")
                                        FirebaseDatabase.getInstance()
                                            .getReference("user/login/$username").setValue(data)
                                            .addOnSuccessListener {
                                                val navTo =Intent(this, LoginActivity::class.java)
                                                reCaptchaStatus = true
                                                message = "Account Created Successfully!"


                                                Log.d("Address", "address")
                                                val data_address = Address("", "", "","", "", "")
                                                FirebaseDatabase.getInstance()
                                                    .getReference("user/address/$username").setValue(data_address)
                                                    .addOnSuccessListener {
                                                        Log.d("Address", "address_reg")
                                                    }
                                                Log.d("Payment", "payment")
                                                val data_payment = Payment("", "", "","", "")
                                                FirebaseDatabase.getInstance()
                                                    .getReference("user/payment/$username").setValue(data_payment)
                                                    .addOnSuccessListener {
                                                        Log.d("Payment", "payment_reg")
                                                    }


                                                var stringValue: ArrayList<String> = ArrayList()
                                                stringValue.add(message)
                                                stringValue.add(reCaptchaStatus.toString())
                                                Log.d("WER2", stringValue[0].toString())
                                                Log.d("WER2", stringValue[1].toString())
                                                progressBar!!.visibility = View.GONE
                                                navTo.putExtra("key", stringValue)
//                                              navTo.putExtra((LoginActivity.captchaFromRegisrer).toString(), reCaptchaStatus.toString())
                                                startActivity(navTo)
                                                CustomIntent.customType(this, "up-to-bottom")
//                                              Toast.makeText(baseContext, "Account Created Successfully!", Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(
                                                    baseContext,
                                                    "Cannot Register Account!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                progressBar!!.visibility = View.GONE
                                            }
                                    }
                                }

                                    .addOnFailureListener {
                                        check = false
                                        Log.d("checkAccountOut", check.toString())
                                    }

                            } else {
                                Toast.makeText(baseContext, "This Email already Registered", Toast.LENGTH_SHORT).show()
                                Log.d("mAuth", "This Email already Registered")
                                progressBar!!.visibility = View.GONE
                            }
                        }




                }
            }


        }






        myService = api
        button_reCAPTCHA.setOnClickListener{


            SafetyNet.getClient(this@RegisterActivity)
                .verifyWithRecaptcha(SAFETY_NET_SITE_KEY)
                .addOnSuccessListener { response ->
                    if(response.tokenResult!!.isNotEmpty())
                        verifyTokenOnServer(response.tokenResult)
                }
                .addOnFailureListener {  e->
                    if (e is ApiException){
                        Log.d("EDMTERROR", "Error: "+ CommonStatusCodes.getStatusCodeString(e.statusCode))
                    }
                    else{
                        Log.d("EDMTERROR", "Unknown Error has occured")
                    }
                }
        }




    }



    override fun onPause() {
        super.onPause()
        clearError(REusername)
        clearError(REemail)
        clearError(RElast_name)
        clearError(REfirst_name)
        clearError(REpassword)
        clearError(REconfirm_password)
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
        val dialog = SpotsDialog(this@RegisterActivity)
        dialog.show()
        dialog.setMessage("Please Wait a little more...")
        myService.validate(response!!).enqueue(object: Callback<MyResponse> {
            override fun onFailure(call: Call<MyResponse>?, t: Throwable) {
                dialog.dismiss()
                Log.d("EDMTERROR", t.message.toString())
            }

            override fun onResponse(call: Call<MyResponse>?, response: Response<MyResponse>?) {
                dialog.dismiss()
                if(response!!.body()!!.success) {
                    reCaptchaStatus = true
                    Toast.makeText(this@RegisterActivity, "reCaptcha Verified", Toast.LENGTH_SHORT)
                        .show()
                }
                else {
                    Toast.makeText(this@RegisterActivity, response.body()!!.message, Toast.LENGTH_SHORT)
                        .show()
                }
            }

        })

    }

    fun isValidEmail(target: CharSequence?): Boolean {
        return if (TextUtils.isEmpty(target)) {
            false
        } else {
            Patterns.EMAIL_ADDRESS.matcher(target).matches()
        }
    }


    fun isValidLength(password: String?) : Boolean {
        return if (password != null) {
            password.length > 5
        } else {
            false
        }
    }



}