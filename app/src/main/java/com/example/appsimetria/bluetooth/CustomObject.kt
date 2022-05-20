package com.example.appsimetria.bluetooth

import android.content.Context
import android.content.Intent


class CustomObject {
    private var context: Context? = null
    // and some other fields here...

    fun CustomObject(c: Context?) {
        this.context = c
    }

    fun startActivity() {
        val intent = Intent(context, BLEOperations::class.java)
        context!!.startActivity(intent)
    }
}