package com.example.appsimetria

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.util.Log
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
import kotlinx.android.synthetic.main.custom_toast_main_menu_1.*
import kotlinx.android.synthetic.main.custom_toast_opciones_2.*
import kotlinx.android.synthetic.main.item_principal_menu_device.*
import org.jetbrains.anko.toast

import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainMenu : AppCompatActivity() {

    private lateinit var resultScanner: String
    private lateinit var baseDatos: FirebaseFirestore
    private lateinit var binding: ActivityMainMenuBinding
    private lateinit var viewModel: MainViewModel

    private var escaneo: Boolean = false

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

            /**
             * M??todo de escaneo normal.
             * Tras el escaner el usuario permanecer?? en esta misma activity y se le informar?? de si el dispositivo escaneado existe en el servidor o no.
             */
            cardEscaneoImagen.setOnClickListener {
                initScanner()
            }

            /**
             * M??todo de escaneo r??pido.
             * Tras el escaner el usuario ser?? llevado a la Activity de Maps.
             */
            cardEscaneoRapido.setOnClickListener {
                escaneo = true
                initScanner()
                //startActivity(Intent(this@MainMenu, MapsAddDevice::class.java))
            }

            /**
             * Activity de Maps donde el usuario podr?? a??adir un dispositivo m??s preciso con el escaneo normal.
             */
            includeAddDevice.cardAddClickable.setOnClickListener {
                saveDataAdd(resultScanner, getCurrentDate())
                loadAllData()

                startActivity(Intent(this@MainMenu, MapsAddDevice::class.java))
                transition()
            }

            /**
             * Activity de DeleteMaps donde el usuario podr?? observar todos los dispositivos disponibles en el servidor.
             */
            includeDeleteDevice.cardDeleteClickable.setOnClickListener {
                saveDataDelete(resultScanner, getCurrentDate())
                loadAllData()

                startActivity(Intent(this@MainMenu, MapsDeleteDevice::class.java))
                transition()
            }

            /**
             * Men?? donde se listan todos los dispositivos y se podr?? acceder a la informaci??n detallada de cada uno o situarlo en el mapa
             */
            includeMenuDevice.cardMenuClickable.setOnClickListener {
                saveDataModify(resultScanner, getCurrentDate())
                //loadAllData()

                startActivity(Intent(this@MainMenu, DeviceMenu::class.java))
                transition()
            }

            /**
             * Activity de informaci??n detallada de cada dispositivo.
             */
            includeVisualizeDevice.cardVisualizeClickable.setOnClickListener {
                startActivity(Intent(this@MainMenu, VisualizeDevice::class.java))
                transition()
            }


            /**
             * BLE Adapter para detecci??n de dispositivos y posterior transmisi??n de datos rwx
             */
            includeBLEDevice.cardBleClickable.setOnClickListener {
                startActivity(Intent(this@MainMenu, AdapterBLE::class.java))
                transition()
            }
        }
    }

    /**
     * OVERRIDES
     * onActivityResult: Lanzar?? directamente a la c??mara debido a la implementaci??n del Escaner ZXing y posteriormente evaluar?? si ha sido efectuado el escaneo r??pido o el normal.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                toastPersonalizadoMainMenu1()
            } else {
                Toast.makeText(this, "Dispositivo Escaneado: " + result.contents.substring(0, result.contents.length - 4), Toast.LENGTH_LONG).show()
                Log.e("Escaneo Valor", escaneo.toString())
                if (escaneo) {
                    saveDataAdd(resultScanner, getCurrentDate())
                    loadAllData()

                    startActivity(Intent(this@MainMenu, MapsAddDevice::class.java))
                    transition()
                } else {
                    listaDispositivos.forEach {
                        if (result.contents.substring(0, result.contents.length - 4) == it.mac) {
                            Toast.makeText(this, "Dispositivo existente", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "Dispositivo inexistente", Toast.LENGTH_LONG).show()
                        }
                    }

                    resultScanner = result.contents.substring(0, result.contents.length - 4)
                    saveData(resultScanner)
                    loadData()
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    /**
     * Al volver a cargar en memoria esta Activity.
     */
    override fun onResume() {
        super.onResume()
        !escaneo
    }

    /**
     * Cuando el usuario pulsa de la BottomNavigation el bot??n de volve hacia atr??s.
     */
    override fun onBackPressed() {
        super.onBackPressed()

        finish()
        startActivity(Intent(this, Login::class.java))
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out)
    }

    /**
     * FUNCTIONS
     * Al iniciar esta misma Activity.
     */
    private fun startFunctions() {
        window.statusBarColor = ContextCompat.getColor(this, R.color.viewTop)

        loadData()
        loadAllData()
        getAllDevices()
    }

    /**
     * Llamada @GET para cargar todos los dispositivos desde el servidor en un ArrayList
     */
    private fun getAllDevices() {
        viewModel.getAllDevices()
        viewModel.getResponse.observe(this, androidx.lifecycle.Observer { result ->
            result.result.forEach {
                listaDispositivos.add(it)
            }
            binding.includeMenuDevice.textDispositives.text = "N??mero de dispositivos: ${listaDispositivos.size}"
        })
    }

    private fun transition() {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right)
    }

    /**
     * Iniciar el Escaner con sus par??metros.
     */
    private fun initScanner(){
        val integrator = IntentIntegrator(this)
        integrator.setPrompt("ESCANEA EL DISPOSITIVO DESEADO")
        integrator.setTimeout(15000)
        integrator.setBeepEnabled(false)
        integrator.initiateScan()
    }

    /**
     * SaveData de los ajustes.
     */
    @SuppressLint("CommitPrefEdits")
    private fun saveData(datos: String){
        val sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        editor.putString("datos", datos)
        editor.apply()
    }

    /**
     * Cargar los datos del escaneo activo (MaterialCardView del layout)
     */
    @SuppressLint("SetTextI18n")
    private fun loadData() {
        val sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)

        resultScanner = sharedPreferences.getString("datos", null).toString()
        textoEscaneoActivo.text = ("DISPOSITIVO ACTUAL: $resultScanner")
    }

    /**
     * Guardar los datos de ??ltimo dispositivo A??adido en local
     */
    private fun saveDataAdd(datos: String, date: String){
        val sharedPreferences = getSharedPreferences("Add", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        editor.putString("add", datos)
        editor.putString("date", date)
        editor.apply()
    }

    /**
     * Guardar los datos de ??ltimo dispositivo Eliminado en local
     */
    private fun saveDataDelete(datos: String, date: String){
        val sharedPreferences = getSharedPreferences("Delete", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        editor.putString("delete", datos)
        editor.putString("date", date)
        editor.apply()
    }

    /**
     * Guardar los datos de ??ltimo dispositivo Modificado en local
     */
    private fun saveDataModify(datos: String, date: String){
        val sharedPreferences = getSharedPreferences("Modify", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        editor.putString("modify", datos)
        editor.putString("date", date)
        editor.apply()
    }

    /**
     * Cargar todos los datos anteriormente guardados.
     */
    @SuppressLint("SetTextI18n")
    private fun loadAllData() {
        val sharedPreferencesAdd = getSharedPreferences("Add", Context.MODE_PRIVATE)
        val sharedPreferencesDelete = getSharedPreferences("Delete", Context.MODE_PRIVATE)
        val sharedPreferencesModify = getSharedPreferences("Modify", Context.MODE_PRIVATE)

        with(binding){
            includeAddDevice.textDispositive.text = sharedPreferencesAdd.getString("add", null).toString()
            includeAddDevice.textLastModificationAdd.text = sharedPreferencesAdd.getString("date", null)

            includeDeleteDevice.textDispositive.text = sharedPreferencesDelete.getString("delete", null).toString()
            includeDeleteDevice.textLastModificationDelete.text = sharedPreferencesDelete.getString("date", null)

            //includeAddDispositives.hourCard.text = getCurrentTime24HFormat()
            //includeDeleteDispositives.hourCard.text = getCurrentTime24HFormat()
            //includeMenuDispositives.hourCard.text = getCurrentTime24HFormat()
            //includeVisualizeDispositives.hourCard.text = getCurrentTime24HFormat()

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
    private fun toastPersonalizadoMainMenu1() {
        val layoutToast =  layoutInflater.inflate(R.layout.custom_toast_main_menu_1, constraintToastDevicesAdapter1)
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