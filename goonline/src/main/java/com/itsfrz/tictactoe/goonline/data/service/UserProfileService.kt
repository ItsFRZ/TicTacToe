package com.itsfrz.tictactoe.goonline.data.service

import com.itsfrz.tictactoe.goonline.data.models.UserProfile

interface UserProfileService {
    suspend fun updateUserProfile(userProfile: UserProfile) : Unit
    suspend fun fetchAndStoreUserProfileInfo(userId : String?)
    suspend fun updateOnlineStatus(isOnline : Boolean,userId : String?)
}