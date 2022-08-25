package com.viol4tsf.gpstracking.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viol4tsf.gpstracking.db.Run
import com.viol4tsf.gpstracking.repository.MainRepository
import kotlinx.coroutines.launch

//viewmodel для передачи данных в интерфейс из репозитория
class MainViewModel @ViewModelInject constructor(
    val mainRepository: MainRepository
): ViewModel(){

    val runsSortedByDate = mainRepository.getAllRunsSortedByDate()

    fun insertRun(run: Run) = viewModelScope.launch {
        mainRepository.insertRun(run)
    }
}