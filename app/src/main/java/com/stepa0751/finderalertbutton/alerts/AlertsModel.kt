package com.stepa0751.finderalertbutton.alerts

import java.io.Serializable
import org.osmdroid.util.GeoPoint


data class AlertsModel (
    val timeStartAlert: Float = 0.0f,
    val latitude: Float = 0.0f,
    val longitude: Float = 0.0f,
    val geoPointsList: ArrayList<GeoPoint>
): Serializable