package com.stepa0751.finderalertbutton.utils

import android.app.AlertDialog
import android.content.Context
import com.stepa0751.finderalertbutton.R

object DialogManager {
//    Функция вызова диалога о включении GPS
    fun showLocEnableDialog(context: Context, listener: Listener) {
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()
        dialog.setTitle(R.string.Location_disabled)
        dialog.setMessage(context.getString(R.string.Location_dialog_message))
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes") { _, _ -> listener.onClick()
        }
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No") { _, _ -> dialog.dismiss()
        }
        dialog.show()
    }
//    Интерфейс слушателя вызова диалога о включении GPS
    interface Listener{
        fun onClick()
    }
}