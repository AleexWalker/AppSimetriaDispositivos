package com.example.appsimetria.maps

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.appsimetria.MainMenu
import com.example.appsimetria.R

import com.example.appsimetria.databinding.ActivityMapsAddDeviceBinding
import com.example.appsimetria.requests.MainViewModel
import com.example.appsimetria.requests.MainViewModelFactory
import com.example.appsimetria.requests.models.DispositivoGetAll
import com.example.appsimetria.requests.repository.Repository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.custom_dialog_close.view.*
import kotlinx.android.synthetic.main.custom_dialog_observation.view.*
import kotlinx.android.synthetic.main.custom_toast_maps_add_1.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MapsAddDevice : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener, GoogleMap.OnMyLocationButtonClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var binding: ActivityMapsAddDeviceBinding
    private lateinit var geocoder: Geocoder

    private lateinit var viewModel: MainViewModel
    private lateinit var baseDatos: FirebaseFirestore
    private lateinit var resultScanner: String

    private var arrayDispositivos: ArrayList<DispositivoGetAll> = arrayListOf()

    private var dispositivo_id: Int = -1
    private var latitud: Double = 0.0
    private var longitud: Double = 0.0

    companion object {
        private const val LOCATION_REQUEST_CODE = 1
    }

    data class Dispositivo1(val id: String, val latitud: String, val longitud: String, val fecha: String, val hora: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsAddDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black50)

        //Firestore Database & Load Scanner
        baseDatos = Firebase.firestore
        loadScanner()

        val repository = Repository()
        val viewModelFactory = MainViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
        getAllDevices()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment
            .getMapAsync(this)

        geocoder = Geocoder(applicationContext, Locale.getDefault())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun loadScanner() {
        val sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        resultScanner = sharedPreferences.getString("datos", null).toString()
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

        with(mMap){
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isMyLocationButtonEnabled = true
            uiSettings.isIndoorLevelPickerEnabled = true
            uiSettings.isMapToolbarEnabled = true

            isTrafficEnabled = true

            setOnMarkerDragListener(this@MapsAddDevice)
            setOnMyLocationButtonClickListener(this@MapsAddDevice)
        }

        with(binding){
            itemBotonAddMapsType.imagenMapsType.setOnClickListener {
                if (mMap.mapType == GoogleMap.MAP_TYPE_NORMAL)
                    mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                else
                    mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            }
            itemBotonAddMapsBack.imagenMapsBack.setOnClickListener {
                transition()
                startActivity(Intent(this@MapsAddDevice, MainMenu::class.java))
            }
            itemBotonCard.cardAddDispositivo.setOnClickListener {

                val dialogView = LayoutInflater.from(this@MapsAddDevice).inflate(R.layout.custom_dialog_observation, null)
                val builderDialogView = android.app.AlertDialog.Builder(this@MapsAddDevice)
                    .setView(dialogView)
                val alertDialog = builderDialogView.show()
                alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                dialogView.custom_decline.setOnClickListener {
                    alertDialog.dismiss()
                }

                dialogView.custom_accept.setOnClickListener {
                    if (dispositivo_id == -1) {
                        saveLatLngData(latitud, longitud)
                        /**writeDispositivo(Dispositivo1(resultScanner,latitud.toString(),longitud.toString(),getCurrentDate(),getCurrentTime()))*/
                        postAddDevice()
                        startActivity(Intent(this@MapsAddDevice, MainMenu::class.java))
                    } else {
                        postUpdateDevice()
                        startActivity(Intent(this@MapsAddDevice, MainMenu::class.java))
                    }
                    alertDialog.dismiss()
                }
            }
        }
        setUpMap()
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
            return
        }
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->

            if (location != null) {
                lastLocation = location
                val currentLatLong = LatLng(location.latitude, location.longitude)
                placeMarkerOnMap(resultScanner, currentLatLong)

                mMap
                    .animateCamera(CameraUpdateFactory
                        .newLatLngZoom(currentLatLong, 18f))

                latitud = location.latitude
                longitud = location.longitude
            }
        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        mMap
            .animateCamera(CameraUpdateFactory
            .newLatLngZoom(LatLng(latitud, longitud), 18f))
        return true
    }

    override fun onMarkerClick(p0: Marker) = false

    private fun placeMarkerOnMap(title: String, currentLatLong: LatLng) {
        val marker: Marker? = mMap.addMarker(MarkerOptions()
            .title(title)
            .flat(false)
            .position(currentLatLong)
            .draggable(true)
            .icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))
        marker!!.showInfoWindow()
    }

    override fun onMarkerDrag(p0: Marker) {
        return
    }

    override fun onMarkerDragEnd(p0: Marker) {
        latitud = p0.position.latitude
        longitud = p0.position.longitude

        toastPersonalizadoMaps1()
        saveLatLngData(latitud, longitud)
    }

    override fun onMarkerDragStart(p0: Marker) {
        Toast.makeText(this, "Arrastrado desde: (${p0.position.latitude},${p0.position.longitude}) ", Toast.LENGTH_LONG).show()
        return
    }

    private fun postAddDevice() {
        val listGeocoder: List<Address> = geocoder.getFromLocation(latitud, longitud, 1)

        viewModel.postAddDevice(
            "1",
            resultScanner,
            listGeocoder[0].thoroughfare,
            listGeocoder[0].postalCode,
            listGeocoder[0].locality,
            getCurrentDate(),
            latitud,
            longitud,
            listGeocoder[0].subAdminArea,
            Build.MANUFACTURER.replaceFirstChar { it.toUpperCase() },
            Build.MODEL,
            listGeocoder[0].featureName,
            listGeocoder[0].countryName
        )

        viewModel.postResponse.observe(this, androidx.lifecycle.Observer { response ->
            Log.i("PostAdd", response.status.toString())
            Log.i("PostAdd", response.title)
            Log.i("PostAdd", response.message)
            Log.i("PostAdd", response.result.toString())

        })
    }

    private fun postUpdateDevice() {
        val listGeocoder: List<Address> = geocoder.getFromLocation(latitud, longitud, 1)

        viewModel.postUpdateDevice(
            "1",
            resultScanner,
            listGeocoder[0].thoroughfare,
            listGeocoder[0].postalCode,
            listGeocoder[0].locality,
            getCurrentDate(),
            latitud,
            longitud,
            listGeocoder[0].subAdminArea,
            Build.MANUFACTURER.replaceFirstChar { it.toUpperCase() },
            Build.MODEL,
            listGeocoder[0].featureName,
            listGeocoder[0].countryName,
            dispositivo_id.toString()
        )

        viewModel.postResponse.observe(this, androidx.lifecycle.Observer { response ->
            Log.i("PostUpdate", response.status.toString())
            Log.i("PostUpdate", response.title)
            Log.i("PostUpdate", response.message)
            Log.i("PostUpdate", response.result.toString())
        })
    }

    private fun getAllDevices() {
        viewModel.getAllDevices()
        viewModel.getResponse.observe(this, androidx.lifecycle.Observer { response ->

            response.result.forEach {
                arrayDispositivos.add(it)
            }
            arrayDispositivos.forEach {
                if (it.mac == resultScanner){
                    dispositivo_id = it.dispositivo
                }
            }
            Log.e("ID", dispositivo_id.toString())
        })
    }

    private fun saveLatLngData(latitud : Double, longitud : Double) {
        val sharedPreferences = getSharedPreferences("LatLng", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        editor.putFloat("latitud", latitud.toFloat())
        editor.putFloat("longitud", longitud.toFloat())
        editor.apply()
    }

    private fun toastPersonalizadoMaps1() {
        val layoutToast =  layoutInflater.inflate(R.layout.custom_toast_maps_add_1, constraintToastMaps1)
        Toast(this).apply {
            duration = Toast.LENGTH_SHORT
            setGravity(Gravity.TOP, 0, 20)
            view = layoutToast
        }.show()
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
    }

    private fun getCurrentTime(): String {
        return SimpleDateFormat("E HH:mm", Locale.getDefault()).format(Date())
    }

    private fun transition() {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right)
    }
}

