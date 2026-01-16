package com.asr.financial.presentation.ui.components.period

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.asr.financial.presentation.ui.constants.UIConstants.CARD_PADDING_DP
import com.asr.financial.presentation.ui.constants.UIConstants.SECTION_SPACING_DP
import com.asr.financial.presentation.ui.constants.UIConstants.TINY_SPACING_DP

/**
 * Reusable year dropdown selector
 * 
 * @param selectedYear Currently selected year
 * @param years List of available years
 * @param showDropdown Whether dropdown is expanded
 * @param onDropdownChange Callback when dropdown state changes
 * @param onYearSelected Callback when year is selected
 * @param label Label text for the dropdown
 * @param modifier Modifier for the component
 */
@Composable
fun YearDropdown(
    selectedYear: Int,
    years: List<Int>,
    showDropdown: Boolean,
    onDropdownChange: (Boolean) -> Unit,
    onYearSelected: (Int) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = TINY_SPACING_DP.dp)
        )
        Box {
            OutlinedCard(
                onClick = { onDropdownChange(true) },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = CARD_PADDING_DP.dp, vertical = SECTION_SPACING_DP.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedYear.toString(),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            DropdownMenu(
                expanded = showDropdown,
                onDismissRequest = { onDropdownChange(false) }
            ) {
                years.forEach { year ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = year.toString(),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        onClick = {
                            onYearSelected(year)
                            onDropdownChange(false)
                        }
                    )
                }
            }
        }
    }
}
