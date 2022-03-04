package com.example.loginformapp

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_after_login.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.user_edit_dialog_layout.*

class AfterLoginPage : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_after_login)


        FirebaseApp.initializeApp(this)
        database = Firebase.database.reference
        database.child("user").child("login").child("123").get().addOnSuccessListener {
            val fname = it.child("first_name").value
            val lname = it.child("last_name").value
            val username = it.child("username").value
            val email = it.child("email").value
            Log.i("firebase", "Got value ${it.value}")

            val fnameView: TextView = findViewById(R.id.info_userFname) as TextView
            fnameView.text = fname.toString()
            val lnameView: TextView = findViewById(R.id.info_userLname) as TextView
            lnameView.text = lname.toString()
            val emailView: TextView = findViewById(R.id.info_userEmail) as TextView
            emailView.text = email.toString()
            val usernameView: TextView = findViewById(R.id.info_username) as TextView
            usernameView.text = username.toString()

        }.addOnFailureListener {
            Log.e("firebase", "Error getting data", it)
        }

        button_changeUserData.setOnClickListener{
            showAlertDialog()
        }
    }

   private fun showAlertDialog() {
        val inflater = layoutInflater
        val view_dialog: View = inflater.inflate(R.layout.user_edit_dialog_layout, null)
        val btn: Button = view_dialog.findViewById<Button>(R.id.button_saveUserData)
            val builder = AlertDialog.Builder(this)
            builder
                .setView(view_dialog)
                .setTitle("Edit User")
                .setMessage("Change information about user")
                .setCancelable(true)
                .setPositiveButton(
                    "Close"
                ) { dialog, id -> dialog.cancel() }

            val alert = builder.create()
            alert.show()


        btn.setOnClickListener {
            val username = editInfo_username.text.toString().trim()
            val fname = editInfo_userFname.text.toString().trim()
            val lname = editInfo_userLname.text.toString().trim()
            val email = editInfo_userEmail.text.toString().trim()

            if (TextUtils.isEmpty(username)) {
                fieldError(editInfo_username)
                editInfo_username?.error = "Fulfill username field"
            } else {
                clearError(editInfo_username)
            }
            if (TextUtils.isEmpty(fname)) {
                fieldError(editInfo_userFname)
                editInfo_userFname?.error = "Fulfill username field"
            } else {
                clearError(editInfo_userFname)
            }
            if (TextUtils.isEmpty(lname)) {
                fieldError(editInfo_userLname)
                editInfo_userLname?.error = "Fulfill username field"
            } else {
                clearError(editInfo_userLname)
            }
            if (TextUtils.isEmpty(email)) {
                fieldError(editInfo_userEmail)
                editInfo_userEmail?.error = "Fulfill username field"
            } else {
                clearError(editInfo_username)
            }

//            database.child("user").child("login").
//            child("123").child("first_name").setValue(fname)

//            database.child("user").child("login").
//            child("123").child("last_name").setValue(lname)

//            database.child("user").child("login").
//            child("123").child("username").setValue(username)

//            database.child("user").child("login").
//            child("123").child("email").setValue(email)


        }

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
}
