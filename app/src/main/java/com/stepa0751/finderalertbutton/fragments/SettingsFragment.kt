package com.stepa0751.finderalertbutton.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.stepa0751.finderalertbutton.R


class SettingsFragment : PreferenceFragmentCompat() {
    private lateinit var timePref: Preference
    private lateinit var serverTimePref: Preference
    private lateinit var colorPref: Preference
    private lateinit var crewPref: Preference
    private lateinit var idPref: Preference
    private lateinit var tokenPref: Preference


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.main_preferences, rootKey)

        init()
    }

//    как найти элемент в настройках, который обозначается на экране
//    добавлять можно один и тот же слушатель для нескольких меню

    private fun init() {
        timePref = findPreference("update_time_key")!!
        serverTimePref = findPreference("server_update_key")!!
        colorPref = findPreference("color_key")!!
        crewPref = findPreference("id_crew_key")!!
        idPref = findPreference("chat_id_key")!!
        tokenPref = findPreference("token_key")!!
        val changeListener = onChangeListener()
        timePref.onPreferenceChangeListener = changeListener
        serverTimePref.onPreferenceChangeListener = changeListener
        colorPref.onPreferenceChangeListener = changeListener
        crewPref.onPreferenceChangeListener = changeListener
        idPref.onPreferenceChangeListener = changeListener
        tokenPref.onPreferenceChangeListener = changeListener

        initPrefs()
    }

    //    пишем слушатель изменений в настройках
    private fun onChangeListener(): Preference.OnPreferenceChangeListener {
        return Preference.OnPreferenceChangeListener { pref, value ->
            when (pref.key) {
                "update_time_key" -> onTimeChange(value.toString())
                "server_update_key" -> onServerTimeChange(value.toString())
                "id_user_key" -> onIdChange(value.toString())
                "id_crew_key" -> onIdChange(value.toString())
                // здесь меняем цвет иконки лямбдой, даже не создавая отдельную функцию
                "color_key" -> pref.icon?.setTint(Color.parseColor(value.toString()))
            }
            true
        }
    }

    //   функция смены текста в менюхе настроек на экране фрагмента сразу же
    private fun onTimeChange(value: String) {
        val nameArray = resources.getStringArray(R.array.loc_time_update_name)
        val valueArray = resources.getStringArray(R.array.loc_time_update_value)
        val title = timePref.title.toString().substringBefore(":")
        timePref.title = "$title: ${nameArray[valueArray.indexOf(value)]}"

    }

    private fun onServerTimeChange(value: String) {
        val nameArray = resources.getStringArray(R.array.server_time_update_name)
        val valueArray = resources.getStringArray(R.array.server_time_update_value)
        val title = serverTimePref.title.toString().substringBefore(":")
        serverTimePref.title = "$title: ${nameArray[valueArray.indexOf(value)]}"

    }

    private fun onIdChange(value: String) {
        val title = crewPref.title.toString().substringBefore(":")
        crewPref.title = "$title: ${value}"
    }


    //   настраиваем показ сохраненных цифр и всего прочего при запуске экрана с настройками
    private fun initPrefs() {
//        val id_pref = idPref.preferenceManager.sharedPreferences
        val crew_pref = crewPref.preferenceManager.sharedPreferences
        val pref = timePref.preferenceManager.sharedPreferences
        val nameArray = resources.getStringArray(R.array.loc_time_update_name)
        val valueArray = resources.getStringArray(R.array.loc_time_update_value)
        val title = timePref.title
        timePref.title =
            "$title: ${nameArray[valueArray.indexOf(pref?.getString("update_time_key", "5000"))]}"

        val serverPref = serverTimePref.preferenceManager.sharedPreferences
        val serverNameArray = resources.getStringArray(R.array.server_time_update_name)
        val serverValueArray = resources.getStringArray(R.array.server_time_update_value)
        val serverTitle = serverTimePref.title
        serverTimePref.title = "$serverTitle: ${
            serverNameArray[serverValueArray.indexOf(
                serverPref?.getString(
                    "server_update_key",
                    "5000"
                )
            )]
        }"

        val crew_title = crewPref.title
        crewPref.title = "$crew_title: ${crew_pref?.getString("id_crew_key", "0")}"

        val color = pref?.getString("color_key", "#0636C3")
        colorPref.icon?.setTint(Color.parseColor(color))
    }

}