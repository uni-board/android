package com.uniboard.onnboarding.presentation

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.uniboard.R
import com.uniboard.board.domain.RemoteObjectRepository
import com.uniboard.board.presentation.BoardDestination
import com.uniboard.board_details.presentation.domain.BoardSettings
import com.uniboard.board_details.presentation.domain.BoardSettingsRepository
import com.uniboard.core.presentation.NavigationFragment
import com.uniboard.databinding.FragmentOnboardingBinding
import com.uniboard.onnboarding.domain.BoardCreatorRepository
import com.uniboard.onnboarding.domain.RecentBoardsRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable


@Serializable
data object OnboardingDestination

class OnboardingFragment : NavigationFragment(R.layout.fragment_onboarding),
    CustomAdapter.OnItemClickListener {
    var recentsRepository: RecentBoardsRepository? = null
    var boardSettingsRepository: ((id: String) -> BoardSettingsRepository)? = null
    lateinit var repository: BoardCreatorRepository
    private lateinit var adapter: CustomAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var arrayList: ArrayList<ItemsViewModel>
    lateinit var heading: List<String>
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
            buttonOldBoards.setOnClickListener {
                showMessageBoxList()
            }
        }
    }

    fun showMessageBoxList() {
        val messageBoxView = LayoutInflater.from(activity).inflate(R.layout.dialog_list, null)
        val layoutManager = LinearLayoutManager(context)
        lifecycleScope.launch {
            dataInit()
            recyclerView = messageBoxView.findViewById(R.id.recycler_view)
            recyclerView.layoutManager = layoutManager
            recyclerView.setHasFixedSize(true)
            adapter = CustomAdapter(arrayList, this@OnboardingFragment, this@OnboardingFragment)
            recyclerView.adapter = adapter
            val messageBoxBuilder =
                MaterialAlertDialogBuilder(requireActivity(), R.style.MaterialAlertDialog_rounded)
                    .setView(messageBoxView)
                    .setTitle("Существующие id:")
            val messageBoxInstance = messageBoxBuilder.show()
            messageBoxView.setOnClickListener {
                messageBoxInstance.dismiss()
            }
        }
    }

    override fun onItemClick(position: Int, dataList: List<ItemsViewModel>) {
        lifecycleScope.launch { navController.navigate(BoardDestination(dataList[position].heading)) }
    }

    override fun onDelClick(position: Int, dataList: List<ItemsViewModel>) {
        lifecycleScope.launch { recentsRepository!!.removeBoard(dataList[position].heading) }
        recyclerView.adapter!!.notifyItemRemoved(position)
        val arrayyList = arrayListOf<ItemsViewModel>()
        for (i in dataList) {
            if (i != dataList[position]) {
                arrayyList.add(i)
            }
        }
        recyclerView.adapter = CustomAdapter(arrayyList, this, this)
    }

    fun showMessageBoxConnect() {
        val messageBoxView = LayoutInflater.from(activity).inflate(R.layout.fragment_dialog, null)
        val messageBoxBuilder =
            MaterialAlertDialogBuilder(requireActivity(), R.style.MaterialAlertDialog_rounded)
                .setView(messageBoxView)
                .setTitle(" Подключись через id")

        val btnConnect: Button = messageBoxView.findViewById(R.id.btnPositive)
        val textId: TextInputEditText = messageBoxView.findViewById(R.id.editTitle)
        val messageBoxInstance = messageBoxBuilder.show()
        btnConnect.setOnClickListener {
            lifecycleScope.launch {
                recentsRepository?.addBoard(textId.text.toString())
            }
            navController.navigate(BoardDestination(textId.text.toString()))
            messageBoxInstance.dismiss()
        }
        messageBoxView.setOnClickListener {
            messageBoxInstance.dismiss()
        }
    }

    fun showMessageBoxCreate() {
        val messageBoxView = LayoutInflater.from(activity).inflate(R.layout.fragment_dialog2, null)
        val messageBoxBuilder =
            MaterialAlertDialogBuilder(requireActivity(), R.style.MaterialAlertDialog_rounded)
                .setView(messageBoxView)
                .setTitle(" Введи название и описание")

        val btnConnect: Button = messageBoxView.findViewById(R.id.btnPositive)
        val id = runBlocking { repository.createBoard().getOrThrow() }
        val name: TextInputEditText = messageBoxView.findViewById(R.id.editTitle)
        val description: TextInputEditText = messageBoxView.findViewById(R.id.editDescr)
        val messageBoxInstance = messageBoxBuilder.show()
        btnConnect.setOnClickListener {
            lifecycleScope.launch {
                recentsRepository!!.addBoard(id)
                boardSettingsRepository?.invoke(id)?.update(
                    BoardSettings(name.text.toString(), description.text.toString())
                )
            }
            navController.navigate(BoardDestination(id))
            messageBoxInstance.dismiss()
        }
        messageBoxView.setOnClickListener {
            messageBoxInstance.dismiss()
        }
    }

    suspend fun getName(id: String): String? {
        return boardSettingsRepository?.invoke(id)!!.get().getOrThrow().name
    }

    private suspend fun dataInit() {
        arrayList = arrayListOf<ItemsViewModel>()
        val name = recentsRepository!!.getBoards().map { getName(it) }
        for (i in name) {
            val array = i?.let { ItemsViewModel(it) }
            if (array != null) {
                arrayList.add(array)
            }
        }
    }


//    private suspend fun dataInit() {
//        arrayList = arrayListOf<ItemsViewModel>()
//        heading = recentsRepository!!.getBoards()
//        for (i in heading.indices) {
//            val array = ItemsViewModel(heading[i])
//            arrayList.add(array)
//        }
//    }
}