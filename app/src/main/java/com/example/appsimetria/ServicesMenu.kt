package com.example.appsimetria

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.appsimetria.auth.Login
import com.example.appsimetria.databinding.ActivityServicesMenuBinding
import com.example.appsimetria.dispositives.MenuDispositive
import com.example.appsimetria.dispositives.VisualizeDispositive
import com.example.appsimetria.maps.DeleteDispositiveMaps
import com.example.appsimetria.maps.NewDispositiveMaps
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_services_menu.*
import kotlinx.android.synthetic.main.custom_toast_opciones_1.*
import kotlinx.android.synthetic.main.custom_toast_opciones_2.*
import kotlinx.android.synthetic.main.item_principal_add_dispositive.view.*
import kotlinx.android.synthetic.main.item_principal_add_dispositive.view.hourCard
import kotlinx.android.synthetic.main.item_principal_add_dispositive.view.textDispositive
import kotlinx.android.synthetic.main.item_principal_delete_dispositive.view.*
import kotlinx.android.synthetic.main.item_principal_menu_dispositive.*
import kotlinx.android.synthetic.main.item_principal_menu_dispositive.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class ServicesMenu : AppCompatActivity() {
    private lateinit var resultScanner: String
    private lateinit var baseDatos: FirebaseFirestore
    private lateinit var binding: ActivityServicesMenuBinding

    private var latitud: Double = 0.0
    private var longitud: Double = 0.0
    private var listaDispositivos: HashMap<String, Any> = hashMapOf()

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServicesMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        baseDatos = FirebaseFirestore.getInstance()
        baseDatos.firestoreSettings = FirebaseFirestoreSettings.Builder().build()

        startFunctions()

        binding.imagenAtras.setOnClickListener {
            val rotar = getDrawable(R.drawable.ad_rotation) as AnimatedVectorDrawable
            imagenAtras.setImageDrawable(rotar)
            rotar.start()

            startActivity(Intent(this, Login::class.java))
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out)
        }

        binding.imagenMenu.setOnClickListener {
            val showPopUp = PopupMenu(this, imagenMenu)
            showPopUp.menuInflater.inflate(R.menu.menu, showPopUp.menu)

            showPopUp.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.itemMenu1 -> Toast.makeText(this, "Primer Item del Menu", Toast.LENGTH_SHORT).show()
                    R.id.itemMenu2 -> Toast.makeText(this, "Segundo Item del Menu", Toast.LENGTH_SHORT).show()
                    else -> Toast.makeText(this, "Tercer Item del Menu", Toast.LENGTH_SHORT).show()
                }
                true
            }
            showPopUp.show()
        }

        binding.cardEscaneoImagen.setOnClickListener {
            initScanner()
        }

        binding.includeAddDispositives.cardAddClickable.setOnClickListener {
            saveDataAdd(resultScanner, getCurrentDate())
            loadAllData()

            startActivity(Intent(this, NewDispositiveMaps::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right)
        }

        binding.includeDeleteDispositives.cardDeleteClickable.setOnClickListener {
            saveDataDelete(resultScanner, getCurrentDate())
            loadAllData()

            startActivity(Intent(this, DeleteDispositiveMaps::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right)
        }

        binding.includeMenuDispositives.cardMenuClickable.setOnClickListener {
            saveDataModify(resultScanner, getCurrentDate())
            //loadAllData()

            startActivity(Intent(this, MenuDispositive::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right)
        }

        binding.includeVisualizeDispositives.cardVisualizeClickable.setOnClickListener {
            startActivity(Intent(this, VisualizeDispositive::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right)
        }
    }

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

    private fun startFunctions() {
        window.statusBarColor = ContextCompat.getColor(this, R.color.primary)

        loadData()
        loadAllData()
        loadLatLngData()
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

        baseDatos
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
            }
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

        binding.includeAddDispositives.textDispositive.text = sharedPreferencesAdd.getString("add", null).toString()
        binding.includeAddDispositives.textLastModificationAdd.text = sharedPreferencesAdd.getString("date", null)
        binding.includeAddDispositives.hourCard.text = getCurrentTime24HFormat()

        binding.includeDeleteDispositives.textDispositive.text = sharedPreferencesDelete.getString("delete", null).toString()
        binding.includeDeleteDispositives.textLastModificationDelete.text = sharedPreferencesDelete.getString("date", null)
        binding.includeDeleteDispositives.hourCard.text = getCurrentTime24HFormat()

        binding.includeMenuDispositives.hourCard.text = getCurrentTime24HFormat()

        binding.includeVisualizeDispositives.hourCard.text = getCurrentTime24HFormat()
        //textoUltimoEscaneoModify.text = sharedPreferencesModify.getString("modify", null).toString()
        //textoDiaModify.text = sharedPreferencesModify.getString("date", null)
    }

    private fun loadLatLngData() {
        val sharedPreferences = getSharedPreferences("LatLng", Context.MODE_PRIVATE)

        latitud = sharedPreferences.getFloat("latitud", 0f).toDouble()
        longitud = sharedPreferences.getFloat("longitud", 0f).toDouble()
    }

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
}