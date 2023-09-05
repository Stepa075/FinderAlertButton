package com.stepa0751.finderalertbutton.alerts

import java.io.Serializable


data class AlertsModel (
    val id: String = "",
    val update_id: Long = 0L,
    val date: Long = 0L,
    val timeStartAlert: Float = 0.0f,
    val latitude: Float = 0.0f,
    val longitude: Float = 0.0f,

): Serializable