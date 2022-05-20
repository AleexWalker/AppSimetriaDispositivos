package com.example.appsimetria.dispositives.visualize

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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.appsimetria.R
import com.example.appsimetria.MainMenu
import com.example.appsimetria.databinding.ActivityVisualizeDispositiveBinding
import com.example.appsimetria.requests.MainViewModel
import com.example.appsimetria.requests.MainViewModelFactory
import com.example.appsimetria.requests.models.DispositivoGetAll
import com.example.appsimetria.requests.repository.Repository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.android.synthetic.main.activity_visualize_dispositive.*
import kotlinx.android.synthetic.main.custom_dialog_edit.view.*
import kotlinx.android.synthetic.main.custom_toast_visualize_delete_1.*
import kotlinx.android.synthetic.main.custom_toast_visualize_delete_2.*
import org.jetbrains.anko.internals.AnkoInternals.createAnkoContext

class VisualizeDevice : AppCompatActivity() {

    private lateinit var baseDatos: FirebaseFirestore
    private lateinit var binding: ActivityVisualizeDispositiveBinding
    private lateinit var viewModel: MainViewModel

    private var arrayDispositivos: ArrayList<DispositivoGetAll> = arrayListOf()
    private var macDispositivos: ArrayList<String> = arrayListOf()

    private var seleccionado: String? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVisualizeDispositiveBinding.inflate(layoutInflater)
        setContentView(binding.root)
        baseDatos = FirebaseFirestore.getInstance()
        baseDatos.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
        binding.spinnerDispositivos.background.setColorFilter(resources.getColor(R.color.primary), PorterDuff.Mode.SRC_ATOP);

        val repository = Repository()
        val viewModelFactory = MainViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        getAllDevices()

        with(binding) {

            imagenAtrasModify.setOnClickListener {
                startActivity(Intent(this@VisualizeDevice, MainMenu::class.java))

                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out)
            }

            botonRojo.setOnClickListener {
                toastPersonalizadoDeleteVisualize1()
                finish()
                startActivity(Intent(this@VisualizeDevice, this@VisualizeDevice::class.java))
            }

            botonNaranja.setOnClickListener {
                val dialogView = LayoutInflater.from(this@VisualizeDevice)
                    .inflate(R.layout.custom_dialog_edit, null)
                val builderDialogView = android.app.AlertDialog.Builder(this@VisualizeDevice)
                    .setView(dialogView)
                val alertDialog = builderDialogView.show()
                alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                //dialogView.

                dialogView.botonVerdeDialog.setOnClickListener {
                    alertDialog.dismiss()
                    finish()
                    startActivity(Intent(this@VisualizeDevice, this@VisualizeDevice::class.java))
                }

                dialogView.botonRojoDialog.setOnClickListener {
                    alertDialog.dismiss()
                }
            }

            spinnerDispositivos.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                @SuppressLint("SetTextI18n")
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    arrayDispositivos.forEach {
                        if (binding.spinnerDispositivos.selectedItem == it.mac) {
                            with(binding) {
                                dispositivoModify.text = it.mac

                                textoCalle.text = it.calle
                                textoCodigoPostal.text = it.codigo_postal
                                textoPoblacion.text = it.poblacion_nombre
                                textoProvincia.text = it.provincia_nombre

                                val latModifyHtml: String =
                                    "<b>" + "Latitud: " + "</b>" + it.latitud
                                latModify.text = Html.fromHtml(latModifyHtml)
                                val lonModifyHtml: String =
                                    "<b>" + "Longitud: " + "</b>" + it.longitud
                                lngModify.text = Html.fromHtml(lonModifyHtml)

                                val fechaModifyHtml: String =
                                    "<b>" + "Fecha: " + "</b>" + it.fecha.substring(0, 10)
                                fechaModify.text = Html.fromHtml(fechaModifyHtml)

                                val horaModifyHtml: String =
                                    "<b>" + "Hora: " +"</b>" + it.fecha.substring(11, 19)
                                horaModify.text = Html.fromHtml(horaModifyHtml)

                                marcaDispositivo.text = it.marca
                                modeloDispositivo.text = it.modelo
                            }
                        }
                    }
                }
                override fun onNothingSelected(p0: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        transition()
        finish()
    }

    private fun getAllDevices() {
        viewModel.getAllDevices()
        viewModel.getResponse.observe(this, Observer { result ->
            result.result.forEach {
                arrayDispositivos.add(it)
                macDispositivos.add(it.mac)
            }
            loadSpinnerAdapter()
        })
    }

    private fun loadSpinnerAdapter() {
        val adaptador = ArrayAdapter(this, R.layout.item_spinner, macDispositivos)
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        with(binding) {
            spinnerDispositivos.adapter = adaptador
            spinnerDispositivos.setSelection(comprobacionSpinner())
        }
    }

    private fun comprobacionSpinner(): Int{
        seleccionado = intent.getStringExtra("Seleccionado")
        return if (seleccionado == null) {
            0
        } else {
            macDispositivos.indexOf(seleccionado)
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

    private fun transition() {
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out)
    }
}
/**
spinnerDispositivos.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
    @SuppressLint("SetTextI18n")
    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        arrayDispositivos.forEach {
            if (binding.spinnerDispositivos.selectedItem == it.mac) {
                with(binding) {
                    textoCalle.text = it.calle
                    textoCodigoPostal.text = it.codigo_postal
                    textoLocalidad.text = it.poblacion_nombre
                    textoCiudad.text = it.provincia_nombre
                    textoComunidad.text = it.provincia_nombre

                    val latModifyHtml: String = "<b>" + "Latitud: " + "</b>" + it.latitud
                    latModify.text = android.text.Html.fromHtml(latModifyHtml)
                    val lonModifyHtml: String = "<b>" + "Longitud: " + "</b>" + it.longitud
                    latModify.text = android.text.Html.fromHtml(lonModifyHtml)

                    val fechaModifyHtml: String = "<b>" + "Fecha: " + "</b>" + it.fecha
                    fechaModify.text = android.text.Html.fromHtml(fechaModifyHtml)
                }
            }
        }
    }
    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
*/