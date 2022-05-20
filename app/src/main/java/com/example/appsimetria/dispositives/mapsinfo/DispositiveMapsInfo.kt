package com.example.appsimetria.dispositives.mapsinfo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.appsimetria.R
import com.example.appsimetria.databinding.ActivityDispositiveMapsInfoBinding

class DispositiveMapsInfo : AppCompatActivity() {

    private lateinit var binding: ActivityDispositiveMapsInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDispositiveMapsInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fragment: Fragment = MapsFragment()

    }
}