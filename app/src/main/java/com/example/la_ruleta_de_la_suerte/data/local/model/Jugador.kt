package com.example.la_ruleta_de_la_suerte.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "JUGADOR")
data class Jugador(
    @PrimaryKey val id: Int = 1, // ID fijo porque solo tenemos un jugador
    val cantidadMonedas: Int
)
