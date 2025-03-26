package com.example.la_ruleta_de_la_suerte.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.la_ruleta_de_la_suerte.data.local.dao.PartidaDao
import com.example.la_ruleta_de_la_suerte.data.local.dao.JugadorDao
import com.example.la_ruleta_de_la_suerte.data.local.model.Partida
import com.example.la_ruleta_de_la_suerte.data.local.model.Jugador

@Database(entities = [Partida::class, Jugador::class], version = 5)
abstract class AppDatabase : RoomDatabase() {
    abstract fun partidaDao(): PartidaDao
    abstract fun jugadorDao(): JugadorDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ruleta_db"
                )
                    .fallbackToDestructiveMigration() // Destruye la DB si hay cambios en la versión
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
