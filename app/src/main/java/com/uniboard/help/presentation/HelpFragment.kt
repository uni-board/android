package com.uniboard.help.presentation

import android.os.Bundle
import android.view.View
import com.uniboard.R
import com.uniboard.core.presentation.NavigationFragment
import com.uniboard.databinding.FragmentHelpBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation.findNavController
import kotlinx.serialization.Serializable

@Serializable
data object HelpDestination

class HelpFragment : NavigationFragment(R.layout.fragment_help) {
    private var binding: FragmentHelpBinding? = null
    private var adapter: InfoAdapter? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHelpBinding.bind(view)

        this.initAdapter()

    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }


    private fun initAdapter() {
        adapter = InfoAdapter(
            list = ItemsRepository.items,
            onClick = {
                val bundle = Bundle()
                binding?.run {
                    bundle.putString("ARG", id.toString())
                    navController.navigate(HelpDetailsDestination(id.toString()))
                }

            })


        binding?.run {
            rvItems.adapter = adapter
            rvItems.layoutManager = LinearLayoutManager(requireContext())
        }
    }
}