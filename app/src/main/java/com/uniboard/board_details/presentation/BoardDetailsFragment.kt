package com.uniboard.board_details.presentation


import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.uniboard.R
import com.uniboard.board_details.domain.BoardSettings
import com.uniboard.board_details.domain.BoardSettingsRepository
import com.uniboard.board_details.presentation.utils.copyToClipboard
import com.uniboard.board_details.presentation.utils.createSnackbar
import com.uniboard.core.presentation.NavigationFragment
import com.uniboard.databinding.FragmentBoardDetailsBinding
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable


@Serializable
data class BoardDetailsDestination(val id: String)

class BoardDetailsFragment : NavigationFragment(R.layout.fragment_board_details) {
    var baseUrl: String? = null
        set(value) {
            if (baseUrl != null) {
                field = "$value/$baseUrl"
                binding.tvShareUrl.text = baseUrl
            } else {
                field = value
            }
        }


    var settingsRepository: BoardSettingsRepository? = null
        set(value) {
            field = value
            lifecycleScope.launch {
                settingsRepository?.get()?.onSuccess {
                    binding.tvName.setText(it.name)
                    binding.tvAbout.setText(it.description)

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


    private fun init(id: String) {

        binding.run {
            baseUrl = id
            tvShareId.text = id
            imageBack.setOnClickListener {
                navController.navigateUp()
            }
            tvShareId.setOnClickListener{
                context?.let { it1 -> copyToClipboard(it1, tvShareId.text) }
                createSnackbar(it, "Copied to the clipboard", Color.GREEN)
            }
            tvShareUrl.setOnClickListener {
                context?.let { it1 -> copyToClipboard(it1, tvShareUrl.text) }
                createSnackbar(it, "Copied to the clipboard", Color.GREEN)
            }
            imageButtonCopyId.setOnClickListener {
                context?.let { it1 -> copyToClipboard(it1, tvShareId.text) }
                createSnackbar(it, "Copied to the clipboard", Color.GREEN)
            }
            imageButtonCopyURL.setOnClickListener {
                context?.let { it1 -> copyToClipboard(it1, tvShareUrl.text) }
                createSnackbar(it, "Copied to the clipboard", Color.GREEN)
            }

            imageApply.setOnClickListener {
                lifecycleScope.launch {
                    settingsRepository?.update(
                        BoardSettings(
                            name = tvName.text.toString(),
                            description = tvAbout.text.toString()
                        )
                    )?.onSuccess { it2 -> createSnackbar(it, "Successfully saved", Color.GREEN) }
                }
            }
        }
    }
}