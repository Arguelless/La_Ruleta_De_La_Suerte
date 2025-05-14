package com.example.la_ruleta_de_la_suerte.data.local.db

import com.example.la_ruleta_de_la_suerte.data.local.dao.FirebaseApi
import com.squareup.moshi.Moshi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


object RetrofitInstance {
    private val moshi = Moshi.Builder().build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://laruletadelasuertee-default-rtdb.europe-west1.firebasedatabase.app/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val firebaseApi: FirebaseApi = retrofit.create(FirebaseApi::class.java)
}