package com.uniboard.onnboarding.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.uniboard.R
import com.uniboard.board.domain.RemoteObjectRepository
import com.uniboard.board.presentation.BoardDestination
import com.uniboard.core.presentation.NavigationFragment
import com.uniboard.databinding.FragmentOnboardingBinding
import com.uniboard.onnboarding.domain.BoardCreatorRepository
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable


@Serializable
data object OnboardingDestination

class OnboardingFragment: NavigationFragment(R.layout.fragment_onboarding) {
    lateinit var repository: BoardCreatorRepository
    private lateinit var adapter: CustomAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var arrayList: ArrayList<ItemsViewModel>
    lateinit var heading: Array<String>
    lateinit var objectRepository: (String) -> RemoteObjectRepository
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
            buttonOldBoards?.setOnClickListener {
                showMessageBoxList()
            }
        }
    }
    fun showMessageBoxList(){
        val messageBoxView = LayoutInflater.from(activity).inflate(R.layout.dialog_list, null)
        val layoutManager = LinearLayoutManager(context)
        dataInit()
        recyclerView = messageBoxView.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)
        adapter = CustomAdapter(arrayList)
        recyclerView.adapter = adapter
        val messageBoxBuilder = MaterialAlertDialogBuilder(requireActivity(), R.style.MaterialAlertDialog_rounded)
            .setView(messageBoxView)
            .setTitle("Существующие id:")
        val messageBoxInstance = messageBoxBuilder.show()
        messageBoxView.setOnClickListener {
            messageBoxInstance.dismiss()
        }
    }
    fun showMessageBoxConnect(){
        val messageBoxView = LayoutInflater.from(activity).inflate(R.layout.fragment_dialog, null)
        val messageBoxBuilder = MaterialAlertDialogBuilder(requireActivity(), R.style.MaterialAlertDialog_rounded)
            .setView(messageBoxView)
            .setTitle(" Подключись через id")

        val btnConnect : Button = messageBoxView.findViewById(R.id.btnPositive)
        val textId: TextInputEditText = messageBoxView.findViewById(R.id.editTitle)
        val messageBoxInstance = messageBoxBuilder.show()
        btnConnect.setOnClickListener {
            navController.navigate(BoardDestination(textId.text.toString()))
        }
        messageBoxView.setOnClickListener {
            messageBoxInstance.dismiss()
        }
    }
    fun showMessageBoxCreate(){
        val messageBoxView = LayoutInflater.from(activity).inflate(R.layout.fragment_dialog2, null)
        val messageBoxBuilder = MaterialAlertDialogBuilder(requireActivity(), R.style.MaterialAlertDialog_rounded)
            .setView(messageBoxView)
            .setTitle(" Введи название и описание")

        val btnConnect : Button = messageBoxView.findViewById(R.id.btnPositive)
        val id = runBlocking { repository.createBoard().getOrThrow() }
        val  messageBoxInstance = messageBoxBuilder.show()
        btnConnect.setOnClickListener {
            navController.navigate(BoardDestination(id))
            com.google.android.material.snackbar.Snackbar.make(messageBoxView, id, com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
        }
        messageBoxView.setOnClickListener {
            messageBoxInstance.dismiss()
        }
    }
    // NEED TO IMPLEMENT PUTTING AN ACTUAL ID: FOR NOW IT`S PLACEHOLDER
    private fun dataInit() {
        arrayList = arrayListOf<ItemsViewModel>()
        heading = arrayOf("superduperid", "megaidultra", "abracadabra", "obamaid", "ddfjfdjd")
        for (i in heading.indices) {
            val array = ItemsViewModel(heading[i])
            arrayList.add(array)
        }
    }
}