package com.example.umeal.home.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.example.umeal.R
import com.example.umeal.auth.login.ui.LoginActivity
import com.example.umeal.databinding.FragmentHomeBinding
import com.example.umeal.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.buttonPrivacyPolice.setOnClickListener {
            startActivity(Intent(this.context, PrivacyPoliceActivity::class.java))
        }

        binding.buttonLogout.setOnClickListener {
            AlertDialog.Builder(requireActivity()).apply {
                setTitle("Log out!")
                setMessage("Are you sure you want to logout?")
                setPositiveButton("Log out") { _, _ ->

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

    companion object {

    }
}