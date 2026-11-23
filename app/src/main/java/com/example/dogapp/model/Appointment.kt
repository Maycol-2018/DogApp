package com.example.dogapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val petName: String,
    val symptomDescription: String,
    val petImageName: String = "dog_head"
)
