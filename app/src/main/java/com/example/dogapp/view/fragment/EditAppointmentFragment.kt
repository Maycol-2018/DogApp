package com.example.dogapp.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.dogapp.R
import com.example.dogapp.databinding.FragmentEditAppointmentBinding
import com.example.dogapp.model.Appointment
import com.example.dogapp.viewmodel.AppointmentViewModel
import com.example.dogapp.viewmodel.SaveState

// (Joel) HU5: ventana "Editar Cita".
// Recibe el id de la cita desde el Detalle, llena el formulario con sus datos actuales,
// valida los campos y guarda los cambios en Room por medio del ViewModel.
class EditAppointmentFragment : Fragment() {

    private var _binding: FragmentEditAppointmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AppointmentViewModel by viewModels()

    // (Joel) La cita que se está editando (tal como está guardada en Room)
    private var appointment: Appointment? = null

    // (Joel) Para llenar el formulario solo la primera vez: si Room vuelve a emitir la cita,
    // no se borra lo que el usuario ya escribió
    private var datosCargados = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditAppointmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    // (Joel) Cada parte de la pantalla en su propia función
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ajustarToolbar()
        controladores()
        observadorViewModel()
    }

    // (Joel) Con edge-to-edge la toolbar se baja el alto de la barra de estado para que el título no quede tapado
    private fun ajustarToolbar() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.editAppointmentToolbar) { toolbar, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            toolbar.updatePadding(top = bars.top)
            insets
        }
    }

    // (Joel) Eventos: validación, flecha de volver y botón Editar Cita
    private fun controladores() {
        validarDatos()

        // Flecha rosada: vuelve al Detalle de la Cita (el detalle sigue mostrando la mascota)
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.editAppointmentButton.setOnClickListener {
            editarCita()
        }
    }

    // (Joel) true solo si los 4 campos tienen algo escrito (los espacios solos no cuentan)
    private fun camposLlenos(): Boolean {
        return listOf(
            binding.petNameInput,
            binding.breedInput,
            binding.ownerInput,
            binding.phoneInput
        ).all { it.text.toString().isNotBlank() }
    }

    // (Joel) Cada vez que se escribe en un campo se revisa si se habilita el botón Editar Cita
    private fun validarDatos() {
        val campos = listOf(
            binding.petNameInput,
            binding.breedInput,
            binding.ownerInput,
            binding.phoneInput
        )
        for (campo in campos) {
            campo.addTextChangedListener {
                binding.editAppointmentButton.isEnabled = camposLlenos()
            }
        }
    }

    // (Joel) Se ejecuta al tocar "Editar Cita": le pasa los datos nuevos al ViewModel
    private fun editarCita() {
        val cita = appointment ?: return
        viewModel.updateAppointment(
            appointment = cita,
            petName = binding.petNameInput.text.toString().trim(),
            breed = binding.breedInput.text.toString().trim(),
            ownerName = binding.ownerInput.text.toString().trim(),
            ownerPhone = binding.phoneInput.text.toString().trim()
        )
    }

    private fun observadorViewModel() {
        observerAppointment()
        observerBreeds()
        observerUpdateState()
    }

    // (Joel) Trae la cita desde Room con el id que mandó el Detalle y llena el formulario
    private fun observerAppointment() {
        val appointmentId = requireArguments().getLong("appointmentId")
        viewModel.getAppointment(appointmentId).observe(viewLifecycleOwner) { cita ->
            if (cita != null) {
                appointment = cita
                if (!datosCargados) {
                    llenarFormulario(cita)
                    datosCargados = true
                }
            }
        }
    }

    private fun llenarFormulario(cita: Appointment) {
        binding.petNameInput.setText(cita.petName)
        // false = no abrir la lista de sugerencias al poner el texto
        binding.breedInput.setText(cita.breed, false)
        binding.ownerInput.setText(cita.ownerName)
        binding.phoneInput.setText(cita.ownerPhone)
    }

    // (Joel) Igual que en Nueva Cita: pide las razas a la API y las pone como sugerencias del campo Raza
    private fun observerBreeds() {
        viewModel.loadBreeds()
        viewModel.breeds.observe(viewLifecycleOwner) { breeds ->
            binding.breedInput.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    breeds
                )
            )
        }
    }

    // (Joel) Reacciona al estado de la edición que publica el ViewModel
    private fun observerUpdateState() {
        viewModel.updateState.observe(viewLifecycleOwner) { state ->
            when (state) {
                // Guardando: círculo de carga y botón bloqueado para evitar doble clic
                is SaveState.Loading -> {
                    binding.editProgress.isVisible = true
                    binding.editAppointmentButton.isEnabled = false
                }

                // Guardó bien: mensaje y volver al Home, donde se ve la cita modificada
                is SaveState.Success -> {
                    binding.editProgress.isVisible = false
                    Toast.makeText(
                        requireContext(),
                        R.string.edit_appointment_saved,
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().popBackStack(R.id.homeAdminFragment, false)
                }

                // Falló: mensaje y se vuelve a habilitar el botón para reintentar
                is SaveState.Error -> {
                    binding.editProgress.isVisible = false
                    binding.editAppointmentButton.isEnabled = camposLlenos()
                    Toast.makeText(
                        requireContext(),
                        R.string.edit_appointment_save_error,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                SaveState.Idle -> binding.editProgress.isVisible = false
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
