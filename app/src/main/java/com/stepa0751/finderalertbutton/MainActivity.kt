package com.stepa0751.finderalertbutton

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.stepa0751.finderalertbutton.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {

        }
    }

}