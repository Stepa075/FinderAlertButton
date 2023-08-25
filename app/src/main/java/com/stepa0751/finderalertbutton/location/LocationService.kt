package com.stepa0751.finderalertbutton.location

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.stepa0751.finderalertbutton.MainActivity
import com.stepa0751.finderalertbutton.R
import org.osmdroid.util.GeoPoint

//  Создали сервис для работы в фоновом режиме... В манифесте его нужно прописать!!!
class LocationService : Service() {

    //  Переменная для хранения последнего местоположения для измерения расстояния между старой и новой точками
    private var lastLocation: Location? = null
    //  Переменная для хранения высчтанного расстояния
    private var distance = 0.0f
    private  var latit = 0.0f
    private  var longit = 0.0f
    //    Эта переменная нужна для того, чтобы подключаться к провайдеру GPS и получать у него данные о местоположении
    private lateinit var locProvider: FusedLocationProviderClient
    private lateinit var locRequest: LocationRequest
    private lateinit var geoPointsList: ArrayList<GeoPoint>

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startNotification()
        startLocationUpdates()
        isRunning = true
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        geoPointsList = ArrayList()
        initLocation()

    }

    override fun onDestroy() {
        super.onDestroy()
        //  Переменную "работает" делаем ложь
        isRunning = false
        //  Отписываеся от обновлений местоположения
        locProvider.removeLocationUpdates(locCallBack)
    }


    //   Все что ниже - темный лес, но нужен он для запуска сервиса и отображения его в фореграунд.
    private fun startNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nChannel = NotificationChannel(
                CHANNEL_ID,
                "Location Service",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val nManager = getSystemService(NotificationManager::class.java) as NotificationManager
            nManager.createNotificationChannel(nChannel)
        }
        val nIntent = Intent(this, MainActivity::class.java)
        val pIntent = PendingIntent.getActivity(
            this,
            10,
            nIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(
            this,
            CHANNEL_ID
        ).setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Alarm tracker running")
            .setContentIntent(pIntent).build()
        startForeground(99, notification)
    }

    //  Инициализация клиента доступа к подписке на местоположение
    private fun initLocation(){
        //   Создаем объект
        locRequest =  LocationRequest.create()
        //  Назначаем интервал обновления
        val interval_pref = PreferenceManager.getDefaultSharedPreferences(baseContext)
            .getString("update_time_key", "5000")
        locRequest.interval = interval_pref?.toLong()!!
        //  Максимальная скорость обновления местоположения
        locRequest.fastestInterval = 10000
        //  Назначаем приоритет "Высокая точность"
        locRequest.priority = PRIORITY_HIGH_ACCURACY
        //  Инициализируем сам locProvider
        locProvider = LocationServices.getFusedLocationProviderClient(baseContext)

    }

    // Сюда приходит информация о местоположении в lResult
    private val locCallBack = object : LocationCallback() {
        override fun onLocationResult(lResult: LocationResult) {
            super.onLocationResult(lResult)
            val currentLocation = lResult.lastLocation
            if (lastLocation != null && currentLocation != null) {
                if ((currentLocation?.speed ?: 0.0f) > 0.5) distance += (currentLocation
                    ?: lastLocation)?.let { lastLocation?.distanceTo(it) }!!
                geoPointsList.add(GeoPoint(currentLocation.latitude, currentLocation.longitude))
                val locModel = LocationModel(
                    currentLocation.speed,
                    distance,
                    currentLocation.latitude.toFloat(),
                    currentLocation.longitude.toFloat(),
                    geoPointsList
                )
                latit = currentLocation.latitude.toFloat()
                longit = currentLocation.longitude.toFloat()
                sendLocData(locModel)
                sendDataAndLocation()
                sendLocation()


            }
            lastLocation = currentLocation

//            Log.d(
//                "MyLog",
//                "Location: ${lResult.lastLocation?.latitude} : ${lResult.lastLocation?.longitude}"
//            )

        }
    }

    private fun sendLocData(locModel: LocationModel){
        val i = Intent(LOC_MODEL_INTENT)
        i.putExtra(LOC_MODEL_INTENT, locModel)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(i)
    }

    //  Функция запуска слушателя местоположения, для нее нужны несколько параметров:
    // И с начала в ней идет проверка на разрешение пользователем приложению доступа к местоположению
    //  Так просит котлин, потому что он не понимает что мы уже где-то раньше проверяли это.
    private fun startLocationUpdates(){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        locProvider.requestLocationUpdates(
            //  Этот параметр получаем в initLocation
            locRequest,
            // Сюда будет приходить информация о нашем местоположении
            locCallBack,
            //  И лупер нужен, чтобы повторять поток запроса инфы о местоположении,
            // т.к. поток закрывается после выполнения всех команд в нем.
            Looper.myLooper()
        )
    }

    private fun sendDataAndLocation(){
        val user_id_pref = PreferenceManager.getDefaultSharedPreferences(baseContext)
            .getString("id_user_key", "0000")
        val token_pref = PreferenceManager.getDefaultSharedPreferences(baseContext)
            .getString("token_key", "")
        val chat_id_pref = PreferenceManager.getDefaultSharedPreferences(baseContext)
            .getString("chat_id_key", "")

//        val id_user_program = "0001"
        val lat = latit.toString()
        val lon = longit.toString()
        val url = "https://api.telegram.org/bot${token_pref}/sendmessage?chat_id=-${chat_id_pref}&text=User ID: ${user_id_pref}. Allert button pressed! Location: ${lat}, ${lon}"
        val queue = Volley.newRequestQueue(baseContext)
        val sRequest = StringRequest(
            Request.Method.GET,
            url, { response ->
                Log.d("MyLog", "Response: ${response.subSequence(1, 10)}")
//
//                val list = getWeatherByDays(response)
//                dayList.value = list
//                currentDay.value = list[0]
            },
            { Log.d("MyLog", "Error request: $it") }
        )
        queue.add(sRequest)

    }


    private fun sendLocation(){
        val token_pref = PreferenceManager.getDefaultSharedPreferences(baseContext)
            .getString("token_key", "")
        val chat_id_pref = PreferenceManager.getDefaultSharedPreferences(baseContext)
            .getString("chat_id_key", "")
        val lat = latit.toString()
        val lon = longit.toString()
        val url = "https://api.telegram.org/bot${token_pref}/sendlocation?chat_id=-${chat_id_pref}&latitude=${lat}&longitude=${lon}"
        val queue = Volley.newRequestQueue(baseContext)
        val sRequest = StringRequest(
            Request.Method.GET,
            url, { response ->
                Log.d("MyLog", "Response: ${response.subSequence(1, 10)}")
//                val list = getWeatherByDays(response)
//                dayList.value = list
//                currentDay.value = list[0]
            },
            { Log.d("MyLog", "Error request: $it") }
        )
        queue.add(sRequest)
    }

    companion object {
        const val CHANNEL_ID = "channel_1"
        const val LOC_MODEL_INTENT = "loc_intent"
        var isRunning = false
        var startTime = 0L
    }
}