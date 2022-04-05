package com.example.appsimetria.maps

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.appsimetria.ServicesMenu
import com.example.appsimetria.R

import com.example.appsimetria.databinding.ActivityNewDispositiveMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_new_dispositive_maps.*
import kotlinx.android.synthetic.main.custom_toast_maps_add_1.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class NewDispositiveMaps : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener, GoogleMap.OnMyLocationButtonClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var binding: ActivityNewDispositiveMapsBinding
    private lateinit var geocoder: Geocoder

    private lateinit var baseDatos: FirebaseFirestore
    private lateinit var resultScanner: String

    private var latitud: Double = 0.0
    private var longitud: Double = 0.0

    companion object {
        private const val LOCATION_REQUEST_CODE = 1
    }

    data class Dispositivo(val id: String, val latitud: String, val longitud: String, val fecha: String, val hora: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewDispositiveMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Firestore Database & Load Scanner
        baseDatos = Firebase.firestore
        loadScanner()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

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

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        mMap.setOnMarkerDragListener(this)
        mMap.setOnMyLocationButtonClickListener(this)

        item_boton_add_maps_type.setOnClickListener {
            if (mMap.mapType == GoogleMap.MAP_TYPE_NORMAL)
                mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            else
                mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        }

        item_boton_add_maps_back.setOnClickListener {
            startActivity(Intent(this, ServicesMenu::class.java))
        }

        item_boton_card.setOnClickListener {
            saveLatLngData(latitud, longitud)
            writeDispositivo(Dispositivo(resultScanner, latitud.toString(), longitud.toString(), getCurrentDate(), getCurrentTime()))

            val intentMenu = Intent(this, ServicesMenu::class.java)
            startActivity(intentMenu)
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
        mMap.animateCamera(CameraUpdateFactory
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

    private fun writeDispositivo(dispositivo: Dispositivo) {
        val listGeocoder: List<Address> = geocoder.getFromLocation(latitud, longitud, 1)

        val data = HashMap<String, Any>()

        data["ID"] = dispositivo.id
        data["Latitud"] = dispositivo.latitud
        data["Longitud"] = dispositivo.longitud
        data["Fecha"] = dispositivo.fecha
        data["Hora"] = dispositivo.hora

        data["Localidad"] = listGeocoder[0].locality
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
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
    }

    private fun getCurrentTime(): String {
        return SimpleDateFormat("E HH:mm", Locale.getDefault()).format(Date())
    }
}