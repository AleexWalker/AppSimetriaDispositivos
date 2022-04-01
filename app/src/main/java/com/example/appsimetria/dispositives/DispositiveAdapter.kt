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
                         private val clickListener: (String) -> Unit): RecyclerView.Adapter<DispositiveAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val dispositivo: TextView = itemView.findViewById(R.id.itemDispositivo)
        val localidad: TextView = itemView.findViewById(R.id.ciudadItemDispositivo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_dispositivo, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = listaDispositivos[position]
        holder.dispositivo.text = currentItem.dispositivo
        holder.localidad.text = currentItem.localidad

        holder.itemView.setOnClickListener {
            clickListener(listaDispositivos[position].toString())
        }
    }

    override fun getItemCount(): Int {
        return listaDispositivos.size
    }
}
