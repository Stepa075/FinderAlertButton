package com.stepa0751.finderalertbutton.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import checkPermission
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.stepa0751.finderalertbutton.COMMAND_ID
import com.stepa0751.finderalertbutton.COMMAND_START
import com.stepa0751.finderalertbutton.COMMAND_STOP
import com.stepa0751.finderalertbutton.ForegroundService
import com.stepa0751.finderalertbutton.R
import com.stepa0751.finderalertbutton.STARTED_TIMER_TIME_MS
import com.stepa0751.finderalertbutton.databinding.FragmentMainBinding
import com.stepa0751.finderalertbutton.location.LocationService
import com.stepa0751.finderalertbutton.utils.DialogManager
import com.stepa0751.finderalertbutton.utils.TimeUtils
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import showToast
import java.util.Timer
import java.util.TimerTask

class MainFragment : Fragment() {

    private var isBackGroundServiceRunning = false
    private var timer: Timer? = null
    private var startTime = 0L
    private var startTimeForNewService = 0L
    val timeData = MutableLiveData<String>()
    private lateinit var pLauncher: ActivityResultLauncher<Array<String>>

    //    Создаем переменную binding
    private lateinit var binding: FragmentMainBinding
    private var isServiceRunning = false

    // Надуваем инфлейтер и получаем доступ ко всем элементов вью
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Инициализируем binding c инфлейтером, который пришел нам в onCreateView см. выше
        binding = FragmentMainBinding.inflate(inflater, container, false)
        // полцчаем доступ ко всем элементам разметки
        return binding.root
        }

    //  Инициализируем все, что необходимо после создания вью
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerPermissions()
        checkServiceState()
        setOnClicks()
        updateTime()
        receiveDataAndLocation()
        Log.d("MyLog", "OnViewCreated")
        startTimeForNewService = System.currentTimeMillis()
        lifecycleScope.launch(Dispatchers.Main) {
            while (true) {
                val timeNow = System.currentTimeMillis() - startTimeForNewService
                binding.timerView.text = TimeUtils.getTime(System.currentTimeMillis() - startTimeForNewService)
                delay(INTERVAL)
                if(timeNow >= 10000){
                    startTimeForNewService = System.currentTimeMillis()
                }
            }
        }
    }


    fun onAppBackgrounded(){
        val startIntent = Intent(activity, ForegroundService::class.java)
        startIntent.putExtra(COMMAND_ID, COMMAND_START)
        startIntent.putExtra(STARTED_TIMER_TIME_MS, startTime)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity?.startForegroundService(startIntent)
        } else {
            activity?.startService(startIntent)
        }
        Log.d("MyLog", "onAppBackgrounded")
    }


    fun onAppForegrounded(){
        val stopIntent = Intent(activity, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity?.startForegroundService(stopIntent)
        } else {
            activity?.startService(stopIntent)
        }
        Log.d("MyLog", "onAppForegrounded")
    }

    fun onAppClosing(){
        onAppForegrounded()
        isBackGroundServiceRunning = true
        Log.d("MyLog", "onAppClosing")
    }

    private fun receiveDataAndLocation() {
        val queue = Volley.newRequestQueue(context)
        val user_id_pref = context?.let {
            PreferenceManager.getDefaultSharedPreferences(it)
                .getString("id_user_key", "0000")
        }
        val token_pref = context?.let {
            PreferenceManager.getDefaultSharedPreferences(it)
                .getString("token_key", "")
        }
        val chat_id_pref = context?.let {
            PreferenceManager.getDefaultSharedPreferences(it)
                .getString("chat_id_key", "")
        }

        val url = "https://api.telegram.org/bot${token_pref}/getUpdates?chat_id=-${chat_id_pref}"

        val sRequest = StringRequest(
            Request.Method.GET,
            url, { response ->
                parseServerResponse(response)

            },
            { Log.d("MyLog", "Error request: $it") }
        )
       queue.add(sRequest)
    }

    @SuppressLint("NewApi")
    private fun parseServerResponse(response: String) {
        try {
            val mainObject = JSONObject(response)
            val ok = mainObject.get("ok")
            Log.d("MyLog", "JSON Response: ok = $ok")
            val result = mainObject.get("result") as JSONArray
            for (results in 0 until result.length()){
                val item = result[results] as JSONObject
                val updateId = item.get("update_id")
                Log.d("MyLog", "JSON Response: update_id = $updateId")
                val channelPost = item.get("channel_post") as JSONObject
                try{
                    val date = channelPost.getString("date")
                    Log.d("MyLog", "JSON Response: date = $date")
                } catch (e: Exception){
                    Log.d("MyLog", "JSON Response: date is $e")
                }
                try{
                    val location = channelPost.get("location") as JSONObject
                    val location_latitude = location.get("latitude")
                    val location_longitude = location.get("longitude")
                    Log.d("MyLog", "JSON Response: location_latitude = $location_latitude")
                    Log.d("MyLog", "JSON Response: location_longitude = $location_longitude")
                }catch (e: Exception){
                    Log.d("MyLog", "JSON Response: location is $e")
                }
                try{
                    val text = channelPost.getString("text")
                    val textId = text.substringBefore(";").substringAfter(":")
                    val textAlert = text.substringAfter(";").substringBefore(";")
                    val textLat = text.substringAfter(";").substringAfter(";").substringBefore(",")
                    val textLon = text.substringAfter(";").substringAfter(";").substringAfter(",")

                    Log.d("MyLog", "JSON Response: text = $text")
                    Log.d("MyLog", "JSON Response: textId = $textId")
                    Log.d("MyLog", "JSON Response: textAlert = $textAlert")
                    Log.d("MyLog", "JSON Response: textLat = $textLat")
                    Log.d("MyLog", "JSON Response: textLon = $textLon")
                }catch (e: Exception){
                    Log.d("MyLog", "JSON Response: text is $e")
                }
            }
        } catch(e: Exception)  {
            Log.d("MyLOg", "non parametres $e")
        }
    }

    //  Когда возвращаемся в вью проверяем доступность местонахождения в телефоне
    override fun onResume() {
        super.onResume()
        checkLocPermission()
        if(isBackGroundServiceRunning) onAppForegrounded()
        isBackGroundServiceRunning = false

        Log.d("MyLog", "OnResume")
    }

    override fun onPause() {
        super.onPause()
        if(!isBackGroundServiceRunning) onAppBackgrounded()
        isBackGroundServiceRunning = true
        Log.d("MyLog", "OnPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MyLog", "OnDestroy")
    }

    override fun onStop() {
        super.onStop()
        if(!isBackGroundServiceRunning) onAppBackgrounded()
        isBackGroundServiceRunning = true
        Log.d("MyLog", "OnStop")
    }

    private fun registerPermissions() {
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {

            if (it[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
//               initOsm()
                Log.d("MyLog", "ACCESS_FINE_LOCATION == true")
                checkLocationEnabled()
            } else {
                showToast("You have not given permission for location tracking. The app don't work!")

            }
        }
    }


    private fun checkServiceState() {
        isServiceRunning = LocationService.isRunning
        if (isServiceRunning) {
            binding.startStop.setImageResource(R.drawable.ic_alarm_red)
        }
    }


    //  Функция инициализации слушателя нажатий для ВСЕГО ВЬЮ
    fun setOnClicks() = with(binding) {
        val listener = onClicks()
        startStop.setOnClickListener(listener)
        bStart.setOnClickListener(listener)
    }

    //  Функция сработки слушателя нажатий на этом вью
    private fun onClicks(): View.OnClickListener {
        return View.OnClickListener {
            when (it.id) {
                R.id.start_stop -> startStopService()
                R.id.bStart -> onAppClosing()
            }
        }
    }


    //  Обновление времени в tv_text с помощью обсервера, который слушает изменения в переменной timeData типа MutableLiveData<String>()
    private fun updateTime() {
        timeData.observe(viewLifecycleOwner) {
            binding.tvTime.text = it
        }
    }


    //  Запуск таймера, который (если что-то там есть) возьмет startTime из LocationService.startTime
    //  И запускать нужно в основном потоке, иначе значения в текст вью меняться не будут!
    private fun startTimer() {
        timer?.cancel()
        timer = Timer()
        startTime = LocationService.startTime
        timer?.schedule(object : TimerTask() {
            override fun run() {
                activity?.runOnUiThread {
                    timeData.value = getCurrentTime()
                }
            }
        }, 1000, 1000)
    }

    //  Функция выбора набора разрешений для более старых и новых версий андроид
    private fun checkLocPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            checkPermissionAfter10()
        } else {
            checkPermissionBefore10()
        }
    }


    //   Если больше или равно 10 версии андроида
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkPermissionAfter10() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            && checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        ) {
//            initOsm()
            checkLocationEnabled()

        } else {
            pLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            )
        }
    }

    //   Если меньше 10 версии андроида
    private fun checkPermissionBefore10() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
