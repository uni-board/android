package com.uniboard.help.presentation

import android.os.Bundle
import android.view.View
import com.uniboard.R
import com.uniboard.core.presentation.NavigationFragment
import com.uniboard.databinding.DetailsHelpInfoBinding
import kotlinx.serialization.Serializable


@Serializable
data class HelpDetailsDestination(val arg: String)

class DetailsHelpInfo: NavigationFragment(R.layout.details_help_info) {
    private var binding: DetailsHelpInfoBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DetailsHelpInfoBinding.bind(view)

        val id = arguments?.getString("arg")?:"ERROR"
        val music = ItemsRepository.items.find { it.id == id.toInt()}


        binding?.run {

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
