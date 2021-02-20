package com.lewis.asmdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.i("MainActivity", "ttt run1")
        Log.i("MainActivity", "ttt run2")
        Log.i("MainActivity", "ttt run3")
    }
}
