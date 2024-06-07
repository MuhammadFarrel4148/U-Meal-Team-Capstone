package com.example.umeal.home.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.umeal.R
import com.example.umeal.data.ResultState
import com.example.umeal.databinding.FragmentHomeBinding
import com.example.umeal.home.ViewModelFactory

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var dailyProgress: Double = 0.0
    private var userDailyCalories: Int = 0
    private var userFulfilledCalories: Int = 2500
    private var userTrimester: Int = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val factory: ViewModelFactory = ViewModelFactory.getInstance(requireActivity())
        val viewModel: HomeViewModel by viewModels {
            factory
        }
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setUserName()
        setUserTrimester()
        val articlesAdapter = ArticlesAdapter()

        viewModel.getUserDailyCalories(userTrimester)
        viewModel.dailyCalorieResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ResultState.Loading -> {
//                    showLoading(true)
                }

                is ResultState.Success -> {
                    viewModel.calories.observe(viewLifecycleOwner) { calories ->
                        userDailyCalories = calories
                        dailyProgress =
                            ((userFulfilledCalories.toDouble() / userDailyCalories.toDouble()) * 100)
                        binding.progressBar.progress = dailyProgress.toInt()
                        binding.progressBar.max = 100
                        binding.tvTrimester.text =
                            String.format(getString(R.string.trimester), userTrimester.toString())
                        binding.tvFulfilledCalories.text = String.format(
                            getString(R.string.calories_count),
                            userFulfilledCalories.toString(),
                            userDailyCalories.toString()
                        )
                    }
                }

                is ResultState.Error -> {
                    Toast.makeText(view?.context, result.error, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.getArticles().observe(viewLifecycleOwner) { result ->
            if (result != null) {
                when (result) {
                    is ResultState.Loading -> {
                        binding.loadingFrame.visibility = View.VISIBLE
                    }

                    is ResultState.Success -> {
                        if (result.data.isEmpty()) {
                            binding.loadingFrame.visibility = View.VISIBLE
                            binding.loadingBar.visibility = View.GONE
                            binding.tvNoArticle.visibility = View.VISIBLE
                        } else {
                            binding.loadingFrame.visibility = View.GONE
                            val articleData = result.data
                            articlesAdapter.submitList(articleData)
                        }
                    }

                    is ResultState.Error -> {
                        binding.loadingFrame.visibility = View.GONE
                        Toast.makeText(
                            requireContext(),
                            "Error fetching articles",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        binding.rvArticle.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter = articlesAdapter
        }
        return root
    }


    private fun setUserName() {
        val name = "Bella"
        binding.tvMomsUsername.text = String.format(resources.getString(R.string.moms_name), name)
    }

    private fun setUserTrimester() {
        userTrimester = 1
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}