package com.uniboard.board_details.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.compose.runtime.Composable
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.uniboard.R
import com.uniboard.core.presentation.AndroidNavigationFragment
import com.uniboard.core.presentation.NavigationFragment
import com.uniboard.databinding.FragmentBoardDetailsBinding
import kotlinx.serialization.Serializable

@Serializable
data class BoardDetailsDestination(val id: String)

class BoardDetailsFragment : NavigationFragment(R.layout.fragment_board_details) {
    private lateinit var binding: FragmentBoardDetailsBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBoardDetailsBinding.bind(view)
        val id = arguments?.getString("id")
        composable<BoardDetailsDestination> {
            AnimatedNavigationFragment<BoardDetailsFragment>(
                navController = navController,
                arguments = it.arguments,
                scope = containerTransformScope(this),
                key = BoardDetailsDestination,
                boundsTransform = DefaultBoundsTransform
            ) {
                // init from Root Module
            }
        }

        init(id.toString())
    }


    fun init(id: String) {

        binding?.run {
            tvShareId?.text=id
            tvShareUrl?.text="https://api.uniboard-api.freemyip.com/board/$id"
            tvName?.text=""
            tvAbout?.text=""
            imageBack?.setOnClickListener {
                val messageBoxBuilder = MaterialAlertDialogBuilder(requireActivity(), R.style.MaterialAlertDialog_rounded)
                val  messageBoxInstance = messageBoxBuilder.show()
                val messageBoxView = LayoutInflater.from(activity).inflate(R.layout.fragment_board_details, null)



                messageBoxView.setOnClickListener {
                    messageBoxInstance.dismiss()
                }

            }
        }

    }
}