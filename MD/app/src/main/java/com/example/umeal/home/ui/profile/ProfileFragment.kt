package com.example.umeal.home.ui.profile

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.umeal.R
import com.example.umeal.auth.SplashActivity
import com.example.umeal.auth.login.ui.LoginActivity
import com.example.umeal.databinding.FragmentHomeBinding
import com.example.umeal.databinding.FragmentProfileBinding
import com.example.umeal.utils.PreferenceManager
import java.util.Calendar

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferenceManager: PreferenceManager
    private val viewModel = ProfileViewModel()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        preferenceManager = PreferenceManager(requireActivity())

        binding.tvUserName.text = preferenceManager.name
        binding.tvUserEmail.text = preferenceManager.email
        binding.buttonPrivacyPolice.setOnClickListener {
            startActivity(Intent(this.context, PrivacyPoliceActivity::class.java))
        }

        val year = preferenceManager.hphtYear
        val month = preferenceManager.hphtMonth
        val day = preferenceManager.hphtDays

        binding.buttonChangeHPHT.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireActivity(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    preferenceManager.hphtDays = selectedDay
                    preferenceManager.hphtMonth = selectedMonth
                    preferenceManager.hphtYear = selectedYear
                    Toast.makeText(this.context, "Tanggal HPHT telah diubah", Toast.LENGTH_SHORT)
                        .show()
                },
                year, month, day
            )
            datePickerDialog.show()
        }

        binding.buttonLogout.setOnClickListener {
            AlertDialog.Builder(requireActivity()).apply {
                setTitle("Log out!")
                setMessage("Are you sure you want to logout?")
                setPositiveButton("Log out") { _, _ ->
                    preferenceManager.clear()
                    startActivity(Intent(this.context, SplashActivity::class.java))
                    requireActivity().finish()
                }
                setNegativeButton("No", null)
                create()
                show()
            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object
}