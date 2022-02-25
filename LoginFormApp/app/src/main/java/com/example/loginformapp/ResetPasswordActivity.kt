package com.example.loginformapp

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_reset_password.*
import java.util.concurrent.TimeUnit
import android.widget.ProgressBar
import maes.tech.intentanim.CustomIntent


class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var db: DatabaseReference
    private var mAuth: FirebaseAuth? = null
    private var progressBar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)
        progressBar = findViewById<ProgressBar>(R.id.progressBarReset) as ProgressBar


        mAuth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance().getReference("user/login")
        Log.d("mAuth1", mAuth.toString())
        btnResetPassword.setOnClickListener {
            val email = edtResetEmail.text.toString().trim()

            if (TextUtils.isEmpty(email)){
                fieldError(edtResetEmail)
                edtResetEmail?.error = "Fulfill email field"
            } else {
                clearError(edtResetEmail)
                if(!isValidEmail(email) && email.isNotBlank()){
                    fieldError(edtResetEmail)
                    edtResetEmail?.error = "Fulfill email field"
                }
                else {
                    clearError(edtResetEmail)
                    progressBar!!.visibility = View.VISIBLE
                    mAuth!!.sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Handler().postDelayed({
                                    progressBar!!.visibility = View.INVISIBLE
                                    Toast.makeText(
                                        this@ResetPasswordActivity,
                                        "Check mailbox to reset your password",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }, 3000)
                            } else {
                                Handler().postDelayed({
                                    progressBar!!.visibility = View.INVISIBLE
                                    Toast.makeText(
                                        this@ResetPasswordActivity,
                                        "Can't reset following mail password",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }, 3000)
                            }
                        }
                }



            }
        }

        backToLogin.setOnClickListener {
            val navTo = startActivity(Intent(this, LoginActivity::class.java))
            CustomIntent.customType(this, "bottom-to-up")
        }
    }

    fun isValidEmail(target: CharSequence?): Boolean {
        return if (TextUtils.isEmpty(target)) {
            false
        } else {
            Patterns.EMAIL_ADDRESS.matcher(target).matches()
        }
    }


    override fun onPause() {
        super.onPause()
        clearError(edtResetEmail)

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






}