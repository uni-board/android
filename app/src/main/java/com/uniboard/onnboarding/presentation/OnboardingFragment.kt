package com.uniboard.onnboarding.presentation

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.uniboard.R
import com.uniboard.board.presentation.BoardDestination
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
        binding.run {
            button1.setOnClickListener {
                showMessageBoxConnect()
            }
            button2.setOnClickListener {
                showMessageBoxCreate()
            }
        }
    }
    fun showMessageBoxConnect(){
        val messageBoxView = LayoutInflater.from(activity).inflate(R.layout.fragment_dialog, null)
        val messageBoxBuilder = MaterialAlertDialogBuilder(requireActivity(), R.style.MaterialAlertDialog_rounded)
        //val messageBoxBuilder = AlertDialog.Builder(activity).setView(messageBoxView)
            .setView(messageBoxView)
            .setTitle(" Подключись через id")

        val btnConnect : Button = messageBoxView.findViewById(R.id.btnPositive)
        val id : TextInputEditText = messageBoxView.findViewById(R.id.editTitle)

        val  messageBoxInstance = messageBoxBuilder.show()
        btnConnect.setOnClickListener {
            navController.navigate(BoardDestination(id.toString()))
        }

        messageBoxView.setOnClickListener {
            messageBoxInstance.dismiss()
        }
    }
    fun showMessageBoxCreate(){
        val messageBoxView = LayoutInflater.from(activity).inflate(R.layout.fragment_dialog2, null)
        val messageBoxBuilder = MaterialAlertDialogBuilder(requireActivity(), R.style.MaterialAlertDialog_rounded)
            //val messageBoxBuilder = AlertDialog.Builder(activity).setView(messageBoxView)
            .setView(messageBoxView)
            .setTitle(" Введи название и описание")

        val btnYes : Button = messageBoxView.findViewById(R.id.btnPositive)

        val  messageBoxInstance = messageBoxBuilder.show()
        //btnYes.setOnClickListener {
        //}

        messageBoxView.setOnClickListener {
            messageBoxInstance.dismiss()
        }
    }
}