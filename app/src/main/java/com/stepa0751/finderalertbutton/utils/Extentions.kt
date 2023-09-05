
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.asLiveData
import androidx.preference.PreferenceManager
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.stepa0751.finderalertbutton.db.Item
import com.stepa0751.finderalertbutton.db.MainDb
import org.json.JSONArray
import org.json.JSONObject

var offsetFromRequest: Long? = null

//// Функция для переключения между фрагментами
//fun Fragment.openFragment(f: Fragment){
//    (activity as AppCompatActivity).supportFragmentManager
//        .beginTransaction()
//        .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
//        .replace(R.id.placeHolder, f).commit()
//
//}


// Функция для переключения между активити, если мы решим добавить еще активити ???
// Этот код переключает и фрагменты с мэин на второй
//fun AppCompatActivity.openFragment(f: Fragment){
//
////    если какой-либо фрагмент открыт (приложение уже работает)
//    if(supportFragmentManager.fragments.isNotEmpty()){
//        //то проверяем не совпадает ли название фрагмента с тем, который мы хотим вызвать
//        //  если совпадает, то делаем ретерн и не продолжаем функцию!!!
//        if (supportFragmentManager.fragments[0].javaClass == f.javaClass) return
//    }
//    //    а если название не совпадает, то переключаемся на вызванный фрагмент
//    supportFragmentManager
//        .beginTransaction()
//        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
//        .replace(R.id.placeHolder, f).commit()
//}

fun Fragment.uploadToDatabase(updateId: Long, date: Long, text:String,
                     latitude: Float, longitude: Float, latitudeTxt: Float, longitudeTxt: Float, offset: Long){
    val db = MainDb.getDb(activity as AppCompatActivity)
    val item = Item(
        null,
        updateId,
        date,
        text,
        latitude,
        longitude,
        latitudeTxt,
        longitudeTxt,
        offset
    )
    Thread{
        db.getDao().insertItem(item)
        Log.d("MyLog", "Writing to db!")
    }.start()
}



 fun Fragment.receiveDataAndLocation() {
     val db = context?.let { MainDb.getDb(activity as AppCompatActivity) }

    val queue = Volley.newRequestQueue(context)
     val list = db?.getDao()?.getAllItem()?.asLiveData()?.observe(this){
         it.forEach{
              offsetFromRequest = it.offset
         }

     }

    var offset = ""
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

     if (offsetFromRequest!=null) {
        offset = "&offset=$offsetFromRequest"
    }


    val url = "https://api.telegram.org/bot${token_pref}/getUpdates?chat_id=-${chat_id_pref}$offset"
    Log.d("MyLog", "offset = $offsetFromRequest")
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
    var listingId: MutableList<Long> = mutableListOf()
    try {
        val mainObject = JSONObject(response)
        val ok = mainObject.get("ok")
        Log.d("MyLog", "JSON Response: ok = $ok")
        val result = mainObject.get("result") as JSONArray
        for (results in 0 until result.length()){
            val item = result[results] as JSONObject
            val updateId = item.get("update_id")
            val updateIdInt = updateId.toString().toLong()
            listingId.add(updateIdInt)
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
    offsetFromRequest = findMax(listingId)

}

fun findMax(list: List<Long>): Long {
    Log.d("MyLog", "Max in list: ${list.max()}")
    return list.max()
}

fun Fragment.findMin(list: List<Int>): Int {
    return list.min()
}

fun Fragment.findMax(list: List<Long>): Long {
    Log.d("MyLog", "Max in list: ${list.max()}")
    return list.max()
}



//Экстеншин функции для показа сообщений во фрагментах и MainActivity
fun Fragment.showToast(s: String){
    Toast.makeText(activity, s, Toast.LENGTH_SHORT).show()
}

fun AppCompatActivity.showToast(s: String){
    Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
}
//  Функция определения даны ли разрешения
fun Fragment.checkPermission(p: String): Boolean{
    return when(PackageManager.PERMISSION_GRANTED){
        ContextCompat.checkSelfPermission(activity as AppCompatActivity, p) -> true
        else -> false
    }
}