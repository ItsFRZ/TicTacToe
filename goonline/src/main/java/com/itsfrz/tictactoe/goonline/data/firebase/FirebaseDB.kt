package com.itsfrz.tictactoe.goonline.data.firebase

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object FirebaseDB {

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
}