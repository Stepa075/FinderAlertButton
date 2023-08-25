package com.stepa0751.finderalertbutton.location

import org.osmdroid.util.GeoPoint
import java.io.Serializable

//  Создали дата-класс, которым будут передаваться данные в активити
data class LocationModel(
    val speed: Float = 0.0f,
    val distance: Float = 0.0f,
    val latitude: Float = 0.0f,
    val longitude: Float = 0.0f,
    val geoPointsList: ArrayList<GeoPoint>
) : Serializable