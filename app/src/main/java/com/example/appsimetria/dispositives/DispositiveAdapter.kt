package com.example.appsimetria.dispositives

import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appsimetria.R
import kotlin.collections.ArrayList

class DispositiveAdapter(var items: ArrayList<ItemAdapter>) : RecyclerView.Adapter<DispositiveAdapter.TarjViewHolder>() {
    lateinit var onClick : (View) -> Unit

    class TarjViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var dispositivo: TextView = itemView.findViewById(R.id.itemDispositivo)
        var ciudad: TextView = itemView.findViewById(R.id.ciudadItemDispositivo)

        fun bindTarjeta(t: ItemAdapter, onClick: (View) -> Unit) = with(itemView) {
            dispositivo.text = t.dispositivo
            ciudad.text = t.ciudad

            setOnClickListener { onClick(itemView) }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): TarjViewHolder {
        val itemView = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_dispositivo, viewGroup, false)
        return TarjViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: TarjViewHolder, pos: Int) {
        val itemCard = items[pos]
        viewHolder.bindTarjeta(itemCard, onClick)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}