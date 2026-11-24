package com.example.dogapp.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.dogapp.R
import com.example.dogapp.databinding.FragmentAppointmentDetailBinding
import com.example.dogapp.model.Appointment
import com.example.dogapp.viewmodel.AppointmentViewModel

// (Allan) HU4: ventana "Detalle de la Cita".
// Recibe el id de la cita que se tocó en el Home, la busca en Room (por medio del ViewModel)
// y muestra toda su información. Desde aquí se puede eliminar la cita.
class AppointmentDetailFragment : Fragment() {

    private var _binding: FragmentAppointmentDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AppointmentViewModel by viewModels()

    // (Allan) La cita que se está mostrando; se usa al tocar eliminar
    private var appointment: Appointment? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppointmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    // (Allan) Cada parte de la pantalla en su propia función
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ajustarInsets()
        controladores()
        observerAppointment()
        observerDelete()
    }

    // (Allan) Con edge-to-edge la app dibuja debajo de las barras del sistema:
    // la toolbar se baja el alto de la barra de estado y los botones flotantes
    // suben el alto de la barra de navegación para que no queden tapados.
    private fun ajustarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.detailToolbar) { toolbar, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            toolbar.updatePadding(top = bars.top)
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.detailRoot) { root, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            root.updatePadding(bottom = bars.bottom)
            insets
        }
    }

    // (Allan) Eventos: flecha de volver y eliminar
    private fun controladores() {
        // Flecha rosada: regresa al Home
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        // Botón rosado: elimina la cita de Room (sin diálogo de confirmación, como dice la HU)
        binding.deleteFab.setOnClickListener {
            appointment?.let { viewModel.deleteAppointment(it) }
        }
    }

    // (Allan) Busca la cita por el id que mandó el Home y la pinta en pantalla.
    // Como viene de un Flow de Room, si la cita cambia la pantalla se actualiza sola.
    private fun observerAppointment() {
        val appointmentId = requireArguments().getLong("appointmentId")
        viewModel.getAppointment(appointmentId).observe(viewLifecycleOwner) { cita ->
            // null = la cita ya no existe (se acaba de eliminar): no hay nada que pintar
            if (cita != null) {
                appointment = cita
                mostrarCita(cita)
            }
        }
    }

    private fun mostrarCita(cita: Appointment) {
        binding.detailTitle.text = cita.petName
        binding.detailTurn.text = getString(R.string.appointment_turn_format, cita.id)
        binding.detailBreed.text = cita.breed
        binding.detailSymptom.text = cita.symptomDescription
        binding.detailOwner.text = getString(R.string.appointment_detail_owner_format, cita.ownerName)
        binding.detailPhone.text = getString(R.string.appointment_detail_phone_format, cita.ownerPhone)

        // Misma foto que se ve en el Home: la de la API con Glide, o la imagen local si no tiene
        if (cita.petImageUrl.isNotBlank()) {
            Glide.with(this)
                .load(cita.petImageUrl)
                .placeholder(R.drawable.dog_head) // mientras descarga
                .error(R.drawable.dog_head)       // si falla la descarga
                .into(binding.detailPetImage)
        } else {
            binding.detailPetImage.setImageResource(R.drawable.dog_head)
        }
    }

    // (Allan) Cuando el ViewModel avisa que ya borró la cita: mensaje y volver al Home.
    // Home observa Room con Flow, así que la cita eliminada desaparece sola de la lista.
    private fun observerDelete() {
        viewModel.appointmentDeleted.observe(viewLifecycleOwner) { deleted ->
            if (deleted) {
                Toast.makeText(
                    requireContext(),
                    R.string.appointment_detail_deleted,
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
