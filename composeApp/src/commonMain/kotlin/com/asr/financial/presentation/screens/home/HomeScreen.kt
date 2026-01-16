package com.asr.financial.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.asr.financial.presentation.mvi.event.HomeEvent
import com.asr.financial.presentation.mvi.state.HomeState
import com.asr.financial.presentation.mvi.viewmodel.HomeViewModel
import com.asr.financial.presentation.ui.components.BreadcrumbItem
import com.asr.financial.presentation.ui.responsive.WindowSizeClass
import com.asr.financial.presentation.ui.scaffold.ScreenLayout
import com.asr.financial.utils.formatCurrency
import asr_financial.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Home Screen matching Financial Tracking App design
 */
@Composable
fun HomeScreen(
    windowSizeClass: WindowSizeClass,
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit = {},
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var selectedYear by remember { mutableStateOf(2025) }
    var selectedMonth by remember { mutableStateOf("Decembrie") }
    
    val months = listOf(
        "Ianuarie", "Februarie", "Martie", "Aprilie", "Mai", "Iunie",
        "Iulie", "August", "Septembrie", "Octombrie", "Noiembrie", "Decembrie"
    )
    
    ScreenLayout(
        windowSizeClass = windowSizeClass,
        breadcrumbItems = listOf(BreadcrumbItem(stringResource(Res.string.nav_home))),
        selectedMonth = selectedMonth,
        selectedYear = selectedYear,
        onNavigate = onNavigate,
        onMenuClick = onMenuClick
    ) {
                // Period Selector Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.home_select_period),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Year selector
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(Res.string.home_year),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(4.dp))
                                OutlinedButton(
                                    onClick = { /* TODO: Show year picker */ },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(selectedYear.toString())
                                }
                            }
                            
                            // Month selector
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(Res.string.home_month),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(4.dp))
                                OutlinedButton(
                                    onClick = { /* TODO: Show month picker */ },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(selectedMonth)
                                }
                            }
                        }
                    }
                }
            }
            
            // Statistics Cards
            item {
                when (val state = uiState) {
                    is HomeState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    
                    is HomeState.Success -> {
                        StatisticsCards(
                            totalExpenses = state.totalExpenses,
                            totalIncome = state.totalIncome,
                            balance = state.balance,
                            selectedMonth = selectedMonth,
                            selectedYear = selectedYear
                        )
                    }
                    
                    is HomeState.Error -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
}

/**
 * Statistics Cards - 4 cards in grid
 */
@Composable
private fun StatisticsCards(
    totalExpenses: Double,
    totalIncome: Double,
    balance: Double,
    selectedMonth: String,
    selectedYear: Int
) {
    val perPublisher = totalExpenses / 785
    val missingCongregations = 0 // TODO: Calculate from data
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: Expenses and Donations
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Monthly Expenses
            StatCard(
                title = stringResource(Res.string.stat_monthly_expenses),
                amount = totalExpenses.formatCurrency(),
                amountColor = MaterialTheme.colorScheme.error,
                subtitle = "$selectedMonth $selectedYear",
                modifier = Modifier.weight(1f)
            )
            
            // Monthly Donations
            StatCard(
                title = stringResource(Res.string.stat_monthly_donations),
                amount = totalIncome.formatCurrency(),
                amountColor = MaterialTheme.colorScheme.tertiary,
                subtitle = stringResource(Res.string.stat_balance, balance.formatCurrency()),
                subtitleColor = if (balance >= 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Row 2: Per Publisher and Missing Congregations
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Per Publisher Contribution
            StatCard(
                title = stringResource(Res.string.stat_per_publisher),
                amount = perPublisher.formatCurrency(),
                amountColor = MaterialTheme.colorScheme.primary,
                subtitle = stringResource(Res.string.stat_for_publishers, 785),
                modifier = Modifier.weight(1f)
            )
            
            // Missing Congregations
            StatCard(
                title = stringResource(Res.string.stat_missing_congregations),
                amount = missingCongregations.toString(),
                amountColor = if (missingCongregations > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
                subtitle = stringResource(Res.string.stat_of_congregations, 8),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Individual Stat Card
 */
@Composable
private fun StatCard(
    title: String,
    amount: String,
    amountColor: Color,
    subtitle: String,
    modifier: Modifier = Modifier,
    subtitleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = amount,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = subtitleColor
            )
        }
    }
}
