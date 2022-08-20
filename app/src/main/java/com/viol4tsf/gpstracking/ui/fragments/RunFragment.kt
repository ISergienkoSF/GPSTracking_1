package com.viol4tsf.gpstracking.ui.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.viol4tsf.gpstracking.R
import com.viol4tsf.gpstracking.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RunFragment: Fragment(R.layout.fragment_run){

    private val viewModel: MainViewModel by viewModels()
}