package com.stepa0751.finderalertbutton

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.stepa0751.finderalertbutton.databinding.ActivityMainBinding
import com.stepa0751.finderalertbutton.fragments.MainFragment
import com.stepa0751.finderalertbutton.fragments.SettingsFragment
import com.stepa0751.finderalertbutton.fragments.RoutesFragment
import com.stepa0751.finderalertbutton.utils.openFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater) //Привязка всех элементов в хмл
        setContentView(binding.root)                         // чтобы мы могли к ним обращаться
        onButtomNavClicks()
        openFragment(MainFragment.newInstance()) // собственно вызов функции переключения из Extantions.kt
    }                                              //при запуске приложения
    private fun onButtomNavClicks(){
        binding.bNaw.setOnItemSelectedListener {  //  слушатель кнопок меню
            when(it.itemId){    // переключение фрагментов
                R.id.id_home -> openFragment(MainFragment.newInstance())
                R.id.id_routes ->openFragment(RoutesFragment.newInstance())
                R.id.id_settings ->openFragment(SettingsFragment())
            }
            true
        }
    }
}