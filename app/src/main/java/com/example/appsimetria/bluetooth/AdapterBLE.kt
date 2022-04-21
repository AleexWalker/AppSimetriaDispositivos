package com.example.appsimetria.bluetooth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.appsimetria.databinding.ActivityAdapterBleBinding

class AdapterBLE : AppCompatActivity() {

    private lateinit var binding: ActivityAdapterBleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdapterBleBinding.inflate(layoutInflater)
        setContentView(binding.root)



    }
}