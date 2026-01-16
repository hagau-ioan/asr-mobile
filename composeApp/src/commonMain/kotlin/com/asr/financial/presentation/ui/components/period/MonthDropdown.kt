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
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Reusable month dropdown selector
 * 
 * @param selectedMonthName Currently selected month name
 * @param months List of month numbers paired with their string resources
 * @param showDropdown Whether dropdown is expanded
 * @param onDropdownChange Callback when dropdown state changes
 * @param onMonthSelected Callback when month is selected
 * @param label Label text for the dropdown
 * @param modifier Modifier for the component
 */
@Composable
fun MonthDropdown(
    selectedMonthName: String,
    months: List<Pair<Int, StringResource>>,
    showDropdown: Boolean,
    onDropdownChange: (Boolean) -> Unit,
    onMonthSelected: (Int) -> Unit,
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
                        text = selectedMonthName,
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
                months.forEach { (monthNum, monthRes) ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(monthRes),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        onClick = {
                            onMonthSelected(monthNum)
                            onDropdownChange(false)
                        }
                    )
                }
            }
        }
    }
}
