package com.example.appsimetria.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider

import com.example.appsimetria.R
import com.example.appsimetria.MainMenu
import com.example.appsimetria.databinding.ActivityMapsDeleteDeviceBinding
import com.example.appsimetria.requests.MainViewModel
import com.example.appsimetria.requests.MainViewModelFactory
import com.example.appsimetria.requests.models.DispositivoGetAll
import com.example.appsimetria.requests.repository.Repository

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*

import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.android.synthetic.main.custom_dialog_close.view.*
import kotlinx.android.synthetic.main.custom_toast_maps_add_1.*

/**
 * Clase exactamente igual que MapsAddDevice: Mismas funciones y métodos.
 */
class MapsDeleteDevice : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMarkerDragListener {

    private lateinit var mMap: GoogleMap
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var binding: ActivityMapsDeleteDeviceBinding

    private lateinit var viewModel: MainViewModel
    private lateinit var baseDatos: FirebaseFirestore

    private var seleccionadoCamara: Boolean = false
    private var latitudCamara: Double = 0.0
    private var longitudCamara: Double = 0.0

    private var seleccionado: String? = null
    private var latitudSeleccionado: Double = 0.0
    private var longitudSeleccionado: Double = 0.0

    private var arrayDispositivos: ArrayList<DispositivoGetAll> = arrayListOf()

    private val arrayMarker: ArrayList<Marker> = arrayListOf()
    private var latitud: Double = 0.0
    private var longitud: Double = 0.0

    companion object {
        private const val LOCATION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsDeleteDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        baseDatos = FirebaseFirestore.getInstance()
        baseDatos.firestoreSettings = FirebaseFirestoreSettings.Builder().build()

        val repository = Repository()
        val viewModelFactory = MainViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        seleccionado = intent.getStringExtra("Seleccionado")
        if (seleccionado != null) {
            latitudSeleccionado = intent.getStringExtra("Latitud")!!.toDouble()
            longitudSeleccionado = intent.getStringExtra("Longitud")!!.toDouble()
        }
        seleccionadoCamara = intent.getBooleanExtra("SeleccionadoCamara", false)
        if (seleccionadoCamara) {
            latitudCamara = intent.getStringExtra("LatCamara")!!.toDouble()
            longitudCamara = intent.getStringExtra("LonCamara")!!.toDouble()
        }

        Log.e("SELECCIONADO", seleccionado.toString())
        Log.e("SELECCIONADO", latitudSeleccionado.toString())
        Log.e("SELECCIONADO", longitudSeleccionado.toString())

        Log.e("CAMARA", seleccionadoCamara.toString())
        Log.e("LATCAMARA", latitudCamara.toString())
        Log.e("LONCAMARA", longitudCamara.toString())

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        try {
            val succes : Boolean = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this, R.raw.estilomapa
                ))
            if (!succes) {
                Log.e("MapsActivity", "Style pairsing failed")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("MapsActivity", "Can't find map style. Error: ", e)
        }

        with(mMap) {
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isMyLocationButtonEnabled = true
            uiSettings.isIndoorLevelPickerEnabled = true

            isTrafficEnabled = true

            setOnMyLocationButtonClickListener(this@MapsDeleteDevice)
            setOnMarkerClickListener(this@MapsDeleteDevice)
            setOnMarkerDragListener(this@MapsDeleteDevice)
        }

        with(binding){
            itemBotonDeleteMapsType.imagenMapsType.setOnClickListener {
                if (mMap.mapType == GoogleMap.MAP_TYPE_NORMAL)
                    mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                else
                    mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            }
            itemBotonDeleteMapsBack.imagenMapsBack.setOnClickListener {
                startActivity(Intent(this@MapsDeleteDevice, MainMenu::class.java))
            }
        }

