package de.dbauer.expensetracker.shared.model

import Constants

actual fun getSystemCurrencyCode(): String {
    return Constants.CURRENCY_CODE
}
