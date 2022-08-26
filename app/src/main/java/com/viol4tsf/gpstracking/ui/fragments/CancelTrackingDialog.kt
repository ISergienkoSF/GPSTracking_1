package com.viol4tsf.gpstracking.ui.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.viol4tsf.gpstracking.R

class CancelTrackingDialog : DialogFragment() {

    private var yListener: (() -> Unit)? = null

    //для использования функции из другого фрагмента
    fun setYListener(listener: () -> Unit){
        yListener = listener
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Выйти из прогулки?")
            .setMessage("Вы уверены что хотите покунить текущую прогулку? Все данные при этом будут удалены.")
            .setIcon(R.drawable.ic_baseline_delete_24)
            .setPositiveButton("Да") {_, _ ->
                yListener?.let { y ->
                    y()
                }
            }
            .setNegativeButton("Нет"){dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .create()


    }
}