package com.example.appsimetria.dispositives.visualize

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.appsimetria.R
import com.example.appsimetria.MainMenu
import com.example.appsimetria.databinding.ActivityVisualizeDispositiveBinding
import com.example.appsimetria.requests.MainViewModel
import com.example.appsimetria.requests.MainViewModelFactory
import com.example.appsimetria.requests.models.DispositivoGetAll
import com.example.appsimetria.requests.repository.Repository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.android.synthetic.main.activity_menu_device.*
import kotlinx.android.synthetic.main.custom_dialog_close.view.*
import kotlinx.android.synthetic.main.custom_dialog_edit.*
import kotlinx.android.synthetic.main.custom_dialog_edit.view.*
import kotlinx.android.synthetic.main.custom_toast_visualize_delete_1.*
import kotlinx.android.synthetic.main.custom_toast_visualize_delete_2.*
import java.util.*
import kotlin.collections.ArrayList

class VisualizeDevice : AppCompatActivity() {

    private lateinit var baseDatos: FirebaseFirestore
    private lateinit var binding: ActivityVisualizeDispositiveBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var geocoder: Geocoder

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
        binding.spinnerDispositivos.background.setColorFilter(resources.getColor(R.color.viewTop), PorterDuff.Mode.SRC_ATOP)
        window.statusBarColor = ContextCompat.getColor(this, R.color.viewTop)
        geocoder = Geocoder(applicationContext, Locale.getDefault())

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
                val dialogView = LayoutInflater.from(this@VisualizeDevice).inflate(R.layout.custom_dialog_close, null)
                val builderDialogView = android.app.AlertDialog.Builder(this@VisualizeDevice)
                    .setView(dialogView)
                val alertDialog = builderDialogView.show()
                alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                dialogView.custom_text_decline.setOnClickListener {
                    alertDialog.dismiss()
                }

                dialogView.custom_text_accept.setOnClickListener {
                    toastPersonalizadoDeleteVisualize1()
                    arrayDispositivos.forEach {
                        Log.e("AAAAAAAAAA", spinnerDispositivos.selectedItem.toString())
                        if (spinnerDispositivos.selectedItem.toString() == it.mac) {
                            viewModel.postDeleteDevice(it.dispositivo.toString())
                            viewModel.postResponse.observe(this@VisualizeDevice, androidx.lifecycle.Observer { response ->
                                Log.e("Main", response.status.toString())
                                Log.e("Main", response.title)
                                Log.e("Main", response.message)
                            })
                        }
                    }
                    finish()
                    startActivity(Intent(this@VisualizeDevice, this@VisualizeDevice::class.java))
                }
            }

            botonNaranja.setOnClickListener {
                loadDialogEdit()
            }

            spinnerDispositivos.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                @SuppressLint("SetTextI18n")
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    arrayDispositivos.forEach {
                        if (binding.spinnerDispositivos.selectedItem == it.mac) {
                            with(binding) {
                                dispositivoVisualize.text = it.mac

                                textoCalleVisualize.text = "${it.calle}, "
                                textoNumeroVisualize.text = it.numero
                                textoCodigoPostalVisualize.text = it.codigo_postal
                                textoPoblacionVisualize.text = it.poblacion_nombre
                                textoProvinciaVisualize.text = it.provincia_nombre

                                val latModifyHtml: String =
                                    "<b>" + "Latitud: " + "</b>" + it.latitud
                                latitudVisualize.text = Html.fromHtml(latModifyHtml)
                                val lonModifyHtml: String =
                                    "<b>" + "Longitud: " + "</b>" + it.longitud
                                longitudVisualize.text = Html.fromHtml(lonModifyHtml)

                                val fechaModifyHtml: String =
                                    "<b>" + "Fecha: " + "</b>" + it.fecha.substring(0, 10)
                                fechaVisualize.text = Html.fromHtml(fechaModifyHtml)

                                val horaModifyHtml: String =
                                    "<b>" + "Hora: " +"</b>" + it.fecha.substring(11, 19)
                                horaVisualize.text = Html.fromHtml(horaModifyHtml)

                                marcaDispositivoVisualize.text = it.marca
                                modeloDispositivoVisualize.text = it.modelo
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
        startActivity(Intent(this@VisualizeDevice, MainMenu::class.java))
    }

    private fun ActivityVisualizeDispositiveBinding.loadDialogEdit() {
        val dialogView = LayoutInflater.from(this@VisualizeDevice)
            .inflate(R.layout.custom_dialog_edit, null)
        val builderDialogView = AlertDialog.Builder(this@VisualizeDevice)
            .setView(dialogView)
        val alertDialog = builderDialogView.show()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        arrayDispositivos.forEach {
            if (binding.spinnerDispositivos.selectedItem.toString() == it.mac) {
                with(dialogView) {
                    dispositivoVisualize.text = it.mac
                    textoCalleVisualize.setText(it.calle)
                    textoNumeroVisualize.setText(it.numero)
                    textoCodigoPostalVisualize.setText(it.codigo_postal)
                    textoPoblacionVisualize.setText(it.poblacion_nombre)
                    textoProvinciaUpdate.setText(it.provincia_nombre)

                    latitudVisualize.text = it.latitud
                    longitudVisualize.text = it.longitud

                    fechaVisualize.text = it.fecha

                    marcaUpdate.text = it.marca
                    modeloUpdate.text = it.modelo
                }
            }
        }

        dialogView.botonVerdeDialog.setOnClickListener {
            arrayDispositivos.forEach {
                val listGeocoder: List<Address> =
                    geocoder.getFromLocation(it.latitud.toDouble(), it.longitud.toDouble(), 1)
                if (dispositivoVisualize.text.toString() == it.mac) {
                    with(dialogView) {
                        viewModel.postUpdateDevice(
                            "1",
                            dispositivoVisualize.text.toString(),
                            textoCalleVisualize.text.toString(),
                            textoCodigoPostalVisualize.text.toString(),
                            textoPoblacionVisualize.text.toString(),
                            it.fecha,
                            it.latitud.toDouble(),
                            it.longitud.toDouble(),
                            textoProvinciaUpdate.text.toString(),
                            it.marca,
                            it.modelo,
                            textoNumeroVisualize.text.toString(),
                            listGeocoder[0].countryName,
                            it.dispositivo.toString()
                        )
                        viewModel.postResponse.observe(this@VisualizeDevice, androidx.lifecycle.Observer { response ->
                            Log.i("PostUpdate", response.status.toString())
                            Log.i("PostUpdate", response.title)
                            Log.i("PostUpdate", response.message)
                            Log.i("PostUpdate", response.result.toString())
                        })
                    }
                }
            }
            toastPersonalizadoDeleteVisualize2()
            alertDialog.dismiss()
            finish()
            startActivity(Intent(this@VisualizeDevice, this@VisualizeDevice::class.java))
        }

        dialogView.botonRojoDialog.setOnClickListener {
            alertDialog.dismiss()
        }
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