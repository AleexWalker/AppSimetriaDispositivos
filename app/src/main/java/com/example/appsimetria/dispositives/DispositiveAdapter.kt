package com.example.appsimetria.dispositives

import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appsimetria.R
import kotlin.collections.ArrayList

class DispositiveAdapter(private val listaDispositivos: ArrayList<ItemAdapter>): RecyclerView.Adapter<DispositiveAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_dispositivo, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = listaDispositivos[position]
        holder.dispositivo.text = currentItem.dispositivo
        holder.localidad.text = currentItem.localidad
    }

    override fun getItemCount(): Int {
        return listaDispositivos.size
    }

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val dispositivo: TextView = itemView.findViewById(R.id.itemDispositivo)
        val localidad: TextView = itemView.findViewById(R.id.ciudadItemDispositivo)
    }
}