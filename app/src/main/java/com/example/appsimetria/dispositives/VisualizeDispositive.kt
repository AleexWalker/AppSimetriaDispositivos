package com.example.appsimetria.dispositives

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.appsimetria.R
import com.example.appsimetria.ServicesMenu
import com.example.appsimetria.databinding.ActivityVisualizeDispositiveBinding
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.android.synthetic.main.activity_visualize_dispositive.*
import kotlinx.android.synthetic.main.custom_dialog_edit.view.*
import kotlinx.android.synthetic.main.custom_toast_visualize_delete_1.*
import kotlinx.android.synthetic.main.custom_toast_visualize_delete_2.*

class VisualizeDispositive : AppCompatActivity() {

    private lateinit var baseDatos: FirebaseFirestore
    private lateinit var binding: ActivityVisualizeDispositiveBinding

    private var hashDispositivos: HashMap<String, Any> = hashMapOf()
    private var dispositivo: ArrayList<String> = arrayListOf()
    private var seleccionado: String? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVisualizeDispositiveBinding.inflate(layoutInflater)
        setContentView(binding.root)
        baseDatos = FirebaseFirestore.getInstance()
        baseDatos.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
        spinner_dispositivos.background.setColorFilter(resources.getColor(R.color.primary), PorterDuff.Mode.SRC_ATOP);

        loadAdapter()

