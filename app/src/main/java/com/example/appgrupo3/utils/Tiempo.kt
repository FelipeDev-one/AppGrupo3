package com.example.appgrupo3.utils

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object Tiempo {
    private val zona: ZoneId = ZoneId.systemDefault()
    private val formatoFecha = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    private val formatoFechaHora = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
    private val formatoHora = DateTimeFormatter.ofPattern("HH:mm")

    fun aFecha(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(zona).toLocalDate()

    fun fechaIso(millis: Long): String = aFecha(millis).toString()

    fun fecha(millis: Long): String = aFecha(millis).format(formatoFecha)

    fun fechaHora(millis: Long): String = Instant.ofEpochMilli(millis).atZone(zona).format(formatoFechaHora)

    fun hora(millis: Long): String = Instant.ofEpochMilli(millis).atZone(zona).format(formatoHora)

    fun fechaLegible(iso: String): String = LocalDate.parse(iso).format(formatoFecha)

    fun desdeLegible(texto: String): LocalDate? = try {
        LocalDate.parse(texto.trim(), formatoFecha)
    } catch (e: Exception) {
        null
    }

    fun millis(anio: Int, mes: Int, dia: Int, hora: Int, minuto: Int): Long =
        LocalDateTime.of(anio, mes, dia, hora, minuto).atZone(zona).toInstant().toEpochMilli()

    fun millisDe(fecha: LocalDate, hora: Int, minuto: Int): Long =
        fecha.atTime(hora, minuto).atZone(zona).toInstant().toEpochMilli()

    fun diasHabilesEntre(desde: LocalDate, hasta: LocalDate): Int {
        var dias = 0
        var dia = desde.plusDays(1)
        while (!dia.isAfter(hasta)) {
            if (dia.dayOfWeek != DayOfWeek.SATURDAY && dia.dayOfWeek != DayOfWeek.SUNDAY) dias++
            dia = dia.plusDays(1)
        }
        return dias
    }
}
