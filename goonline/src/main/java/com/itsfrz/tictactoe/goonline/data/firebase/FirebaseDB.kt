package com.itsfrz.tictactoe.goonline.data.firebase

import android.annotation.SuppressLint
import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object FirebaseDB {

    fun init(context: Context){
        FirebaseApp.initializeApp(context)
    }
    fun getInstance() : FirebaseDatabase{
        return FirebaseDatabase.getInstance()
    }

    fun getReference() : DatabaseReference{
        return getInstance().reference
    }

    fun getProfileReference(path : String) : DatabaseReference{
        return getInstance().getReference(path)
    }

    fun getPlayGroundReference(path : String) : DatabaseReference{
        return getInstance().getReference(path)
    }

    fun getReference(path : String) : DatabaseReference{
        return getInstance().getReference(path)
    }

}