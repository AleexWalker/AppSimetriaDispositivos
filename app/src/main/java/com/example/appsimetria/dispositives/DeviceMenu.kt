package com.example.appsimetria.dispositives

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appsimetria.MainMenu
import com.example.appsimetria.R
import com.example.appsimetria.databinding.ActivityMenuDeviceBinding
import com.example.appsimetria.dispositives.visualize.VisualizeDevice
import com.example.appsimetria.requests.MainViewModel
import com.example.appsimetria.requests.MainViewModelFactory
import com.example.appsimetria.requests.models.DispositivoGetAll
import com.example.appsimetria.requests.repository.Repository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

/**
 * Clase para cargar todos los dispositivos del servidor en un RecyclerView donde se podrá proceder a la información detallada de cada dispositivo o acceder a la ubicación exacta de cualquiera de ellos.
 */
class DeviceMenu : AppCompatActivity() {

    private lateinit var baseDatos: FirebaseFirestore
    private lateinit var binding: ActivityMenuDeviceBinding
    private lateinit var viewModel: MainViewModel

    private var arrayDispositivos: ArrayList<DispositivoGetAll> = arrayListOf()
    private var macDispositivos: ArrayList<String> = arrayListOf()

    @SuppressLint("RtlHardcoded")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        baseDatos = FirebaseFirestore.getInstance()
        baseDatos.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
        window.statusBarColor = ContextCompat.getColor(this, R.color.viewTop)

        val repository = Repository()
        val viewModelFactory = MainViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        getAllDevices()

        binding.autoCompleteDispositivo.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            val intentDispositivo = Intent(this, VisualizeDevice::class.java)
            intentDispositivo.putExtra("Seleccionado", adapterView.getItemAtPosition(i).toString())
            startActivity(intentDispositivo)
            binding.autoCompleteDispositivo.setText("")

            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out)
        }

        binding.imagenAtrasMenuDispositive?.setOnClickListener {
            startActivity(Intent(this, MainMenu::class.java))

            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out)
        }
    }

    private fun getAllDevices() {
        val itemDispositivo = ArrayList<DeviceAdapterItem>()

        with(binding){
            viewModel.getAllDevices()
            viewModel.getResponse.observe(this@DeviceMenu, Observer { result ->
                result.result.forEach {
                    arrayDispositivos.add(it)
                    macDispositivos.add(it.mac)
                    itemDispositivo.add(DeviceAdapterItem(it.mac, "${it.calle}, ${it.numero}, ${it.poblacion_nombre}", it.latitud, it.longitud))

                    val adaptador = DeviceAdapter(itemDispositivo) {
                        val intentSeleccionado = Intent(this@DeviceMenu, VisualizeDevice::class.java)
                        intentSeleccionado.putExtra("Seleccionado", it.dispositivo)
                        startActivity(intentSeleccionado)

                        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out)
                    }
                    recyclerList.adapter = adaptador
                    recyclerList.layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
                }
                if (arrayDispositivos.isEmpty())
                    binding.textoDispositivo!!.visibility = View.VISIBLE
            })

            val adaptadorAutoComplete = ArrayAdapter<String>(this@DeviceMenu, android.R.layout.simple_dropdown_item_1line, macDispositivos)
            autoCompleteDispositivo.setAdapter(adaptadorAutoComplete)
            autoCompleteDispositivo.threshold = 1
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MainMenu::class.java))
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out)
    }
}

/**private fun loadData() {
    val itemDispositivo = ArrayList<ItemAdapterDispositive>()

        baseDatos
            .collection("Dispositivos")
            .get()
            .addOnCompleteListener { task ->
                for (document in task.result) {
                    itemDispositivo.add(ItemAdapterDispositive(document.id, "${document.getString("Calle")}, ${document.getString("Numero")}, ${document.getString("Localidad")}"))
                    macDispositivos.add(document.id)
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
    val adaptadorAutoComplete = ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, macDispositivos)
    autoCompleteDispositivo.setAdapter(adaptadorAutoComplete)
    autoCompleteDispositivo.threshold = 1
}*/