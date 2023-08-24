package com.stepa0751.finderalertbutton.utils

import android.content.pm.PackageManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.stepa0751.finderalertbutton.R


// Функция для переключения между фрагментами
fun Fragment.openFragment(f: Fragment){
    (activity as AppCompatActivity).supportFragmentManager
        .beginTransaction()
        .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        .replace(R.id.placeHolder, f).commit()

}


// Функция для переключения между активити, если мы решим добавить еще активити ???
// Этот код переключает и фрагменты с мэин на второй
fun AppCompatActivity.openFragment(f: Fragment){

//    если какой-либо фрагмент открыт (приложение уже работает)
    if(supportFragmentManager.fragments.isNotEmpty()){
        //то проверяем не совпадает ли название фрагмента с тем, который мы хотим вызвать
        //  если совпадает, то делаем ретерн и не продолжаем функцию!!!
        if (supportFragmentManager.fragments[0].javaClass == f.javaClass) return
    }
    //    а если название не совпадает, то переключаемся на вызванный фрагмент
    supportFragmentManager
        .beginTransaction()
        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
        .replace(R.id.placeHolder, f).commit()
}
