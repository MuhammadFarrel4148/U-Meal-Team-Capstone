package com.example.umeal.home.ui.dashboard

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.umeal.R
import com.example.umeal.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    @SuppressLint("DefaultLocale")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // set trimester dropdown
        val trimesterItems = listOf(1, 2, 3)
        val trimesterAdapter =
            ArrayAdapter(requireContext(), R.layout.dropdown_item, trimesterItems)
        binding.edtTrimester.setAdapter(trimesterAdapter)

        // set factor activity dropdown
        val factorActivityItems = resources.getStringArray(R.array.factor_activity_array)
        val factorActivityAdapter =
            ArrayAdapter(requireContext(), R.layout.dropdown_item, factorActivityItems)
        binding.edtActivityFactor.setAdapter(factorActivityAdapter)

        // set factor stress dropdown
        val factorStressItems = resources.getStringArray(R.array.factor_stress_array)
        val factorStressAdapter =
            ArrayAdapter(requireContext(), R.layout.dropdown_item, factorStressItems)
        binding.edtStressFactor.setAdapter(factorStressAdapter)



        binding.btnCalculate.setOnClickListener {
            val bb = binding.edtWeight.text.toString().toDoubleOrNull() ?: 0.0
            val tb = binding.edtHeight.text.toString().toDoubleOrNull() ?: 0.0
            val age = binding.edtAge.text.toString().toDoubleOrNull() ?: 0.0
            val tr = binding.edtTrimester.text.toString().toIntOrNull() ?: 0
            val fActivity = binding.edtActivityFactor.text.toString()
            val fStress = binding.edtActivityFactor.text.toString()
            if (bb == 0.0 || tb == 0.0 || age == 0.0 || tr == 0 || fActivity.isEmpty() || fActivity.isEmpty()) {
                Toast.makeText(this.context, getString(R.string.warning_input), Toast.LENGTH_SHORT)
                    .show()
                Log.d(TAG, "onCreateView: $tr , $bb, $tb , $age, $tr , $fActivity, $fStress, ")
            } else {
                val totalCal = dashboardViewModel.countCalorie(bb, tb, age, tr, fActivity, fStress)
                val formattedValue = String.format("%.2f", totalCal)
                binding.tvCalorie.text = formattedValue
            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "DashboardFragment"
    }
}