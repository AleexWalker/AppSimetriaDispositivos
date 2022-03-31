package com.example.appsimetria.dispositives

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appsimetria.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.android.synthetic.main.activity_dispositive_menu.*
import kotlinx.coroutines.*

class DispositiveMenu : AppCompatActivity() {

    private lateinit var baseDatos: FirebaseFirestore
    private var listaDispositivos: ArrayList<String> = arrayListOf()

    @SuppressLint("RtlHardcoded")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dispositive_menu)
        baseDatos = FirebaseFirestore.getInstance()
        baseDatos.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
        autoCompleteDispositivo.gravity = Gravity.START

        loadData()

        cardEscaneoList.setOnClickListener{
            autoCompleteDispositivo.gravity = Gravity.START
        }
    }

    private fun loadData() {
        val itemDispositivo = ArrayList<ItemAdapter>()

        baseDatos
            .collection("Dispositivos")
            .get()
            .addOnCompleteListener { task ->
                for (document in task.result) {
                    itemDispositivo.add(ItemAdapter(document.id, "${document.getString("Calle")}, ${document.getString("Numero")}, ${document.getString("Localidad")}"))
                    listaDispositivos.add(document.id)
                    Log.e(TAG, "${document.id} ${document.getString("Localidad")}, ${document.getString("Numero")}, ${document.getString("Ciudad")}")
                }
                val adaptador = DispositiveAdapter(itemDispositivo)
                recyclerList.adapter = adaptador
                recyclerList.layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
            }
        val adaptadorAutoComplete = ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, listaDispositivos)
        autoCompleteDispositivo.setAdapter(adaptadorAutoComplete)
        autoCompleteDispositivo.threshold = 1
    }
}