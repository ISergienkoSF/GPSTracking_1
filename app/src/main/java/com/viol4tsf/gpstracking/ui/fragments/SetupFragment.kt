package com.viol4tsf.gpstracking.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.viol4tsf.gpstracking.R
import com.viol4tsf.gpstracking.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.viol4tsf.gpstracking.other.Constants.KEY_HEIGHT
import com.viol4tsf.gpstracking.other.Constants.KEY_NAME
import com.viol4tsf.gpstracking.other.Constants.KEY_WEIGHT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_setup.*
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment: Fragment(R.layout.fragment_setup){

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    //для примитивных типов данных
    @set:Inject
    var isFirstTimeAppOpen = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //ужадение фрагмента из бэкстека, чтобы пользователь не мог вернуться на фрагмент
        if (!isFirstTimeAppOpen){
            val navOptions = NavOptions.Builder()
                    //выталкивание фрагмента
                .setPopUpTo(R.id.setupFragment, true)
                .build()
            findNavController().navigate(R.id.action_setupFragment_to_runFragment, savedInstanceState, navOptions)
        }

        continueTextView.setOnClickListener {
            val success = writePersonalDataToSharedPreferences()
            if (success) {
                findNavController().navigate(R.id.action_setupFragment_to_runFragment)
            } else{
                Snackbar.make(requireView(), "Пожалуйста, заполните все поля!", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    //запись данных о пользователе
    private fun writePersonalDataToSharedPreferences(): Boolean {
        val name = nameEditText.text.toString()
        val weight = weightEditText.text.toString()
        if (name.isEmpty() || weight.isEmpty()){
            return false
        }
        //вход в режим редактирования
        sharedPreferences.edit()
            .putString(KEY_NAME, name)
            .putFloat(KEY_WEIGHT, weight.toFloat())
            .putBoolean(KEY_FIRST_TIME_TOGGLE, false)
            .apply()
        val toolbarText = "Вперёд, $name!"
        requireActivity().toolbarTitleTextView.text = toolbarText
        return true
    }
}