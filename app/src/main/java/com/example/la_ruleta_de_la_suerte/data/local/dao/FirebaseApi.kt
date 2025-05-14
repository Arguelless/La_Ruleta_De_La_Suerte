package com.example.la_ruleta_de_la_suerte.data.local.dao

import com.example.la_ruleta_de_la_suerte.data.local.model.PlayerScore
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface FirebaseApi {

    @GET("top_scores.json")
    fun getTopScores(): Call<Map<String, PlayerScore>>

    @POST("top_scores.json")
    fun addPlayerScore(@Body playerScore: PlayerScore): Call<Void>
}