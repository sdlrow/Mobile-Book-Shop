package com.example.loginformapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_account_info.*
import kotlinx.android.synthetic.main.alert_custom_dialog.view.*
import maes.tech.intentanim.CustomIntent


class AccountInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_info)



        mainLoginBtn.setOnClickListener {
            //Inflate the dialog with custom view

//            val mDialogView = LayoutInflater.from(this).inflate(R.layout.alert_custom_dialog, null)
//
//            val mBuilder = AlertDialog.Builder(this).setView(mDialogView)
//
//            val  mAlertDialog = mBuilder.show()


            val builder = AlertDialog.Builder(this,R.style.CustomAlertDialog)
                .create()
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.alert_custom_dialog, null)
            builder.setView(mDialogView)
            builder.show()
            val ok = mDialogView.findViewById<Button>(R.id.dialogOkBtn)
            val cancel = mDialogView.findViewById<Button>(R.id.dialogCancelBtn)
            builder.setView(mDialogView)







            ok.setOnClickListener {
                //dismiss dialog
                builder.dismiss()
                var preferences: SharedPreferences = getSharedPreferences("UserDataBook", Context.MODE_PRIVATE)
                var editor = preferences.edit()
                editor.putBoolean("remember", false)
                editor.putString("username", "")
                editor.putString("hashPassword", "")
                editor.apply()
                val navTo = startActivity(Intent(this, StartActivity::class.java))
                CustomIntent.customType(this, "fadein-to-fadeout")
            }
            //cancel button click of custom layout
            cancel.setOnClickListener {
                //dismiss dialog
                builder.dismiss()
            }
        }

    }
}