package com.example.appsimetria.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.appsimetria.R
import com.example.appsimetria.MainMenu
import com.example.appsimetria.databinding.ActivityLoginBinding
import kotlinx.android.synthetic.main.custom_toast_login_1.*
import kotlinx.android.synthetic.main.custom_toast_login_2.*
import kotlin.system.exitProcess

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.primary_statusbar)

        binding.textoRegistrar?.setOnClickListener {
            val intentRegister = Intent(this, Register::class.java)
            startActivity(intentRegister)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        binding.botonLogin?.setOnClickListener {
            val password = findViewById<TextView>(R.id.contraseña)
            val user = findViewById<TextView>(R.id.usuario)

            if (user.text.isEmpty() || password.text.isEmpty()) {
                toastPersonalizadoLogin2()
            } else {
                if (user.text.toString() == password.text.toString()){
                    val intentMenu = Intent(this, MainMenu::class.java)
                    startActivity(intentMenu)
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out)
                } else {
                    toastPersonalizadoLogin1()
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        exitProcess(0)
    }

    private fun toastPersonalizadoLogin1() {
        val layoutToast =  layoutInflater.inflate(R.layout.custom_toast_login_1, constraintToastLogin1)
        Toast(this).apply {
            duration = Toast.LENGTH_SHORT
            setGravity(Gravity.BOTTOM, 0, 100)
            view = layoutToast
        }.show()
    }

    private fun toastPersonalizadoLogin2() {
        val layoutToast =  layoutInflater.inflate(R.layout.custom_toast_login_2, constraintToastLogin2)
        Toast(this).apply {
            duration = Toast.LENGTH_SHORT
            setGravity(Gravity.BOTTOM, 0, 100)
            view = layoutToast
        }.show()
    }
}

//https://medium.com/@tom.truyen/implement-qr-code-scanner-in-android-kotlin-be5fa7f0d50c Página Códigos QR
//Desarrollo UI ----> FIGMA Y DRIBBBLE