//            initOsm()
            checkLocationEnabled()
        } else {
            pLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        }
    }

    //    Определение включен ли GPS и вызов диалог менеджера, если выключен
    private fun checkLocationEnabled() {
        val lManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isEnabled = lManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!isEnabled) {
            DialogManager.showLocEnableDialog(activity as AppCompatActivity,
                object : DialogManager.Listener {
                    override fun onClick() {
                        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }

                })
        } else {
            showToast("Location enabled")
        }
    }

    //  Здесь получаем время следования по маршруту, если приложение закрыли и работал только сервис
    //  От текущего времени отнимаем стартовое (все в миллисекундах!!!)
    private fun getCurrentTime(): String {
        return "Elapsed time: ${TimeUtils.getTime(System.currentTimeMillis() - startTime)}"
    }


    //  Функция запуска и остановки сервиса, в зависимости от состояния переменной isServiceRunning
    private fun startStopService() {
        if (!isServiceRunning) {
//            Здесь я подставил включение зеленой кнопки, зачем??????
            binding.startStop.setImageResource(R.drawable.ic_disalarm_green)
            startLocService()
        } else {
            activity?.stopService(Intent(activity, LocationService::class.java))
            binding.startStop.setImageResource(R.drawable.ic_disalarm_green)
            timer?.cancel()
        }
        isServiceRunning = !isServiceRunning
    }


    //  Запуск сервиса, в зависимости от нажатия на кнопку "старт" или "стоп"
    private fun startLocService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity?.startForegroundService(Intent(activity, LocationService::class.java))
//            Здесь я подставил включение красной кнопки, зачем??????
            binding.startStop.setImageResource(R.drawable.ic_alarm_red)
        } else {
            activity?.startService(Intent(activity, LocationService::class.java))
        }
        binding.startStop.setImageResource(R.drawable.ic_alarm_red)
        LocationService.startTime = System.currentTimeMillis()
        startTimer()
    }


    companion object {
        private const val INTERVAL = 10L
        @JvmStatic
        fun newInstance() = MainFragment()
    }

}


