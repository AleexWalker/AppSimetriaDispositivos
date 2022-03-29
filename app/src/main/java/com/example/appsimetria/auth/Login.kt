package com.example.appsimetria.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.appsimetria.R
import com.example.appsimetria.ServicesMenu
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.custom_toast_login_1.*
import kotlinx.android.synthetic.main.custom_toast_login_2.*

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        window.statusBarColor = ContextCompat.getColor(this, R.color.primary_statusbar)

        textoRegistrar.setOnClickListener {
            val intentRegister = Intent(this, Register::class.java)
            startActivity(intentRegister)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        botonLogin.setOnClickListener {
            val password = findViewById<TextView>(R.id.contraseña)
            val user = findViewById<TextView>(R.id.usuario)

            if (user.text.isEmpty() || password.text.isEmpty()) {
                toastPersonalizadoLogin2()
            } else {
                if (user.text.toString() == password.text.toString()){
                    val intentMenu = Intent(this, ServicesMenu::class.java)
                    startActivity(intentMenu)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                } else {
                    toastPersonalizadoLogin1()
                }
            }
        }
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