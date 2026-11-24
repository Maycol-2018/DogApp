package com.example.dogapp.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.dogapp.R
import com.example.dogapp.databinding.FragmentPlaceholderBinding

// (Allan) HU4: destino provisional de "Editar Cita" para que el botón azul del detalle ya navegue.
// La pantalla real (formulario de edición) se implementa en HU5; recibe el argumento "appointmentId".
class EditAppointmentFragment : Fragment() {

    private var _binding: FragmentPlaceholderBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaceholderBinding.inflate(inflater, container, false)
        binding.placeholderTitle.setText(R.string.edit_appointment_placeholder_title)
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
