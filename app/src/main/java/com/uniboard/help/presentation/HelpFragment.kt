package com.uniboard.help.presentation

import android.os.Bundle
import android.view.View
import com.uniboard.R
import com.uniboard.core.presentation.NavigationFragment
import com.uniboard.databinding.FragmentHelpBinding
import kotlinx.serialization.Serializable

@Serializable
data object HelpDestination

class HelpFragment : NavigationFragment(R.layout.fragment_help) {
    private lateinit var binding: FragmentHelpBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHelpBinding.bind(view)
    }
}