        imagenAtrasModify.setOnClickListener {
            startActivity(Intent(this, ServicesMenu::class.java))

            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out)
        }

        binding.botonRojo.setOnClickListener {
            baseDatos
                .collection("Dispositivos")
                .document(binding.spinnerDispositivos.selectedItem.toString())
                .delete()
                .addOnSuccessListener { Log.d(ContentValues.TAG, "DocumentSnapshot successfully deleted!")
                    toastPersonalizadoDeleteVisualize1()}
                .addOnFailureListener { e -> Log.w(ContentValues.TAG, "Error deleting document", e) }

            finish()
            startActivity(intent)
        }

        binding.botonNaranja.setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog_edit, null)
            val builderDialogView = android.app.AlertDialog.Builder(this)
                .setView(dialogView)
            val alertDialog = builderDialogView.show()
            alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            setDialogViews(dialogView)

            dialogView.botonVerdeDialog.setOnClickListener {
                alertDialog.dismiss()
                saveDialogData(dialogView)

                finish()
                startActivity(intent)
            }

            dialogView.botonRojoDialog.setOnClickListener {
                alertDialog.dismiss()
            }
        }

        spinner_dispositivos.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            @SuppressLint("SetTextI18n")
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                baseDatos
                    .collection("Dispositivos")
                    .document(spinner_dispositivos.selectedItem.toString())
                    .addSnapshotListener { value, error ->
                        if (value != null) {
                            loadFirebaseData(value)
                        }
                    }
            }

            private fun loadFirebaseData(value: DocumentSnapshot) {
                hashDispositivos["Calle"] = value!!.getString("Calle").toString()
                hashDispositivos["Numero"] = value.getString("Numero").toString()
                hashDispositivos["Codigo Postal"] = value.getString("Codigo Postal").toString()
                hashDispositivos["Localidad"] = value.getString("Localidad").toString()
                hashDispositivos["Ciudad"] = value.getString("Ciudad").toString()

                hashDispositivos["Latitud"] = value.getString("Latitud").toString()
                hashDispositivos["Longitud"] = value.getString("Longitud").toString()

                hashDispositivos["Hora"] = value.getString("Hora").toString()
                hashDispositivos["Fecha"] = value.getString("Fecha").toString()

                hashDispositivos["Marca"] = value.getString("Marca").toString()
                hashDispositivos["Modelo"] = value.getString("Modelo").toString()

                dispositivoModify.text = value.id
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

                val latModifyHtml: String =
                    "<b>" + "Latitud: " + "</b>" + value.getString("Latitud"); latModify.text =
                    Html.fromHtml(latModifyHtml)
                val lngModifyHtml: String =
                    "<b>" + "Longitud: " + "</b>" + value.getString("Longitud"); lngModify.text =
                    Html.fromHtml(lngModifyHtml)

                val fechaModifyHtml: String =
                    "<b>" + "Fecha: " + "</b>" + value.getString("Fecha"); fechaModify.text =
                    Html.fromHtml(fechaModifyHtml)
                val horaModifyHtml: String =
                    "<b>" + "Hora: " + "</b>" + value.getString("Hora"); horaModify.text =
                    Html.fromHtml(horaModifyHtml)

                val marcaTelefonoHtml: String =
                    "<b>" + "Marca: " + "</b>" + value.getString("Marca"); phoneManufacturer.text =
                    Html.fromHtml(marcaTelefonoHtml)
                val modeloTelefonoHtml: String =
                    "<b>" + "Modelo: " + "</b>" + value.getString("Modelo"); phoneModel.text =
                    Html.fromHtml(modeloTelefonoHtml)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }

    private fun saveDialogData(dialogView: View) {
        hashDispositivos["Calle"] = dialogView.textoCalle.text.toString()
        hashDispositivos["Numero"] = dialogView.textoNumero.text.toString()
        hashDispositivos["Codigo Postal"] = dialogView.textoCodigoPostal.text.toString()
        hashDispositivos["Localidad"] = dialogView.textoLocalidad.text.toString()
        hashDispositivos["Ciudad"] = dialogView.textoCiudad.text.toString()

        hashDispositivos["Latitud"] = dialogView.latModify.text.toString()
        hashDispositivos["Longitud"] = dialogView.lngModify.text.toString()

        baseDatos
            .collection("Dispositivos")
            .document(spinner_dispositivos.selectedItem.toString())
            .update(hashDispositivos)
            .addOnSuccessListener { Log.d(ContentValues.TAG, "Successfly written") ; toastPersonalizadoDeleteVisualize2() }
            .addOnFailureListener { Log.w(ContentValues.TAG, "Failed to be written!") }
    }

    @SuppressLint("SetTextI18n")
    private fun setDialogViews(dialogView: View) {
        dialogView.dispositivoModify.text = spinner_dispositivos.selectedItem.toString()

        dialogView.textoCalle.setText(hashDispositivos["Calle"].toString())
        dialogView.textoNumero.setText(hashDispositivos["Numero"].toString())
        dialogView.textoCodigoPostal.setText(hashDispositivos["Codigo Postal"].toString())
        dialogView.textoLocalidad.setText(hashDispositivos["Localidad"].toString())
        dialogView.textoCiudad.setText(hashDispositivos["Ciudad"].toString())

        dialogView.latModify.setText(hashDispositivos["Latitud"].toString())
        dialogView.lngModify.setText(hashDispositivos["Longitud"].toString())

        dialogView.horaModify.text = "Hora: ${hashDispositivos["Hora"].toString()}"
        dialogView.fechaModify.text = "Fecha: ${hashDispositivos["Fecha"].toString()}"

        dialogView.phoneManufacturerEdicion.text = "Marca: ${hashDispositivos["Marca"].toString()}"
        dialogView.phoneModelEdicion.text = "Modelo: ${hashDispositivos["Modelo"].toString()}"
    }

    override fun onBackPressed() {
        super.onBackPressed()

        finish()
        startActivity(Intent(this, ServicesMenu::class.java))
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out)
    }

    private fun loadAdapter() {
        baseDatos
            .collection("Dispositivos")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        dispositivo.add(document.id)
                    }
                    val adaptador = ArrayAdapter(this, R.layout.item_spinner, dispositivo)
                    adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner_dispositivos.adapter = adaptador
                    spinner_dispositivos.setSelection(comprobacionSpinner())
                }
            }
    }

    private fun comprobacionSpinner(): Int{
        seleccionado = intent.getStringExtra("Seleccionado")
        return if (seleccionado == null) {
            0
        } else {
            dispositivo.indexOf(seleccionado)
        }
    }

    private fun toastPersonalizadoDeleteVisualize1() {
        val layoutToast =  layoutInflater.inflate(R.layout.custom_toast_visualize_delete_1, constraintToastVisualize1)
        Toast(this).apply {
            duration = Toast.LENGTH_SHORT
            setGravity(Gravity.BOTTOM, 0, 90)
            view = layoutToast
        }.show()
    }

    private fun toastPersonalizadoDeleteVisualize2() {
        val layoutToast =  layoutInflater.inflate(R.layout.custom_toast_visualize_delete_2, constraintToastVisualize2)
        Toast(this).apply {
            duration = Toast.LENGTH_SHORT
            setGravity(Gravity.BOTTOM, 0, 90)
            view = layoutToast
        }.show()
    }
}
