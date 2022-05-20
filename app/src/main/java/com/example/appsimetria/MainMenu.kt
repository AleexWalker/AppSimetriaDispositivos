package com.example.appsimetria

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider

import com.example.appsimetria.auth.Login
import com.example.appsimetria.bluetooth.AdapterBLE
import com.example.appsimetria.databinding.ActivityMainMenuBinding
import com.example.appsimetria.dispositives.DeviceMenu
import com.example.appsimetria.dispositives.mapsinfo.DispositiveMapsInfo
import com.example.appsimetria.dispositives.visualize.VisualizeDevice
import com.example.appsimetria.maps.MapsAddDevice
import com.example.appsimetria.maps.MapsDeleteDevice
import com.example.appsimetria.requests.MainViewModel
import com.example.appsimetria.requests.MainViewModelFactory
import com.example.appsimetria.requests.models.DispositivoGetAll
import com.example.appsimetria.requests.repository.Repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.zxing.integration.android.IntentIntegrator

import kotlinx.android.synthetic.main.activity_main_menu.*
import kotlinx.android.synthetic.main.custom_toast_opciones_1.*
import kotlinx.android.synthetic.main.custom_toast_opciones_2.*
import kotlinx.android.synthetic.main.item_principal_menu_dispositive.*

import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainMenu : AppCompatActivity() {

    private lateinit var resultScanner: String
    private lateinit var baseDatos: FirebaseFirestore
    private lateinit var binding: ActivityMainMenuBinding
    private lateinit var viewModel: MainViewModel

    private var listaDispositivos: ArrayList<DispositivoGetAll> = arrayListOf()

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        baseDatos = FirebaseFirestore.getInstance()
        baseDatos.firestoreSettings = FirebaseFirestoreSettings.Builder().build()

        val repository = Repository()
        val viewModelFactory = MainViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        startFunctions()

        with(binding) {
            imagenAtras.setOnClickListener {
                val rotar = getDrawable(R.drawable.ad_rotation) as AnimatedVectorDrawable
                imagenAtras.setImageDrawable(rotar)
                rotar.start()

                startActivity(Intent(this@MainMenu, Login::class.java))
                transition()
            }

            imagenMenu.setOnClickListener {
                val showPopUp = PopupMenu(this@MainMenu, imagenMenu)
                showPopUp.menuInflater.inflate(R.menu.menu, showPopUp.menu)

                showPopUp.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.itemMenu1 -> Toast.makeText(this@MainMenu, "Primer Item del Menu", Toast.LENGTH_SHORT).show()
                        R.id.itemMenu2 -> Toast.makeText(this@MainMenu, "Segundo Item del Menu", Toast.LENGTH_SHORT).show()
                        else -> Toast.makeText(this@MainMenu, "Tercer Item del Menu", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                showPopUp.show()
            }

            cardEscaneoImagen.setOnClickListener {
                initScanner()
            }

            includeAddDispositives.cardAddClickable.setOnClickListener {
                saveDataAdd(resultScanner, getCurrentDate())
                loadAllData()

                startActivity(Intent(this@MainMenu, MapsAddDevice::class.java))
                transition()
            }

            includeDeleteDispositives.cardDeleteClickable.setOnClickListener {
                saveDataDelete(resultScanner, getCurrentDate())
                loadAllData()

                startActivity(Intent(this@MainMenu, MapsDeleteDevice::class.java))
                transition()
            }

            includeMenuDispositives.cardMenuClickable.setOnClickListener {
                saveDataModify(resultScanner, getCurrentDate())
                //loadAllData()

                startActivity(Intent(this@MainMenu, DeviceMenu::class.java))
                transition()
            }

            includeVisualizeDispositives.cardVisualizeClickable.setOnClickListener {
                startActivity(Intent(this@MainMenu, VisualizeDevice::class.java))
                transition()
            }

            includeBLEDispositives.cardBleClickable.setOnClickListener {
                startActivity(Intent(this@MainMenu, AdapterBLE::class.java))
                transition()
            }

            logoSimetria.setOnClickListener {
                startActivity(Intent(this@MainMenu, DispositiveMapsInfo::class.java))
                transition()
            }
        }
    }

    /**
     * OVERRIDES
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                toastPersonalizadoOpciones1()
            } else {
                Toast.makeText(this, "Dispositivo Escaneado: " + result.contents.substring(0, result.contents.length - 4), Toast.LENGTH_LONG).show()

                resultScanner = result.contents.substring(0, result.contents.length - 4)
                saveData(resultScanner)
                loadData()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        finish()
        startActivity(Intent(this, Login::class.java))
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out)
    }

    /**
     * FUNCTIONS
     */
    private fun startFunctions() {
        window.statusBarColor = ContextCompat.getColor(this, R.color.primary)

        loadData()
        loadAllData()
        getAllDevices()
    }

    private fun getAllDevices() {
        viewModel.getAllDevices()
        viewModel.getResponse.observe(this, androidx.lifecycle.Observer { result ->
            result.result.forEach {
                listaDispositivos.add(it)
            }
            binding.includeMenuDispositives.textDispositives.text = "Número de dispositivos: ${listaDispositivos.size}"
        })
    }

    private fun transition() {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right)
    }

    private fun initScanner(){
        val integrator = IntentIntegrator(this)
        integrator.setPrompt("ESCANEA EL DISPOSITIVO DESEADO")
        integrator.setTimeout(15000)
        integrator.setBeepEnabled(false)
        integrator.initiateScan()
    }

    @SuppressLint("CommitPrefEdits")
    private fun saveData(datos: String){
        val sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        editor.putString("datos", datos)
        editor.apply()
    }

    @SuppressLint("SetTextI18n")
    private fun loadData() {
        val sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)

        resultScanner = sharedPreferences.getString("datos", null).toString()
        textoEscaneoActivo.text = ("DISPOSITIVO EN MEMORIA: $resultScanner")
    }

    private fun saveDataAdd(datos: String, date: String){
        val sharedPreferences = getSharedPreferences("Add", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        editor.putString("add", datos)
        editor.putString("date", date)
        editor.apply()
    }

    private fun saveDataDelete(datos: String, date: String){
        val sharedPreferences = getSharedPreferences("Delete", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        editor.putString("delete", datos)
        editor.putString("date", date)
        editor.apply()
    }

    private fun saveDataModify(datos: String, date: String){
        val sharedPreferences = getSharedPreferences("Modify", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        editor.putString("modify", datos)
        editor.putString("date", date)
        editor.apply()
    }

    @SuppressLint("SetTextI18n")
    private fun loadAllData() {
        val sharedPreferencesAdd = getSharedPreferences("Add", Context.MODE_PRIVATE)
        val sharedPreferencesDelete = getSharedPreferences("Delete", Context.MODE_PRIVATE)
        val sharedPreferencesModify = getSharedPreferences("Modify", Context.MODE_PRIVATE)

        with(binding){
            includeAddDispositives.textDispositive.text =
                sharedPreferencesAdd.getString("add", null).toString()
            includeAddDispositives.textLastModificationAdd.text =
                sharedPreferencesAdd.getString("date", null)
            includeAddDispositives.hourCard.text = getCurrentTime24HFormat()

            includeDeleteDispositives.textDispositive.text =
                sharedPreferencesDelete.getString("delete", null).toString()
            includeDeleteDispositives.textLastModificationDelete.text =
                sharedPreferencesDelete.getString("date", null)
            includeDeleteDispositives.hourCard.text = getCurrentTime24HFormat()

            includeMenuDispositives.hourCard.text = getCurrentTime24HFormat()

            includeVisualizeDispositives.hourCard.text = getCurrentTime24HFormat()
            //textoUltimoEscaneoModify.text = sharedPreferencesModify.getString("modify", null).toString()
            //textoDiaModify.text = sharedPreferencesModify.getString("date", null)
        }
    }

    /**
     * DATES
     */
    private fun getCurrentDate(): String {
        return SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
    }

    private fun getCurrentTime(): String {
        return SimpleDateFormat("EEEE, HH:mm", Locale.getDefault()).format(Date())
    }

    private fun getCurrentTime24HFormat(): String {
        val output = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val date = Date()
        return output.format(date)
    }

    /**
     * CUSTOM TOASTS
     */
    private fun toastPersonalizadoOpciones1() {
        val layoutToast =  layoutInflater.inflate(R.layout.custom_toast_opciones_1, constraintToastOpciones1)
        Toast(this).apply {
            duration = Toast.LENGTH_SHORT
            setGravity(Gravity.BOTTOM, 0, 100)
            view = layoutToast
        }.show()
    }

    private fun toastPersonalizadoOpciones2() {
        val layoutToast =  layoutInflater.inflate(R.layout.custom_toast_opciones_2, constraintToastOpciones2)

        Toast(this).apply {
            duration = Toast.LENGTH_SHORT
            setGravity(Gravity.BOTTOM, 0, 200)
            view = layoutToast
        }.show()
    }
}

/**
 * FIREBASE FUNCTIONS
 */

    /**baseDatos
    .collection("Dispositivos")
    .get()
    .addOnSuccessListener { documents ->
        if(documents != null){
            for(document in documents) {
                listaDispositivos[document.id] = document.id
            }
            binding.includeMenuDispositives.textDispositives.text = "Numero de dispositivos: ${listaDispositivos.size}"
        }
    }
    .addOnFailureListener {
        Log.e(TAG, "Document not found")
    }*/