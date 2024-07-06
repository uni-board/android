package com.uniboard.help.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uniboard.R
import com.uniboard.core.presentation.NavigationFragment
import com.uniboard.databinding.DetailsHelpInfoBinding
import kotlinx.serialization.Serializable


@Serializable
data class HelpDetailsDestination(val arg: Int)

class DetailsHelpInfo : NavigationFragment(R.layout.details_help_info) {
    private var binding: DetailsHelpInfoBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DetailsHelpInfoBinding.inflate(inflater, container, false)
        return binding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DetailsHelpInfoBinding.bind(view)

        initAdapter()
    }





    private fun initAdapter() {
        val id = arguments?.getInt("arg") ?: -1
        val item = ItemsRepository.items.find { it.id == id }
        binding?.run {
//            detailText.text = item!!.detailedText
            detailText.text = "textasdddddddddddddddddddddddddddddtextasddddddddddddddddddddddddddddddddtextasddddddddddddddddddddddddddddddddddddddddtextasddddddddddddddddddddddddddddddddddddddddtextasddddddddddddddddddddddddddddddddddddddddtextasddddddddddddddddddddddddddddddddddddddddtextasddddddddddddddddddddddddddddddddddddddddtextasddddddddddddddddddddddddddddddddddddddddtextasdddddddddddddddddddddddddddddddddddddddddddddddddddtextasdddddddddddddddddddddddddddddddddddddddddddtextasdddddddddddddddddddddddddddddddddddddddddddtextasdddddddddddddddddddddddddddddddddddddddddddtextasdddddddddddddddddddddddddddddddddddddddddddtextasdddddddddddddddddddddddddddddddddddddddddddtextasdddddddddddddddddddddddddddddddddddddddddddtextasdddddddddddddddddddddddddddddddddddddddddddtextasdddddddddddddddddddddddddddddddddddddddddddtextasdddddddddddddddddddddddddddddddddddddddddddtextasdddddddddddddddddddddddddddddddddddddddddddtextasdddddddddddddddddddddddddddddddddddddddddddtextasdddddddddddddddddddddddddddddddddddddddddddtextasdddddddddddddddddddddddddddddddddddddddddddtextasdddddddddddddddddddddddddddddddddddddddddddtextasdddddddddddddddddddddddddddddddddddddddddddtextasdddddddddddddddddddddddddddddddddddddddddddtextasdddddddddddddddddddddddddddddddddddddddddddtextasdddddddddddddddddddddddddddddddddddddddddddtextasdddddddddddddddddddddddddddddddddddddddddddtextasdddddddddddddddddddddddddddddddddddddddddddtextasddddddddddddddddddddddddddddddddddddddddddddddddddddddddd"
            detailImage1.setImageResource(item!!.detailedImages[0])
            detailImage2.setImageResource(item.detailedImages[1])
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}

