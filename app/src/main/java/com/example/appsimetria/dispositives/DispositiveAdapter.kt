package com.example.appsimetria.dispositives

import android.view.*
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.appsimetria.R
import kotlin.collections.ArrayList

class DispositiveAdapter(private val listaDispositivos: ArrayList<ItemAdapter>,
                         private val clickListener: (ItemAdapter) -> Unit): RecyclerView.Adapter<DispositiveAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View, clickAtPosition: (String) -> Unit): RecyclerView.ViewHolder(itemView) {
        val dispositivo: TextView = itemView.findViewById(R.id.itemDispositivo)
        val localidad: TextView = itemView.findViewById(R.id.ciudadItemDispositivo)

        init {
            itemView.setOnClickListener {
                clickAtPosition(adapterPosition.toString())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_dispositivo, parent, false)) {
            clickListener(listaDispositivos[it.toInt()])
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = listaDispositivos[position]
        holder.dispositivo.text = currentItem.dispositivo
        holder.localidad.text = currentItem.localidad

        holder.itemView.setOnClickListener {
            clickListener(listaDispositivos[position])
        }
    }

    override fun getItemCount(): Int {
        return listaDispositivos.size
    }
}
