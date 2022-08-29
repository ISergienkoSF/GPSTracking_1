package com.viol4tsf.gpstracking.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.viol4tsf.gpstracking.R
import com.viol4tsf.gpstracking.other.Constants.KEY_CALORIES
import com.viol4tsf.gpstracking.other.Constants.KEY_DISTANCE
import com.viol4tsf.gpstracking.other.Constants.KEY_HEIGHT
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
        val distance = sharedPreferences.getLong(
            KEY_DISTANCE, 2000L)
        val calories = sharedPreferences.getInt(KEY_CALORIES, 100)
        nameEditText.setText(name)
        weightEditText.setText(weight.toString())
        distanceEditText.setText(distance.toString())
        caloriesEditText.setText(calories.toString())
    }

    //изменение настроек
    private fun applyChangesToSharedPreferences(): Boolean{
        val nameText = nameEditText.text.toString()
        val weightText = weightEditText.text.toString()
        val distanceText = distanceEditText.text.toString()
        val caloriesText = caloriesEditText.text.toString()
        if (nameText.isEmpty() || weightText.isEmpty() ||
            distanceText.isEmpty() || caloriesText.isEmpty()){
            return false
        }
        sharedPreferences.edit()
            .putString(KEY_NAME, nameText)
            .putFloat(KEY_WEIGHT, weightText.toFloat())
            .putLong(KEY_DISTANCE, distanceText.toLong())
            .putInt(KEY_CALORIES, caloriesText.toInt())
            .apply()
        val toolbarText = "Вперёд, $nameText!"
        requireActivity().toolbarTitleTextView.text = toolbarText
        return true
    }
}