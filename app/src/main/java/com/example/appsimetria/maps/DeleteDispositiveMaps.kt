package com.example.appsimetria.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.appsimetria.R
import com.example.appsimetria.ServicesMenu
import com.example.appsimetria.databinding.ActivityDeleteDispositiveMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*

import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.android.synthetic.main.activity_delete_dispositive_maps.*
import kotlinx.android.synthetic.main.custom_dialog_close.view.*
import kotlinx.android.synthetic.main.custom_toast_maps_add_1.*
import java.lang.Exception

class DeleteDispositiveMaps : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMarkerDragListener {

    private lateinit var mMap: GoogleMap
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var binding: ActivityDeleteDispositiveMapsBinding

    private lateinit var baseDatos: FirebaseFirestore

    private var latitud: Double = 0.0
    private var longitud: Double = 0.0

    companion object {
        private const val LOCATION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeleteDispositiveMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        baseDatos = FirebaseFirestore.getInstance()
        baseDatos.firestoreSettings = FirebaseFirestoreSettings.Builder().build()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
             .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
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

        mMap.setOnMyLocationButtonClickListener(this)
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMarkerDragListener(this)

        item_boton_delete_maps_type.setOnClickListener {
            if (mMap.mapType == GoogleMap.MAP_TYPE_NORMAL)
                mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            else
                mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        }

        item_boton_delete_maps_back.setOnClickListener {
            startActivity(Intent(this, ServicesMenu::class.java))
        }

        setUpMap()
        loadDispositivo()
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                DeleteDispositiveMaps.LOCATION_REQUEST_CODE
            )
            return
        }
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->

            if (location != null) {
                lastLocation = location
                val currentLatLong = LatLng(location.latitude, location.longitude)

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

    override fun onMarkerClick(p0: Marker): Boolean {
        p0.showInfoWindow()
        return true
    }

    override fun onMarkerDrag(p0: Marker) {}

    override fun onMarkerDragEnd(p0: Marker) {}

    @SuppressLint("InflateParams")
    override fun onMarkerDragStart(p0: Marker) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog_close, null)
        val builderDialogView = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
        val alertDialog = builderDialogView.show()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogView.custom_alert_decline.setOnClickListener {
            alertDialog.dismiss()
            rechargeMap()
        }

        dialogView.custom_alert_accept.setOnClickListener {
            alertDialog.dismiss()
            baseDatos
                .collection("Dispositivos")
                .document(p0.title.toString())
                .delete()
                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!")
                    toastPersonalizadoDeleteMaps1()}
                .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }

            rechargeMap()
        }
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
    }

    @SuppressLint("SetTextI18n")
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
    }

    private fun toastPersonalizadoDeleteMaps1() {
        val layoutToast =  layoutInflater.inflate(R.layout.custom_toast_maps_delete_1, constraintToastMaps1)
        Toast(this).apply {
            duration = Toast.LENGTH_SHORT
            setGravity(Gravity.BOTTOM, 0, 90)
            view = layoutToast
        }.show()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun customMarker(): Bitmap {
        val bitmapImage: BitmapDrawable = resources.getDrawable(R.drawable.marker) as BitmapDrawable
        return Bitmap.createScaledBitmap(bitmapImage.bitmap, 100, 100, false)
    }

    private fun rechargeMap() {
        mMap.clear()
        loadDispositivo()
    }
}