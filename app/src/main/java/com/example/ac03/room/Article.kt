package com.example.ac03.room
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "article")
data class Article (
    @PrimaryKey  @ColumnInfo(name = "codi_article") var codiArticle: String,
    var descripcio: String, var familia: String?, var preuSenseIva: Float,
    @ColumnInfo(name = "estoc_activat") var estocActivat: Boolean,
    @ColumnInfo(name = "estoc_actual") var estocActual: Float?,

) : Serializable


