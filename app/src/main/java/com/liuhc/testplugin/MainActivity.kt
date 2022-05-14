package com.liuhc.testplugin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        thread(true) {
            while (true) {
                Thread.sleep((1..3).random() * 1000L)
                val demo = InvokeDemo()
                demo.demoMethod()
            }
        }
    }
}