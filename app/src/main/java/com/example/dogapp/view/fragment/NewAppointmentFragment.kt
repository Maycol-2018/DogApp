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
import com.example.dogapp.databinding.FragmentNewAppointmentBinding
import com.example.dogapp.viewmodel.AppointmentViewModel
import com.example.dogapp.viewmodel.SaveState

// (Yair) HU3: ventana "Nueva Cita". Muestra el formulario, valida los campos,
// carga las razas desde la API y guarda la cita usando el ViewModel.
// El Fragment solo maneja la pantalla: no sabe nada de Room ni de Retrofit (eso es del Repository).
class NewAppointmentFragment : Fragment() {

    // (Yair) ViewBinding: acceso a las vistas del XML sin findViewById.
    // _binding puede ser null (cuando la vista se destruye); binding nunca lo es mientras la pantalla está viva.
    private var _binding: FragmentNewAppointmentBinding? = null
    private val binding get() = _binding!!

    // (Yair) El mismo AppointmentViewModel que usa Home: viewModels() lo crea o lo recupera
    private val viewModel: AppointmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewAppointmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    // (Yair) Aquí se arma la pantalla, cada cosa en su función para que se entienda rápido
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ajustarToolbar()
        configurarSpinner()
        controladores()
        observadorViewModel()
    }

    // (Yair) Con edge-to-edge (targetSdk 37) la app dibuja debajo de la barra de estado.
    // Le sumamos al padding de arriba de la toolbar el alto de esa barra para que el título no quede tapado.
    private fun ajustarToolbar() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.newAppointmentToolbar) { toolbar, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            toolbar.updatePadding(top = bars.top)
            insets
        }
    }

    // (Yair) Llena el Spinner con las opciones de symptoms_options (strings.xml)
    private fun configurarSpinner() {
        binding.symptomsSpinner.adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.symptoms_options,
            android.R.layout.simple_spinner_dropdown_item
        )
    }

    // (Yair) Eventos de la pantalla: validación, botón volver y botón guardar
    private fun controladores() {
        validarDatos()

        // Flecha de la barra: regresa a Home
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.saveAppointmentButton.setOnClickListener {
            guardarCita()
        }
    }

    // (Yair) true solo si los 4 campos de texto tienen algo escrito (los espacios solos no cuentan)
    private fun camposLlenos(): Boolean {
        return listOf(
            binding.petNameInput,
            binding.breedInput,
            binding.ownerInput,
            binding.phoneInput
        ).all { it.text.toString().isNotBlank() }
    }

    // (Yair) Cada vez que el usuario escribe en un campo, se revisa si ya están los 4 llenos
    // y según eso se habilita o deshabilita el botón Guardar.
    private fun validarDatos() {
        val campos = listOf(
            binding.petNameInput,
            binding.breedInput,
            binding.ownerInput,
            binding.phoneInput
        )
        for (campo in campos) {
            campo.addTextChangedListener {
                binding.saveAppointmentButton.isEnabled = camposLlenos()
            }
        }
    }

    // (Yair) Se ejecuta al tocar "Guardar Cita".
    private fun guardarCita() {
        // Posición 0 = "Síntomas" (texto guía): si sigue ahí, el usuario no eligió ningún síntoma
        if (binding.symptomsSpinner.selectedItemPosition == 0) {
            Toast.makeText(
                requireContext(),
                R.string.new_appointment_select_symptom,
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Se le pasa todo al ViewModel; él se encarga de guardar (la foto de la raza la busca el Repository)
        viewModel.saveAppointment(
            petName = binding.petNameInput.text.toString().trim(),
            breed = binding.breedInput.text.toString().trim(),
            ownerName = binding.ownerInput.text.toString().trim(),
            ownerPhone = binding.phoneInput.text.toString().trim(),
            symptom = binding.symptomsSpinner.selectedItem.toString()
        )
    }

    // (Yair) El Fragment "observa" al ViewModel: cuando sus datos cambian, la pantalla se actualiza sola
    private fun observadorViewModel() {
        observerBreeds()
        observerSaveState()
    }

    // (Yair) Pide las razas a la API y, cuando llegan, las pone como sugerencias del campo "Raza"
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

    // (Yair) Reacciona al estado del guardado (SaveState) que publica el ViewModel
    private fun observerSaveState() {
        viewModel.saveState.observe(viewLifecycleOwner) { state ->
            when (state) {
                // Guardando: círculo de carga visible y botón bloqueado para que no den doble clic
                is SaveState.Loading -> {
                    binding.saveProgress.isVisible = true
                    binding.saveAppointmentButton.isEnabled = false
                }

                // Guardó bien: mensaje y volver a Home
                is SaveState.Success -> {
                    binding.saveProgress.isVisible = false
                    Toast.makeText(
                        requireContext(),
                        R.string.new_appointment_saved,
                        Toast.LENGTH_SHORT
                    ).show()
                    // Home observa Room con Flow, así que la lista se actualiza sola.
                    findNavController().popBackStack()
                }

                // Falló: mensaje de error y se vuelve a habilitar el botón para reintentar
                is SaveState.Error -> {
                    binding.saveProgress.isVisible = false
                    binding.saveAppointmentButton.isEnabled = camposLlenos()
                    Toast.makeText(
                        requireContext(),
                        R.string.new_appointment_save_error,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                // Estado inicial: no hay nada que mostrar
                SaveState.Idle -> binding.saveProgress.isVisible = false
            }
        }
    }

    // (Yair) Se libera el binding al destruir la vista para evitar fugas de memoria
    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
