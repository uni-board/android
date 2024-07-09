package com.uniboard.board_details.presentation

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.uniboard.R
import com.uniboard.board_details.presentation.domain.BoardSettings
import com.uniboard.board_details.presentation.domain.BoardSettingsRepository
import com.uniboard.core.presentation.NavigationFragment
import com.uniboard.databinding.FragmentBoardDetailsBinding
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class BoardDetailsDestination(val id: String)

class BoardDetailsFragment : NavigationFragment(R.layout.fragment_board_details) {
    var baseUrl: String? = null
        set(value) {
            if (baseUrl!=null) {
                field = value + "/" + baseUrl
                binding.tvShareUrl?.text = baseUrl
            }
            else{
                field = value
            }
        }


    var settingsRepository: BoardSettingsRepository? = null
        set(value) {
            field=value
            lifecycleScope.launch {
                settingsRepository?.get()?.onSuccess {
                    binding.tvName?.text = it.name
                    binding.tvAbout?.text = it.description

                }
            }
        }

    private lateinit var binding: FragmentBoardDetailsBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBoardDetailsBinding.bind(view)

        val id = arguments?.getString("id")
        init(id.toString())
    }


    fun init(id: String) {

        binding.run {
            baseUrl=id
            tvShareId?.text = id
            imageBack?.setOnClickListener {
                navController.navigateUp()
            }
        }

    }
}