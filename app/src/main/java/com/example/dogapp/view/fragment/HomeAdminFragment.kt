package com.example.dogapp.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogapp.R
import com.example.dogapp.databinding.FragmentHomeAdminBinding
import com.example.dogapp.view.adapter.AppointmentAdapter
import com.example.dogapp.viewmodel.AppointmentViewModel

class HomeAdminFragment : Fragment() {

    private var _binding: FragmentHomeAdminBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AppointmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appointmentAdapter = AppointmentAdapter { appointment ->
            findNavController().navigate(
                R.id.action_homeAdminFragment_to_appointmentDetailFragment,
                bundleOf("appointmentId" to appointment.id)
            )
        }

        binding.appointmentsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = appointmentAdapter
        }

        viewModel.appointments.observe(viewLifecycleOwner) { appointments ->
            appointmentAdapter.submitList(appointments)
        }

        binding.addAppointmentFab.setOnClickListener {
            findNavController().navigate(R.id.action_homeAdminFragment_to_newAppointmentFragment)
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().moveTaskToBack(true)
                }
            }
        )
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
