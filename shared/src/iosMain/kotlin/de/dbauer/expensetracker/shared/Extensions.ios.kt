package de.dbauer.expensetracker.shared

import Constants
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterMediumStyle
import platform.Foundation.NSLocale
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle
import platform.Foundation.NSNumberFormatterDecimalStyle
import platform.Foundation.NSTimeZone
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeZoneWithAbbreviation
import kotlin.time.Instant

// Formatters are expensive to create and these functions run for every list item on every
// recomposition, so they are created once. Formatting with them is thread safe.

// Amounts are always Brazilian Real, so they are formatted the Brazilian way ("R$ 1.234,56")
// independent of the device region.
private val brazilianLocale = NSLocale(localeIdentifier = "pt_BR")

private val utcTimeZone = requireNotNull(NSTimeZone.timeZoneWithAbbreviation("UTC"))

private val currencyFormatter =
    NSNumberFormatter().apply {
        numberStyle = NSNumberFormatterCurrencyStyle
        locale = brazilianLocale
        currencyCode = Constants.CURRENCY_CODE
    }

private val decimalFormatter =
    NSNumberFormatter().apply {
        numberStyle = NSNumberFormatterDecimalStyle
        locale = brazilianLocale
        minimumFractionDigits = 2uL
        maximumFractionDigits = 2uL
    }

// Dates are stored as UTC midnight (that is what the date pickers return), so they must be
// shown in UTC as well. In the local time zone of Brazil (UTC-3) they would show the day before.
private val dateFormatter =
    NSDateFormatter().apply {
        dateStyle = NSDateFormatterMediumStyle
        timeZone = utcTimeZone
    }

private val monthFormatter =
    NSDateFormatter().apply {
        // Stand-alone month name, the correct form when the month is shown without a day
        dateFormat = "LLLL"
        timeZone = utcTimeZone
    }

actual fun Float.toCurrencyString(currencyCode: String): String {
    return currencyFormatter.stringFromNumber(NSNumber(this)) ?: ""
}

actual fun Float.toLocalString(): String {
    return decimalFormatter.stringFromNumber(NSNumber(this)) ?: ""
}

/**
 * Parses a price typed by the user. Brazilian input uses ',' as decimal separator, but depending on
 * the device region the keyboard can offer '.', so both are accepted: "1.234,56", "1234,56",
 * "1234.56", "12,5" and "R$ 12,50" all work. A single '.' followed by exactly three digits is read
 * as a thousands separator ("1.500" is one thousand five hundred), as is usual in Brazil.
 */
actual fun String.toFloatLocaleAware(): Float? {
    val input = trim().removePrefix("R$").filterNot { it.isWhitespace() }
    val negative = input.startsWith("-")
    val number = input.removePrefix("-")
    if (number.isEmpty() || number.any { !it.isDigit() && it != ',' && it != '.' }) return null

    val lastComma = number.lastIndexOf(',')
    val lastDot = number.lastIndexOf('.')
    val isSingleDotDecimal = number.count { it == '.' } == 1 && number.length - lastDot - 1 != 3
    val decimalIndex =
        when {
            lastComma >= 0 && lastDot >= 0 -> maxOf(lastComma, lastDot)
            lastComma >= 0 -> if (number.count { it == ',' } == 1) lastComma else -1
            lastDot >= 0 -> if (isSingleDotDecimal) lastDot else -1
            else -> -1
        }
    val integerDigits = if (decimalIndex >= 0) number.substring(0, decimalIndex) else number
    val integerPart = integerDigits.filter { it.isDigit() }
    val fractionPart = if (decimalIndex >= 0) number.substring(decimalIndex + 1) else ""
    if (fractionPart.any { !it.isDigit() }) return null
    if (integerPart.isEmpty() && fractionPart.isEmpty()) return null

    val normalized = "${integerPart.ifEmpty { "0" }}.${fractionPart.ifEmpty { "0" }}"
    val value = normalized.toDoubleOrNull() ?: return null
    return (if (negative) -value else value).toFloat()
}

actual fun Instant.toLocaleString(): String {
    val date = NSDate.dateWithTimeIntervalSince1970(this.toEpochMilliseconds() / 1000.0)
    return dateFormatter.stringFromDate(date)
}

actual fun LocalDate.toMonthYearStringUTC(): String {
    val startOfMonth = LocalDate(year, month, 1).atStartOfDayIn(TimeZone.UTC)
    val date = NSDate.dateWithTimeIntervalSince1970(startOfMonth.toEpochMilliseconds() / 1000.0)
    return "${monthFormatter.stringFromDate(date)} $year"
}