        getAllDevices()
        setUpMap()
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                MapsDeleteDevice.LOCATION_REQUEST_CODE
            )
            return
        }
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->

            if (location != null) {
                lastLocation = location
                val currentLatLong = LatLng(location.latitude, location.longitude)

                if (seleccionado != null){
                    mMap
                        .animateCamera(CameraUpdateFactory
                            .newLatLngZoom(LatLng(latitudSeleccionado, longitudSeleccionado), 18f))
                } else if (seleccionadoCamara) {
                    mMap
                        .animateCamera(CameraUpdateFactory
                            .newLatLngZoom(LatLng(latitudCamara, longitudCamara), 15f))
                } else {
                    mMap
                        .animateCamera(CameraUpdateFactory
                            .newLatLngZoom(currentLatLong, 18f))
                }

                latitud = location.latitude
                longitud = location.longitude
            }
        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        mMap.animateCamera(CameraUpdateFactory
            .newLatLngZoom(LatLng(latitud, longitud), 18f))
        return true
    }

    override fun onMarkerClick(p0: Marker): Boolean {
        p0.showInfoWindow()
        return true
    }

    override fun onMarkerDrag(p0: Marker) {}

    override fun onMarkerDragEnd(p0: Marker) {}

    /**
     * LayoutInflater: Cuando el usuario mantiene apretado sobre un Marker. Aparece un AlertDialog informando de que se procederá a eliminar dicho dispositivo.
     */
    @SuppressLint("InflateParams")
    override fun onMarkerDragStart(p0: Marker) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog_close, null)
        val builderDialogView = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
        val alertDialog = builderDialogView.show()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogView.custom_text_decline.setOnClickListener {
            alertDialog.dismiss()
            rechargeMap(p0.position.latitude, p0.position.longitude)
        }

        dialogView.custom_text_accept.setOnClickListener {
            arrayDispositivos.forEach {
                if (p0.title.toString() == it.mac) {
                    viewModel.postDeleteDevice(it.dispositivo.toString())
                    viewModel.postResponse.observe(this, androidx.lifecycle.Observer { response ->
                        Log.e("Main", response.status.toString())
                        Log.e("Main", response.title)
                        Log.e("Main", response.message)
                    })
                }
            }
            rechargeMap(p0.position.latitude, p0.position.longitude)
            alertDialog.dismiss()
            toastPersonalizadoDeleteMaps1()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        startActivity(Intent(this, MainMenu::class.java))
    }

    private fun placeMarkerOnMap(title: String, currentLatLong: LatLng) {
        val marker: Marker? = mMap.addMarker(MarkerOptions()
            .title(title)
            .flat(false)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            .position(currentLatLong)
            .draggable(true)
            .visible(true))
        marker!!.showInfoWindow()

        arrayMarker.add(marker)
        //moveCameraToMarker()
    }

    private fun toastPersonalizadoDeleteMaps1() {
        val layoutToast =  layoutInflater.inflate(R.layout.custom_toast_maps_delete_1, constraintToastMaps1)
        Toast(this).apply {
            duration = Toast.LENGTH_SHORT
            setGravity(Gravity.BOTTOM, 0, 90)
            view = layoutToast
        }.show()
    }

    private fun rechargeMap(latitud: Double, longitud: Double) {
        mMap.clear()
        val intentCamara = Intent(this, this::class.java)
        intentCamara.putExtra("SeleccionadoCamara", true)
        intentCamara.putExtra("LatCamara", latitud.toString())
        intentCamara.putExtra("LonCamara", longitud.toString())
        startActivity(intentCamara)
    }

    private fun getAllDevices() {
        viewModel.getAllDevices()
        viewModel.getResponse.observe(this, androidx.lifecycle.Observer { response ->

            response.result.forEach {
                arrayDispositivos.add(it)
            }
            arrayDispositivos.forEach {
                placeMarkerOnMap(it.mac, LatLng(it.latitud.toDouble(), it.longitud.toDouble()))
            }
        })
    }
}

/**
 * FIREBASE FUNCTIONS
 */

    /**@SuppressLint("SetTextI18n")
    private fun loadDispositivo() {
        baseDatos
            .collection("Dispositivos")
            .get()
            .addOnSuccessListener { documents ->
                try {
                    if (documents != null) {
                        for (document in documents)
                            document.getString("ID")?.let {
                                placeMarkerOnMap(it, LatLng(
                                    (document
                                        .getString("Latitud")!!.toDouble()),
                                    (document
                                        .getString("Longitud")!!.toDouble())
                                ))
                            }
                    } else {
                        Toast.makeText(this, "No document found", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.message?.let { Log.e(TAG, it) }
                }
            }
            .addOnFailureListener {
                    e -> Log.e(TAG, "Error writing the document", e)
            }
    }*/

    /**
    baseDatos
        .collection("Dispositivos")
        .document(p0.title.toString())
        .delete()
        .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!")
        toastPersonalizadoDeleteMaps1()}
        .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
     */

    /**
    private fun moveCameraToMarker() {
        val seleccionado = intent.extras!!.getString("seleccionado")
        for (i in 0 until arrayMarker.size) {
            if (arrayMarker[i].title == seleccionado) {
                mMap
                    .animateCamera(CameraUpdateFactory
                    .newLatLngZoom(arrayMarker[i].position, 18f))
            } else {
                mMap
                    .animateCamera(CameraUpdateFactory
                    .newLatLngZoom(LatLng(lastLocation.latitude, lastLocation.longitude), 18f))
            }
        }
    }
     */