package com.example.appsimetria.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.Toast
import com.example.appsimetria.R
import com.example.appsimetria.ServicesMenu
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.custom_toast_register_1.*
import kotlinx.android.synthetic.main.custom_toast_register_2.*

class Register : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        textoLoginRegister.setOnClickListener {
            val intentLogIn = Intent(this, Login::class.java)
            startActivity(intentLogIn)

            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        botonRegister.setOnClickListener {
            if (contraseñaRegister.text.toString() == confirmarContraseñaRegister.text.toString()) {
                if (validPassword(contraseñaRegister.text.toString())) {
                    val intentMenu = Intent(this, ServicesMenu::class.java)
                    startActivity(intentMenu)
                } else {
                    toastPersonalizadoRegister2()
                }
            } else {
                toastPersonalizadoRegister1()
            }
        }
    }

    internal fun validPassword(password : String): Boolean {
        if (password.length <= 5) return false
        if (password.firstOrNull { it.isDigit() } == null) return false
        if (password.filter { it.isLetter() }.firstOrNull { it.isUpperCase() } == null) return false
        if (password.filter { it.isLetter() }.firstOrNull { it.isLowerCase() } == null) return false
        if (password.firstOrNull { it.isLetterOrDigit() } == null) return false

        return true
    }

    private fun toastPersonalizadoRegister1() {
        val layoutToast =  layoutInflater.inflate(R.layout.custom_toast_register_1, constraintToastRegister1)
        Toast(this).apply {
            duration = Toast.LENGTH_SHORT
            setGravity(Gravity.BOTTOM, 0, 100)
            view = layoutToast
        }.show()
    }

    private fun toastPersonalizadoRegister2() {
        val layoutToast =  layoutInflater.inflate(R.layout.custom_toast_register_2, constraintToastRegister2)
        Toast(this).apply {
            duration = Toast.LENGTH_LONG
            setGravity(Gravity.BOTTOM, 0, 100)
            view = layoutToast
        }.show()
    }
}