package com.example.umeal.home.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.umeal.R
import com.example.umeal.ResultState
import com.example.umeal.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var viewModel: HomeViewModel

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var dailyProgress: Double = 0.0
    private var userDailyCalories: Int = 0
    private var userFulfilledCalories: Int = 720
    private val userTrimester: Int = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setUserName()
        setCalorieProgress()
        return root
    }

    private fun setCalorieProgress() {
        viewModel.getUserDailyCalories(userTrimester)
        viewModel.dailyCalorieResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ResultState.Loading ->{
//                    showLoading(true)
                }
                is ResultState.Success -> {
                    viewModel.calories.observe(viewLifecycleOwner) { calories ->
                        userDailyCalories = calories
                        dailyProgress =
                            ((userFulfilledCalories.toDouble() / userDailyCalories.toDouble()) * 100)
                        binding.progressBar.progress = dailyProgress.toInt()
                        binding.progressBar.max = 100
                    }
                }

                is ResultState.Error -> {
                    Toast.makeText(view?.context, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setUserName() {
        val name = "Bella"
        binding.tvMomsUsername.text = String.format(resources.getString(R.string.moms_name), name)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}