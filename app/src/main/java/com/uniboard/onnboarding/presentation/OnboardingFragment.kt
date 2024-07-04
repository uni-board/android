package com.uniboard.onnboarding.presentation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.uniboard.R
import com.uniboard.core.presentation.NavigationFragment
import com.uniboard.databinding.FragmentOnboardingBinding
import kotlinx.serialization.Serializable


@Serializable
data object OnboardingDestination

class OnboardingFragment: NavigationFragment(R.layout.fragment_onboarding) {
    private lateinit var binding: FragmentOnboardingBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentOnboardingBinding.bind(view)
    }
}