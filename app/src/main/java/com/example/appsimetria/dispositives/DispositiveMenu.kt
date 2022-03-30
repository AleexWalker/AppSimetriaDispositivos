package com.example.appsimetria.dispositives

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appsimetria.R
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_dispositive_menu.*
import java.lang.Exception

class DispositiveMenu : AppCompatActivity() {

    private lateinit var baseDatos: FirebaseFirestore
    private var dispositivo: ArrayList<String> = arrayListOf()
    private var ciudadesDispositivos: ArrayList<String> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dispositive_menu)

        baseDatos = Firebase.firestore
        baseDatos
            .collection("Dispositivos")
            .get()
            .addOnSuccessListener { documents ->
                try {
                    if (documents != null) {
                        for (document in documents)
                            document.getString("ID")?.let {
                                dispositivo.add(document.id)
                                ciudadesDispositivos.add(document.getString("Calle")!! + ", " + document.getString("Numero") + "\t" + document.getString("Localidad"))
                            }
                    } else {
                        Toast.makeText(this, "No document found", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.message?.let { Log.e(ContentValues.TAG, it) }
                }
            }
            .addOnFailureListener {
                    e -> Log.e(ContentValues.TAG, "Error writing the document", e)
            }

        val adaptadorAutoComplete = ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, dispositivo)
        autoCompleteDispositivo.setAdapter(adaptadorAutoComplete)
        autoCompleteDispositivo.threshold = 1

        val itemDispositivo = ArrayList<ItemAdapter>()

        for (i in 0 until dispositivo.size)
            itemDispositivo.add(ItemAdapter(i.toString(), ciudadesDispositivos[i].toString()))

        val adaptador = DispositiveAdapter(itemDispositivo)

        recyclerList.adapter = adaptador
        recyclerList.layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)


    }
}