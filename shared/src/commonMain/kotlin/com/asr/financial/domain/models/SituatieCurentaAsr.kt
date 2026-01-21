package com.asr.financial.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Current ASR financial situation data
 * Loaded from Firebase Storage: app/situatie_curenta_asr.json
 */
@Serializable
data class SituatieCurentaAsr(
    @SerialName("total_disponibil")
    val totalDisponibil: Double,
    @SerialName("incasare_asr")
    val incasareAsr: Double,
    @SerialName("plata_asr")
    val plataAsr: Double,
    @SerialName("disponibil_cg_initial")
    val disponibilCgInitial: Double,
    @SerialName("incasare_cg")
    val incasareCg: Double,
    @SerialName("disponibil_cg_final")
    val disponibilCgFinal: Double,
    @SerialName("total_asr")
    val totalAsr: Double,
    val month: Int,
    val year: Int,
    @SerialName("end_date")
    val endDate: String,
    @SerialName("disponibil_cg_month")
    val disponibilCgMonth: Int,
    @SerialName("disponibil_cg_year")
    val disponibilCgYear: Int
)
