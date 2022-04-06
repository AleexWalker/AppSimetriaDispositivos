package com.example.appsimetria.dispositives

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appsimetria.ServicesMenu
import com.example.appsimetria.databinding.ActivityMenuDispositiveBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.android.synthetic.main.activity_menu_dispositive.*

class MenuDispositive : AppCompatActivity() {

    private lateinit var baseDatos: FirebaseFirestore
    private lateinit var binding: ActivityMenuDispositiveBinding
    private var listaDispositivos: ArrayList<String> = arrayListOf()

    @SuppressLint("RtlHardcoded")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuDispositiveBinding.inflate(layoutInflater)
        setContentView(binding.root)
        baseDatos = FirebaseFirestore.getInstance()
        baseDatos.firestoreSettings = FirebaseFirestoreSettings.Builder().build()

        loadData()

        binding.autoCompleteDispositivo.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            val intentDispositivo = Intent(this, VisualizeDispositive::class.java)
            intentDispositivo.putExtra("Seleccionado", adapterView.getItemAtPosition(i).toString())
            startActivity(intentDispositivo)
            autoCompleteDispositivo.setText("")

            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out)
        }

        binding.imagenAtrasMenuDispositive?.setOnClickListener {
            startActivity(Intent(this, ServicesMenu::class.java))

            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out)
        }
    }

    private fun loadData() {
        val itemDispositivo = ArrayList<ItemAdapterDispositive>()

        baseDatos
            .collection("Dispositivos")
            .get()
            .addOnCompleteListener { task ->
                for (document in task.result) {
                    itemDispositivo.add(ItemAdapterDispositive(document.id, "${document.getString("Calle")}, ${document.getString("Numero")}, ${document.getString("Localidad")}"))
                    listaDispositivos.add(document.id)
                }
                val adaptador = AdapterDispositive(itemDispositivo){
                    val intentDispositivo = Intent(this, VisualizeDispositive::class.java)
                    intentDispositivo.putExtra("Seleccionado", it.dispositivo)
                    startActivity(intentDispositivo)

                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out)
                }
                recyclerList.adapter = adaptador
                recyclerList.layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
            }
        val adaptadorAutoComplete = ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, listaDispositivos)
        autoCompleteDispositivo.setAdapter(adaptadorAutoComplete)
        autoCompleteDispositivo.threshold = 1
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, ServicesMenu::class.java))
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out)
    }
}