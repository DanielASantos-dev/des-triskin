package br.com.triskin.presentation.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ptBr = Locale("pt", "BR")

private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", ptBr)
private val shortDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM", ptBr)
private val dateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MM 'às' HH:mm", ptBr)

fun LocalTime.formatHm(): String = format(timeFormatter)
fun LocalDateTime.formatHm(): String = format(timeFormatter)
fun LocalDate.formatShort(): String = format(shortDateFormatter)
fun LocalDateTime.formatShortDateTime(): String = format(dateTimeFormatter)
