package com.example.dogapp.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dogapp.R
import com.example.dogapp.databinding.ItemAppointmentBinding
import com.example.dogapp.model.Appointment

import com.bumptech.glide.Glide // (Yair) HU3: carga la foto de la raza desde internet

class AppointmentAdapter(
    private val onItemClick: (Appointment) -> Unit
) : ListAdapter<Appointment, AppointmentAdapter.AppointmentViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val binding = ItemAppointmentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AppointmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AppointmentViewHolder(
        private val binding: ItemAppointmentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(appointment: Appointment) {
            binding.petName.text = appointment.petName
            binding.petSymptom.text = appointment.symptomDescription
            binding.appointmentTurn.text =
                binding.root.context.getString(R.string.appointment_turn_format, appointment.id)

            // (Yair) HU3: si la cita tiene foto de la API (petImageUrl) se descarga con Glide;
            // si no (citas de ejemplo), se usa la imagen local de siempre.
            if (appointment.petImageUrl.isNotBlank()) {
                Glide.with(binding.petImage)
                    .load(appointment.petImageUrl)
                    .placeholder(R.drawable.dog_head) // se muestra mientras descarga
                    .error(R.drawable.dog_head)       // se muestra si falla la descarga
                    .into(binding.petImage)
            } else {
                // Cancela cualquier descarga pendiente: el RecyclerView reutiliza las filas
                Glide.with(binding.petImage).clear(binding.petImage)
                val imageResource = binding.root.context.resources.getIdentifier(
                    appointment.petImageName,
                    "drawable",
                    binding.root.context.packageName
                ).takeIf { it != 0 } ?: R.drawable.dog_head
                binding.petImage.setImageResource(imageResource)
            }

            binding.root.setOnClickListener { onItemClick(appointment) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Appointment>() {
            override fun areItemsTheSame(oldItem: Appointment, newItem: Appointment): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Appointment, newItem: Appointment): Boolean =
                oldItem == newItem
        }
    }
}