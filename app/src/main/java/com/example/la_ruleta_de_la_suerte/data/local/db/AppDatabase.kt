package com.example.la_ruleta_de_la_suerte.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.la_ruleta_de_la_suerte.data.local.dao.PartidaDao
import com.example.la_ruleta_de_la_suerte.data.local.dao.JugadorDao
import com.example.la_ruleta_de_la_suerte.data.local.model.Partida
import com.example.la_ruleta_de_la_suerte.data.local.model.Jugador

@Database(entities = [Partida::class, Jugador::class], version = 6)
abstract class AppDatabase : RoomDatabase() {
    abstract fun partidaDao(): PartidaDao
    abstract fun jugadorDao(): JugadorDao


}
