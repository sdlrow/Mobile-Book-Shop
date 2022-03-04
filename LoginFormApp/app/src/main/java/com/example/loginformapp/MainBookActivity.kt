package com.example.loginformapp

import android.app.ActionBar
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.loginformapp.adapter.RecyclerAdapter
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import kotlinx.android.synthetic.main.activity_main_book.*
import kotlinx.android.synthetic.main.nav_header.*
import kotlinx.android.synthetic.main.nav_header.view.*
import maes.tech.intentanim.CustomIntent


class MainBookActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var dataUserCheckOn: Boolean = false
    private var dataUsernameCheckOn: String = ""
    private var dataPasswordCheckOn: String = ""
    private var drawer: DrawerLayout? = null
    private lateinit var database: DatabaseReference
    private var toolbar: androidx.appcompat.widget.Toolbar? = null
    lateinit var toggle: ActionBarDrawerToggle
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<RecyclerAdapter.ViewHolder>? = null
    val storageRef = FirebaseStorage.getInstance().reference.child("book_image")
    val imageList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_book)

        storageRef.listAll().addOnSuccessListener(OnSuccessListener<ListResult> { listResult ->
            Log.d("Spec", "INSIDE")
            for (file in listResult.items) {

                file.downloadUrl.addOnSuccessListener { uri ->
                    imageList.add(uri.toString())
                    Log.d("Spec1", uri.toString())
                }
                Log.d("Spec2", imageList.size.toString())
            }
            Log.d("Spec2", imageList.size.toString())
        }
        )
        Log.d("Spec2", imageList.size.toString())
        Handler().postDelayed({
            layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            recyclerView.layoutManager = layoutManager
            adapter = RecyclerAdapter(imageList)
            recyclerView.adapter = adapter
        }, 2500)

        val message = intent.getSerializableExtra("usernameData").toString()
        if(message != "null"){
            Log.d("checkStatus1", "ST---------")
            Log.d("checkStatus1", message)
            Log.d("checkStatus1", "ED---------")
            dataUsernameCheckOn = message
        }
        else{
            var preferences: SharedPreferences = getSharedPreferences("UserDataBook", Context.MODE_PRIVATE)
            dataUserCheckOn = preferences.getBoolean("remember", false)
            dataUsernameCheckOn = preferences.getString("username", "").toString()
            dataPasswordCheckOn = preferences.getString("hashPassword", "").toString()
            Log.d("checkStatus1", "---------")
            Log.d("checkStatus1", dataUserCheckOn.toString())
            Log.d("checkStatus1", dataUsernameCheckOn.toString())
            Log.d("checkStatus1", dataPasswordCheckOn .toString())
        }
        Log.d("checkStatus1", "--------------------------")
        Log.d("checkStatus1", "--------------------------")
        var usernameFName = "First Name"
        var usernameLName = "Last Name"
        var usernameEmail = "Email"
        database = FirebaseDatabase.getInstance().getReference("user/login")
        database.child(dataUsernameCheckOn).get().addOnSuccessListener {
            if (it.exists()) {
                usernameFName = it.child("first_name").value.toString()
                usernameLName = it.child("last_name").value.toString()
                usernameEmail = it.child("email").value.toString()
                Log.d("checkStatus1", usernameFName.toString())
                Log.d("checkStatus1", usernameLName.toString())
                Log.d("checkStatus1", usernameEmail.toString())
                val fnameView: TextView = findViewById(R.id.nav_header_full_name) as TextView
                fnameView.text = ("$usernameLName $usernameLName").toString()

                val emailView: TextView = findViewById(R.id.nav_header_email) as TextView
                emailView.text = usernameEmail.toString()
            }
            else{
                Toast.makeText(baseContext, "Account Data Error", Toast.LENGTH_SHORT).show()
                Toast.makeText(baseContext, "Log Out and Log in Again", Toast.LENGTH_SHORT).show()

            }
        }
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)
        val navView: NavigationView = findViewById(R.id.nav_view)
         toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView.setNavigationItemSelectedListener(this)




    }





    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val intent1 = Intent(this, ProfileActivity::class.java)
        val intent2 = Intent(this, ActivityShipping::class.java)
        val intent3 = Intent(this, ChangePasswordActivity::class.java)
        when (item.itemId){

            R.id.nav_profile ->  startActivity(intent1)
            R.id.nav_shipping_address -> startActivity(intent2)
            R.id.nav_password -> startActivity(intent3)


        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)){
            button_Log_Out.setOnClickListener {
                Log.d("checkStatus1", "---1------")
                var preferences: SharedPreferences = getSharedPreferences("UserDataBook", Context.MODE_PRIVATE)
                var editor = preferences.edit()
                editor.putBoolean("remember", false)
                editor.putString("username", "")
                editor.putString("hashPassword", "")
                editor.apply()
                val navTo = startActivity(Intent(this, StartActivity::class.java))
                CustomIntent.customType(this, "fadein-to-fadeout")
            }
            return true
        }

        return super.onOptionsItemSelected(item)
    }



}
