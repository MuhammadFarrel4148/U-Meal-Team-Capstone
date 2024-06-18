package com.example.umeal.home.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.umeal.R
import com.example.umeal.adapter.HistoryResultAdapter
import com.example.umeal.data.ResultState
import com.example.umeal.data.repository.DataRepository
import com.example.umeal.data.response.ResponseHistory
import com.example.umeal.data.response.ResultsItem
import com.example.umeal.data.retrofit.ApiConfig
import com.example.umeal.databinding.FragmentHomeBinding
import com.example.umeal.home.ViewModelFactory
import com.example.umeal.home.ui.history.HistoryViewModel
import com.example.umeal.home.ui.history.HistoryViewModelFactory
import com.example.umeal.utils.PreferenceManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var preferenceManager: PreferenceManager
    private var getHistory: Job = Job()
    private lateinit var historyViewModel: HistoryViewModel

    private var dailyProgress: Double = 0.0
    private var userDailyCalories: Int = 0


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val factory: ViewModelFactory = ViewModelFactory.getInstance(requireActivity())
        val viewModel: HomeViewModel by viewModels {
            factory
        }

        val apiService = ApiConfig.getApiService()
        val dataRepository = DataRepository(apiService)
        val historyFactory = HistoryViewModelFactory(dataRepository)

        historyViewModel =
            ViewModelProvider(this, historyFactory)[HistoryViewModel::class.java]

        preferenceManager = PreferenceManager(requireActivity())

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setUserName()
        Log.d(TAG, "preference user id: ${preferenceManager.userId}")
        Log.d(TAG, "preference user token: ${preferenceManager.token}")
        Log.d(TAG, "preference user name: ${preferenceManager.name}")
        val trimester = viewModel.countTrimester(
            preferenceManager.hphtDays,
            preferenceManager.hphtMonth,
            preferenceManager.hphtYear
        )

        val articlesAdapter = ArticlesAdapter()


        viewModel.getArticles().observe(viewLifecycleOwner) { result ->
            if (result != null) {
                when (result) {
                    is ResultState.Loading -> {
                        binding.loadingFrame.visibility = View.VISIBLE
                    }

                    is ResultState.Success -> {
                        if (result.data?.isEmpty() == true) {
                            binding.loadingFrame.visibility = View.VISIBLE
                            binding.loadingBar.visibility = View.GONE
                            binding.tvNoArticle.visibility = View.VISIBLE
                        } else {
                            binding.loadingFrame.visibility = View.GONE
                            val articleData = result.data
                            if (articleData != null) {
                                articlesAdapter.submitList(articleData)
                            }
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
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // Month is 0-based
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        binding.rvDailyScannedFood.layoutManager = LinearLayoutManager(requireActivity())

        val auth = preferenceManager.token
        val id = preferenceManager.userId

        lifecycleScope.launch {
            lifecycleScope.launch {
                if (getHistory.isActive) getHistory.cancel()
                getHistory = launch {
                    historyViewModel.getHistory(
                        auth,
                        id,
                        day.toString(),
                        month.toString(),
                        year.toString()
                    )
                        .collect { result ->
                            handleResult(result)
                        }
                }
            }
        }
        viewModel.getUserDailyCalories(trimester)
        binding.tvTrimester.text =
            String.format(getString(R.string.trimester), trimester.toString())
        viewModel.dailyCalorieResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ResultState.Loading -> {

                }

                is ResultState.Success -> {
                    viewModel.calories.observe(viewLifecycleOwner) { calories ->
                        userDailyCalories = calories

                    }
                }

                is ResultState.Error -> {
                    Toast.makeText(view?.context, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
        return root


    }


    private fun setUserName() {
        val name = preferenceManager.name
        binding.tvMomsUsername.text = String.format(resources.getString(R.string.moms_name), name)
    }

    private fun handleResult(result: ResultState<ResponseHistory>) {
        when (result) {
            is ResultState.Loading -> {

            }

            is ResultState.Error -> {
                binding.rvDailyScannedFood.visibility = View.GONE
                binding.tvNoHistory.visibility = View.VISIBLE
            }

            is ResultState.Success -> {
                binding.tvNoHistory.visibility = View.GONE
                binding.rvDailyScannedFood.visibility = View.VISIBLE
                val newCalorie = result.data?.data?.sumOf {
                    it.totalKalori
                } ?: 0
                val adapter = result.data?.data?.let { HistoryResultAdapter(it) }
                binding.rvDailyScannedFood.adapter = adapter
                updateCalorieProgress(newCalorie)
            }
        }
    }

    private fun updateCalorieProgress(newCalorie: Int) {
        if (userDailyCalories > 0) {
            dailyProgress =
                ((newCalorie.toDouble() / userDailyCalories.toDouble()) * 100)
            binding.progressBar.progress = dailyProgress.toInt()
            binding.progressBar.max = 100
            binding.tvFulfilledCalories.text = String.format(
                getString(R.string.calories_count),
                newCalorie.toString(),
                userDailyCalories.toString()
            )
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "home_fragment"
    }
}