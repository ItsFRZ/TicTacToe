package com.itsfrz.tictactoe.goonline.data.service

import com.itsfrz.tictactoe.goonline.data.models.Playground

interface PlaygroundService {
    suspend fun updatePlayground(playground: Playground) : Unit
    suspend fun fetchPlaygroundInfoAndStore(userId : String?)
}