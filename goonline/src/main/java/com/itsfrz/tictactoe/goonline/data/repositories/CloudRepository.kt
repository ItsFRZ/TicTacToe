package com.itsfrz.tictactoe.goonline.data.repositories

import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.itsfrz.tictactoe.goonline.common.Constants
import com.itsfrz.tictactoe.goonline.data.firebase.FirebaseDB
import com.itsfrz.tictactoe.goonline.data.models.Playground
import com.itsfrz.tictactoe.goonline.data.models.UserProfile
import com.itsfrz.tictactoe.goonline.data.service.PlaygroundService
import com.itsfrz.tictactoe.goonline.data.service.UserProfileService
import com.itsfrz.tictactoe.goonline.datastore.GameStoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CloudRepository(
    private val database: FirebaseDB,
    private val dataStoreRepository: GameStoreRepository,
    private val scope : CoroutineScope
    ) : PlaygroundService, UserProfileService {

    private val TAG : String = "CLOUD_REPO"

    override suspend fun updatePlayground(playground: Playground) {
        try {
            if (playground.userId.isNullOrEmpty())
                throw Exception("UserId should not be null or empty")

            database.getReference()
                .child(Constants.USER_PLAYGROUND)
                .child(playground.userId)
                .setValue(playground)
        }catch (e : FirebaseException){
            Log.e(TAG, "updatePlayground: ${e.message}")
        }
    }

    override suspend fun fetchPlaygroundInfoAndStore(userId: String?){
        try {
            if (userId.isNullOrEmpty())
                throw Exception("UserId should not be null or empty")
            database.getPlayGroundReference("${Constants.USER_PLAYGROUND}/${userId}")
                .addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userPlayground = snapshot.getValue<Playground>()
                            ?: throw Exception("User Playground Got Null From Firebase")
                        scope.launch(Dispatchers.IO) {
                            dataStoreRepository.updatePlayground(userPlayground)
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        throw Exception(error.message)
                    }
                })
        }catch (e : FirebaseException){
            Log.e(TAG, "fetchPlaygroundInfoAndStore: ${e.message}")
        }
    }

    override suspend fun updateUserProfile(userProfile: UserProfile) {
        try {
            if (userProfile.userId.isNullOrEmpty())
                throw Exception("UserId should not be null or empty")

            database.getReference()
                .child(Constants.USER_PROFILE)
                .child(userProfile.userId)
                .setValue(userProfile)
        }catch (e : FirebaseException){
            Log.e(TAG, "updateUserProfile: ${e.message}")
        }
    }

    override suspend fun fetchAndStoreUserProfileInfo(userId : String?) {
        try {
            if (userId.isNullOrEmpty())
                throw Exception("UserId should not be null or empty")
            database.getProfileReference("${Constants.USER_PROFILE}/${userId}")
                .addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userProfile = snapshot.getValue<UserProfile>()
                            ?: throw Exception("Userprofile Null From Firebase")
                        scope.launch(Dispatchers.IO) {
                            dataStoreRepository.updateUserProfile(userProfile)
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        throw Exception(error.message)
                    }
                })
        }catch (e : FirebaseException){
            Log.e(TAG, "fetchUserProfileInfo: ${e.message}")
        }
    }
}