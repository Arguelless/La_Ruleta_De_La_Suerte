package com.example.la_ruleta_de_la_suerte.utils

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.la_ruleta_de_la_suerte.R
import com.example.la_ruleta_de_la_suerte.data.local.model.PlayerScore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TopGlobalAdapter(private val partidas: List<PlayerScore>) :
    RecyclerView.Adapter<TopGlobalAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val resultado: TextView = view.findViewById(R.id.tv_resultado)
            val nombreusuario: TextView = view.findViewById(R.id.nombre_usuario)
            val fecha: TextView = view.findViewById(R.id.tv_fecha)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.top_global_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val sdf = SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault())
            val partida = partidas[position]
            holder.resultado.text = "${partida.coinDif} Coins"
            holder.nombreusuario.text = "${position}. ${partida.name}"
            holder.fecha.text = sdf.format(Date(partida.date))

        }

        override fun getItemCount() = partidas.size
    }
