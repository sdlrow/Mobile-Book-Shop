package com.example.loginformapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.Toast
import at.favre.lib.crypto.bcrypt.BCrypt
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_start.*
import maes.tech.intentanim.CustomIntent

class StartActivity : AppCompatActivity() {
    private var dataUserCheckOn: Boolean = false
    private var dataUsernameCheckOn: String = ""
    private var dataPasswordCheckOn: String = ""
    private var progressBar: ProgressBar? = null
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_start)
        var preferences: SharedPreferences = getSharedPreferences("UserDataBook", Context.MODE_PRIVATE)
        dataUserCheckOn = preferences.getBoolean("remember", false)
        dataUsernameCheckOn = preferences.getString("username", "").toString()
        dataPasswordCheckOn = preferences.getString("hashPassword", "").toString()
        progressBar = findViewById<ProgressBar>(R.id.progressBarStart) as ProgressBar
        Log.d("checkStatus1", "---------")
        Log.d("checkStatus1", dataUserCheckOn.toString())
        Log.d("checkStatus1", dataUsernameCheckOn.toString())
        Log.d("checkStatus1", dataPasswordCheckOn .toString())


        if(dataUserCheckOn){
            progressBar!!.visibility = View.VISIBLE
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            FirebaseApp.initializeApp(this)
            database = FirebaseDatabase.getInstance().getReference("user/login")
            database.child(dataUsernameCheckOn).get().addOnSuccessListener {
                if (it.exists()) {
                    val usernameLogin = it.child("username").value
                    val hashPass = it.child("password").value.toString()
                    val result = dataPasswordCheckOn.equals(hashPass)
                    Log.d("checkStatus1", result.toString())
                    if(result){
                        Log.d("checkStatus1", "=====")
                        Handler().postDelayed({
                            progressBar!!.visibility = View.INVISIBLE
                            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            val navTo = startActivity(Intent(this, MainBookActivity::class.java))
                            CustomIntent.customType(this, "fadein-to-fadeout")
                        }, 2000)
                    }
                    else{
                        Log.d("checkStatus1", "-----")
                        progressBar!!.visibility = View.INVISIBLE
                        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        Login()
                        Register()
                    }
                }
                else{
                    Login()
                    Register()
                }
            }

            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
        else{
            Login()
            Register()
        }

            ButtonLogInSys.isEnabled = !(ButtonLogInSys.isEnabled)
            ButtonLogInRegister.isEnabled = !(ButtonLogInRegister.isEnabled)
            ButtonLogInSys.setOnClickListener {
                val navTo = startActivity(Intent(this, LoginActivity::class.java))
                CustomIntent.customType(this, "left-to-right")
            }




    }

    private fun Login(){
        ButtonLogInSys.isEnabled = !(ButtonLogInSys.isEnabled)
        ButtonLogInRegister.isEnabled = !(ButtonLogInRegister.isEnabled)
        ButtonLogInSys.setOnClickListener {
            val navTo = startActivity(Intent(this, LoginActivity::class.java))
            CustomIntent.customType(this, "left-to-right")
        }
    }

    private fun Register(){
        ButtonLogInRegister.setOnClickListener {
            val navTo = startActivity(Intent(this, RegisterActivity::class.java))
            CustomIntent.customType(this, "left-to-right")
        }

    }


}