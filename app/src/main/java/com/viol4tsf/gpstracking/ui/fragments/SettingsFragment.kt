package com.viol4tsf.gpstracking.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.viol4tsf.gpstracking.R
import com.viol4tsf.gpstracking.other.Constants.KEY_NAME
import com.viol4tsf.gpstracking.other.Constants.KEY_WEIGHT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_settings.*
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment: Fragment(R.layout.fragment_settings){

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadFieldsFromSharedPreferences()
        applyChangesButton.setOnClickListener{
            val success = applyChangesToSharedPreferences()
            if(success){
                Snackbar.make(view, "Изменения успешно применены", Snackbar.LENGTH_LONG).show()
            } else {
                Snackbar.make(view, "Пожалуйста, заполните все поля", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun loadFieldsFromSharedPreferences(){
        val name = sharedPreferences.getString(KEY_NAME, "")
        val weight = sharedPreferences.getFloat(KEY_WEIGHT, 65f)
        nameEditText.setText(name)
        weightEditText.setText(weight.toString())
    }

    //изменение настроек
    private fun applyChangesToSharedPreferences(): Boolean{
        val nameText = nameEditText.text.toString()
        val weightText = weightEditText.text.toString()
        if (nameText.isEmpty() || weightText.isEmpty()){
            return false
        }
        sharedPreferences.edit()
            .putString(KEY_NAME, nameText)
            .putFloat(KEY_WEIGHT, weightText.toFloat())
            .apply()
        val toolbarText = "Вперёд, $$nameText!"
        requireActivity().toolbarTitleTextView.text = toolbarText
        return true
    }
}