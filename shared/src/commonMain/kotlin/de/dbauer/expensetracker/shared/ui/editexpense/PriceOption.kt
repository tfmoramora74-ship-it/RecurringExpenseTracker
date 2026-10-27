package de.dbauer.expensetracker.shared.ui.editexpense

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.dbauer.expensetracker.shared.toLocalString
import org.jetbrains.compose.resources.stringResource
import recurringexpensetracker.shared.generated.resources.Res
import recurringexpensetracker.shared.generated.resources.edit_expense_price

// Characters a price can be typed with; everything else is dropped while typing.
private val allowedPriceCharacters = setOf(',', '.', '-')

@Composable
fun PriceOption(
    price: String,
    onPriceChange: (String) -> Unit,
    priceInputError: Boolean,
    onNext: KeyboardActionScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(Res.string.edit_expense_price),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp),
        )
        ExpenseTextField(
            value = price,
            onValueChange = { newValue ->
                onPriceChange(newValue.filter { it.isDigit() || it in allowedPriceCharacters })
            },
            placeholder = 0f.toLocalString(),
            prefix = { Text(text = "R$ ") },
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next,
                ),
            keyboardActions =
                KeyboardActions(onNext = onNext),
            singleLine = true,
            isError = priceInputError,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
        )
    }
}
