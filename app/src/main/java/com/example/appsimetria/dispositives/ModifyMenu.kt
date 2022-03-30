package com.example.appsimetria.dispositives

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.example.appsimetria.R
import com.example.appsimetria.ServicesMenu
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.android.synthetic.main.activity_modify_menu.*

class ModifyMenu : AppCompatActivity() {

    private lateinit var baseDatos: FirebaseFirestore
    private var dispositivo: ArrayList<String> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_menu)

        baseDatos = FirebaseFirestore.getInstance()
        baseDatos.firestoreSettings = FirebaseFirestoreSettings.Builder().build()

        spinner_dispositivos.background.setColorFilter(resources.getColor(R.color.primary), PorterDuff.Mode.SRC_ATOP);

        baseDatos.collection("Dispositivos")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        dispositivo.add(document.id)
                    }
                    val adaptador = ArrayAdapter(this, R.layout.item_spinner, dispositivo)
                    adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner_dispositivos.adapter = adaptador
                }
            }

        imagenAtrasModify.setOnClickListener {
            startActivity(Intent(this, ServicesMenu::class.java))

            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        spinner_dispositivos.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            @SuppressLint("SetTextI18n")
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

                baseDatos
                    .collection("Dispositivos")
                    .document(spinner_dispositivos.selectedItem.toString())
                    .addSnapshotListener { value, error ->
                        dispositivoModify.text = value!!.id
                        val direccionCalle: String = value.getString("Calle") + ", " +
                                    value.getString("Numero") + ","
                                    textoCalle.text = direccionCalle

                        val direccionCodigoPostal: String = value.getString("Codigo Postal").toString()
                        textoCodigoPostal.text = direccionCodigoPostal

                        val direccionLocalidad: String = value.getString("Localidad").toString() + ","
                        textoLocalidad.text = direccionLocalidad

                        val direccionCiudad: String = value.getString("Ciudad").toString()
                        textoCiudad.text = direccionCiudad

                        val direccionComunidad = value.getString("Comunidad") + ","
                        textoComunidad.text = direccionComunidad

                        val direccionPais = value.getString("Pais")
                        textoPais.text = direccionPais

                        val latModifyHtml: String = "<b>" + "Latitud: " + "</b>" + value.getString("Latitud"); latModify.text = Html.fromHtml(latModifyHtml)
                        val lngModifyHtml: String = "<b>" + "Longitud: " + "</b>" + value.getString("Longitud"); lngModify.text = Html.fromHtml(lngModifyHtml)

                        val fechaModifyHtml: String = "<b>" + "Fecha: " + "</b>" + value.getString("Fecha"); fechaModify.text = Html.fromHtml(fechaModifyHtml)
                        val horaModifyHtml: String = "<b>" + "Hora: " + "</b>" + value.getString("Hora"); horaModify.text = Html.fromHtml(horaModifyHtml)
                    }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }
}