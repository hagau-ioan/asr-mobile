package com.asr.financial.presentation.screens.utilities.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.asr.financial.presentation.screens.charts.LineChart
import com.asr.financial.presentation.screens.utilities.YearlyUtilityData
import com.asr.financial.presentation.screens.utilities.UtilitiesConstants.CHART_HEIGHT_DP
import com.asr.financial.presentation.screens.utilities.UtilitiesConstants.LEGEND_ICON_SIZE_DP
import com.asr.financial.presentation.theme.ChartSecondaryGreen
import com.asr.financial.presentation.ui.constants.UIConstants.CARD_PADDING_DP
import com.asr.financial.presentation.ui.constants.UIConstants.SECTION_SPACING_DP
import com.asr.financial.presentation.ui.constants.UIConstants.SMALL_SPACING_DP
import com.asr.financial.presentation.ui.constants.UIConstants.TINY_SPACING_DP
import asr_financial.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * Card showing year-over-year utility comparison with line chart
 */
@Composable
fun YearlyComparisonCard(
    yearlyData: List<YearlyUtilityData>,
    comparisonYear: Int,
    minYear: Int,
    maxYear: Int,
    onYearChange: (Int) -> Unit
) {
    val isDarkMode = isSystemInDarkTheme()
    val secondaryLegendColor = if (isDarkMode) 
        androidx.compose.ui.graphics.Color.White 
    else 
        ChartSecondaryGreen

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(CARD_PADDING_DP.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.utilities_yearly_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(TINY_SPACING_DP.dp))
                    Text(
                        text = stringResource(Res.string.utilities_yearly_subtitle, comparisonYear, comparisonYear - 1),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(SMALL_SPACING_DP.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onYearChange(comparisonYear - 1) },
                        enabled = comparisonYear > minYear
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.cd_previous_year)
                        )
                    }
                    IconButton(
                        onClick = { onYearChange(comparisonYear + 1) },
                        enabled = comparisonYear < maxYear
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = stringResource(Res.string.cd_next_year)
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(SECTION_SPACING_DP.dp))

            val currentYearData = yearlyData.map { it.currentYearAmount }
            val previousYearData = yearlyData.map { it.previousYearAmount }

            LineChart(
                currentYearData = currentYearData,
                previousYearData = previousYearData,
                currentYearLabel = comparisonYear.toString(),
                previousYearLabel = (comparisonYear - 1).toString(),
                height = CHART_HEIGHT_DP
            )
            
            Spacer(Modifier.height(SMALL_SPACING_DP.dp))
            
            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(TINY_SPACING_DP.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(LEGEND_ICON_SIZE_DP.dp)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = comparisonYear.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(Modifier.width(SECTION_SPACING_DP.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(TINY_SPACING_DP.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(LEGEND_ICON_SIZE_DP.dp)
                            .background(secondaryLegendColor)
                    )
                    Text(
                        text = (comparisonYear - 1).toString(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
