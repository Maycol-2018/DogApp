package com.example.dogapp.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dogapp.R
import com.example.dogapp.databinding.ItemAppointmentBinding
import com.example.dogapp.model.Appointment

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

            val imageResource = binding.root.context.resources.getIdentifier(
                appointment.petImageName,
                "drawable",
                binding.root.context.packageName
            ).takeIf { it != 0 } ?: R.drawable.dog_head
            binding.petImage.setImageResource(imageResource)

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
