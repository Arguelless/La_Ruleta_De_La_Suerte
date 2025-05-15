package com.example.la_ruleta_de_la_suerte.utils

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.la_ruleta_de_la_suerte.R
import com.example.la_ruleta_de_la_suerte.data.local.model.Partida
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistorialAdapter(private val partidas: List<Partida>) :
    RecyclerView.Adapter<HistorialAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val resultado: TextView = view.findViewById(R.id.tv_resultado)
        val total: TextView = view.findViewById(R.id.tv_total)
        val fecha: TextView = view.findViewById(R.id.tv_fecha)
        val indicador: View = view.findViewById(R.id.status_indicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.historial_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sdf = SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault())
        val partida = partidas[position]
        holder.resultado.text = "Result: ${partida.diferenciaMonedas} Coins"
        holder.total.text = "Total: ${partida.totalMonedas} Coins"
        holder.fecha.text = sdf.format(Date(partida.fecha))

        // Cambiar el color del indicador según el resultado
        val color = if (partida.diferenciaMonedas >= 0) Color.GREEN else Color.RED
        holder.indicador.background.setTint(color)
    }

    override fun getItemCount() = partidas.size
}
