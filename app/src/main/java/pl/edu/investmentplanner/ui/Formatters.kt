package pl.edu.investmentplanner.ui

import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val polishLocale = Locale.forLanguageTag("pl-PL")
private val currencyFormatter = NumberFormat.getCurrencyInstance(polishLocale)
private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm")

fun formatAmount(amount: Double): String = currencyFormatter.format(amount)

fun formatDate(timestamp: Long): String = Instant.ofEpochMilli(timestamp)
    .atZone(ZoneId.systemDefault())
    .format(dateFormatter)

fun parsePositiveAmount(text: String): Double? = text
    .trim()
    .replace(',', '.')
    .toDoubleOrNull()
    ?.takeIf { it > 0.0 && it.isFinite() }

fun parseNonNegativeAmount(text: String): Double? = text
    .trim()
    .replace(',', '.')
    .toDoubleOrNull()
    ?.takeIf { it >= 0.0 && it.isFinite() }