/****************************************
 * FIREBASE FUNCTIONS
 ****************************************/

/**private fun writeDispositivo(dispositivo: NewDispositiveMaps.Dispositivo1) {
    val listGeocoder: List<Address> = geocoder.getFromLocation(latitud, longitud, 1)

    val data = HashMap<String, Any>()

    data["ID"] = dispositivo.id
    data["Latitud"] = dispositivo.latitud
    data["Longitud"] = dispositivo.longitud
    data["Fecha"] = dispositivo.fecha
    data["Hora"] = dispositivo.hora

    data["Localidad"] = listGeocoder[0].locality
    data["Numero"] = listGeocoder[0].featureName
    data["Ciudad"] = listGeocoder[0].subAdminArea
    data["Comunidad"] = listGeocoder[0].adminArea
    data["Pais"] = listGeocoder[0].countryName
    data["Calle"] = listGeocoder[0].thoroughfare
    data["Codigo Postal"] = listGeocoder[0].postalCode

    data["Marca"] = Build.MANUFACTURER.replaceFirstChar { it.toUpperCase() }
    data["Modelo"] = Build.MODEL

    baseDatos
        .collection("Dispositivos")
        .document(resultScanner)
        .set(data)
        .addOnSuccessListener { Log.d(TAG, "Successfly written") }
        .addOnFailureListener { Log.w(TAG, "Failed to be written!") }
}*/