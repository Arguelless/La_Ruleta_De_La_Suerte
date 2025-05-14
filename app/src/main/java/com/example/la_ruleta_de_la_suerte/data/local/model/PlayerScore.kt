package com.example.la_ruleta_de_la_suerte.data.local.model

import java.util.Date

data class PlayerScore(
    val name: String = "",
    val coinDif: Int = 0,
    val date: Long = System.currentTimeMillis()
)