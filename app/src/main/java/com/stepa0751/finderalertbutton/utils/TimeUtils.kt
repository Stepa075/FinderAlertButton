package com.stepa0751.finderalertbutton.utils

import android.annotation.SuppressLint

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone

object TimeUtils {
    // Создаем форматер для вывода времени в "нормальном виде" а не в 1.87908789797098
    @SuppressLint("SimpleDateFormat")
    private val timeFormatter = SimpleDateFormat("HH:mm:ss")
    //  В эту функцию передаем время в милисекундах
    fun getTime(timeInMillis: Long): String{
        //  Здесь вызываем преобразователь из милисекунд в нормальный формат
        val cv = Calendar.getInstance()
    //  Эта строка нужна для того, чтобы на часах отображалось время не с 01, а с 00 часов.
        timeFormatter.timeZone = TimeZone.getTimeZone("UTC")
        cv.timeInMillis = timeInMillis
        //  Возвращаем отконвертированное время (в формате, который задали форматером)
        return timeFormatter.format(cv.time)
    }
}