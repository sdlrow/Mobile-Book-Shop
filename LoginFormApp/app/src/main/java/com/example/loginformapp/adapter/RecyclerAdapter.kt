package com.example.loginformapp.adapter

import android.media.Image
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.loginformapp.MainBookActivity
import com.example.loginformapp.R
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import java.io.File

class RecyclerAdapter(private val imagesList: ArrayList<String>): RecyclerView.Adapter<RecyclerAdapter.ViewHolder>(){
//    val classA = MainBookActivity()
//    val imageList = classA.imageList
//    private var imagesList = intArrayOf(R.drawable.img_test,R.drawable.img_test,R.drawable.img_test,R.drawable.img_test,R.drawable.img_test)
//    val storageRef = FirebaseStorage.getInstance().reference.child("book_image")
//    val imageList = ArrayList<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.card_layout, parent, false)
        return ViewHolder(v)
    }


    override fun getItemCount(): Int {
        Log.d("Spec4", imagesList.size.toString())
        return imagesList.size

    }

    override fun onBindViewHolder(holder: RecyclerAdapter.ViewHolder, position: Int) {


//        holder.itemImage.setImageResource(imagesList[position])
        Glide.with(holder.itemView.context).load(imagesList[position]).into(holder.itemImage);
    }



    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var itemImage: ImageView
        init {
            itemImage = itemView.findViewById(R.id.book_image)
        }
    }


}