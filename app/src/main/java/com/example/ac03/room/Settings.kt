package com.example.ac03.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class Settings(
    @PrimaryKey val id: Int = 1, // Se asume que solo hay una fila en la tabla de configuración
    @ColumnInfo(name = "iva") var iva: Float
)
