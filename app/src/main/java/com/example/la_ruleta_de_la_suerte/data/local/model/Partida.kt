package com.example.la_ruleta_de_la_suerte.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "PARTIDA")
data class Partida(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "fecha") val fecha: Long,
    @ColumnInfo(name = "diferenciaMonedas") val diferenciaMonedas: Int
)
