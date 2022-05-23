package com.example.appsimetria.dispositives

import android.content.Context
import android.content.Intent
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

import com.example.appsimetria.R
import com.example.appsimetria.maps.MapsDeleteDevice

import kotlinx.android.synthetic.main.item_adapter_dispositive.view.*
import kotlin.collections.ArrayList

class DeviceAdapter(private val listaDispositivos: ArrayList<DeviceAdapterItem>,
                    private val clickListener: (DeviceAdapterItem) -> Unit): RecyclerView.Adapter<DeviceAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View, clickAtPosition: (String) -> Unit): RecyclerView.ViewHolder(itemView) {
        val dispositivo: TextView = itemView.findViewById(R.id.itemDispositivo)
        val localidad: TextView = itemView.findViewById(R.id.ciudadItemDispositivo)
        val context: Context? = itemView.context

        init {
            itemView.setOnClickListener {
                clickAtPosition(adapterPosition.toString())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_adapter_dispositive, parent, false)) {
            clickListener(listaDispositivos[it.toInt()])
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = listaDispositivos[position]
        val context: Context? = holder.itemView.context
        holder.dispositivo.text = currentItem.dispositivo
        holder.localidad.text = currentItem.localidad

        holder.itemView.setOnClickListener {
            clickListener(listaDispositivos[position])
        }

        holder.itemView.itemMapsDevices.setOnClickListener {
            val intentSeleccionado = Intent(context, MapsDeleteDevice::class.java)
            intentSeleccionado.putExtra("Seleccionado", currentItem.dispositivo)
            intentSeleccionado.putExtra("Latitud", currentItem.latitud)
            intentSeleccionado.putExtra("Longitud", currentItem.longitud)
            Toast.makeText(context, "¡Dispositivo " + currentItem.dispositivo + " situado en el mapa!", Toast.LENGTH_LONG).show()
            context!!.startActivity(intentSeleccionado)
        }
    }

    override fun getItemCount(): Int {
        return listaDispositivos.size
    }
}
