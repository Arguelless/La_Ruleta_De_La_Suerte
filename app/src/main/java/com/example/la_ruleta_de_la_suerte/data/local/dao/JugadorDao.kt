package com.example.la_ruleta_de_la_suerte.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.la_ruleta_de_la_suerte.data.local.model.Jugador
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single

@Dao
interface JugadorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertar(jugador: Jugador): Completable

    @Query("SELECT * FROM JUGADOR LIMIT 1")
    fun obtenerJugador(): Single<Jugador>

    @Query("UPDATE JUGADOR SET cantidadMonedas = :nuevaCantidad WHERE id = 1")
    fun actualizarMonedas(nuevaCantidad: Int): Completable
}