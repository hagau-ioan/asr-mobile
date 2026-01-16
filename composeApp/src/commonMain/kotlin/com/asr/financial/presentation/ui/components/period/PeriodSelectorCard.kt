package com.asr.financial.presentation.ui.components.period

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.asr.financial.presentation.ui.constants.UIConstants.CARD_PADDING_DP
import com.asr.financial.presentation.ui.constants.UIConstants.SECTION_SPACING_DP
import org.jetbrains.compose.resources.StringResource

/**
 * Complete period selector card with year and month dropdowns
 * 
 * @param selectedYear Currently selected year
 * @param selectedMonth Currently selected month number
 * @param selectedMonthName Currently selected month name (localized)
 * @param years List of available years
 * @param months List of month numbers paired with their string resources
 * @param showYearDropdown Whether year dropdown is expanded
 * @param showMonthDropdown Whether month dropdown is expanded
 * @param onYearDropdownChange Callback when year dropdown state changes
 * @param onMonthDropdownChange Callback when month dropdown state changes
 * @param onYearSelected Callback when year is selected
 * @param onMonthSelected Callback when month is selected
 * @param title Title text for the card
 * @param yearLabel Label for year dropdown
 * @param monthLabel Label for month dropdown
 */
@Composable
fun PeriodSelectorCard(
    selectedYear: Int,
    selectedMonth: Int,
    selectedMonthName: String,
    years: List<Int>,
    months: List<Pair<Int, StringResource>>,
    showYearDropdown: Boolean,
    showMonthDropdown: Boolean,
    onYearDropdownChange: (Boolean) -> Unit,
    onMonthDropdownChange: (Boolean) -> Unit,
    onYearSelected: (Int) -> Unit,
    onMonthSelected: (Int) -> Unit,
    title: String,
    yearLabel: String,
    monthLabel: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(CARD_PADDING_DP.dp),
            verticalArrangement = Arrangement.spacedBy(SECTION_SPACING_DP.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SECTION_SPACING_DP.dp)
            ) {
                YearDropdown(
                    selectedYear = selectedYear,
                    years = years,
                    showDropdown = showYearDropdown,
                    onDropdownChange = onYearDropdownChange,
                    onYearSelected = onYearSelected,
                    label = yearLabel,
                    modifier = Modifier.weight(1f)
                )

                MonthDropdown(
                    selectedMonthName = selectedMonthName,
                    months = months,
                    showDropdown = showMonthDropdown,
                    onDropdownChange = onMonthDropdownChange,
                    onMonthSelected = onMonthSelected,
                    label = monthLabel,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
