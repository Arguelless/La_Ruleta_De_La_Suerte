package com.example.la_ruleta_de_la_suerte.data.local.dao

import androidx.room.*
import com.example.la_ruleta_de_la_suerte.data.local.model.Partida
import io.reactivex.rxjava3.core.*

@Dao
interface PartidaDao {
    @Insert
    fun insertar(partida: Partida): Completable

    @Query("SELECT * FROM PARTIDA ORDER BY fecha DESC")
    fun obtenerTodas(): Single<List<Partida>>

    @Query("DELETE FROM PARTIDA")
    fun eliminarTodas(): Completable

}