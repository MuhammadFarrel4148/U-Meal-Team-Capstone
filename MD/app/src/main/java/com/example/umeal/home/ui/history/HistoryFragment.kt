package com.example.umeal.home.ui.history

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.umeal.R
import com.example.umeal.adapter.HistoryResultAdapter
import com.example.umeal.data.ResultState
import com.example.umeal.data.repository.DataRepository
import com.example.umeal.data.response.ResponseHistory
import com.example.umeal.data.retrofit.ApiConfig
import com.example.umeal.databinding.FragmentHistoryBinding
import com.example.umeal.utils.PreferenceManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Calendar

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private lateinit var preferenceManager: PreferenceManager
    private var getHistory: Job = Job()
    private lateinit var viewModel: HistoryViewModel
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val apiService = ApiConfig.getApiService()
        val dataRepository = DataRepository(apiService)
        val factory = HistoryViewModelFactory(dataRepository)

        viewModel =
            ViewModelProvider(this, factory)[HistoryViewModel::class.java]

        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // Month is 0-based
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        binding.tvDate.text = String.format(
            resources.getString(R.string.date),
            day.toString(),
            month.toString(),
            year.toString()
        )
        binding.rvFoodHistory.layoutManager = LinearLayoutManager(requireActivity())

        preferenceManager = PreferenceManager(requireActivity())

        val auth = preferenceManager.token
        val id = preferenceManager.userId

        lifecycleScope.launch {
            if (getHistory.isActive) getHistory.cancel()
            getHistory = launch {
                viewModel.getHistory(auth, id, day.toString(), month.toString(), year.toString())
                    .collect { result ->

                        handleResult(result)
                    }
            }
        }


        binding.btnChooseDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireActivity(),
                { _, selectedYear, selectedMonth, selectedDay ->

                    binding.tvDate.text = String.format(
                        resources.getString(R.string.date),
                        selectedDay.toString(),
                        (selectedMonth + 1).toString(),
                        selectedYear.toString()
                    )
                    lifecycleScope.launch {
                        if (getHistory.isActive) getHistory.cancel()
                        getHistory = launch {
                            viewModel.getHistory(
                                auth,
                                id,
                                selectedDay.toString(),
                                (selectedMonth + 1).toString(),
                                selectedYear.toString()
                            ).collect { result ->
                                handleResult(result)
                            }
                        }
                    }
                },
                year, (month - 1), day
            )
            datePickerDialog.show()
        }
        return root
    }

    private fun handleResult(result: ResultState<ResponseHistory>) {
        when (result) {
            is ResultState.Loading -> setLoadingState(true)
            is ResultState.Error -> {
                setLoadingState(false)
                binding.rvFoodHistory.visibility = View.GONE
                binding.tvNoHistory.visibility = View.VISIBLE
                Toast.makeText(
                    requireActivity(),
                    result.data?.message ?: getString(R.string.no_history),
                    Toast.LENGTH_SHORT
                ).show()
            }

            is ResultState.Success -> {
                setLoadingState(false)
                binding.tvNoHistory.visibility = View.GONE
                binding.rvFoodHistory.visibility = View.VISIBLE
                val adapter = result.data?.data?.let { HistoryResultAdapter(it) }
                binding.rvFoodHistory.adapter = adapter
            }
        }
    }


    private fun setLoadingState(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}