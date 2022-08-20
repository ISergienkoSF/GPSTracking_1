package com.viol4tsf.gpstracking.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.viol4tsf.gpstracking.repository.MainRepository

//viewmodel для передачи данных в интерфейс из репозитория
class MainViewModel @ViewModelInject constructor(
    val mainRepository: MainRepository
): ViewModel(){
}