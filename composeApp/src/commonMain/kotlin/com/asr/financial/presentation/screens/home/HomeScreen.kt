package com.asr.financial.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.asr.financial.domain.model.TransactionType
import com.asr.financial.platform.Clock
import com.asr.financial.presentation.mvi.event.HomeEvent
import com.asr.financial.presentation.mvi.state.HomeState
import com.asr.financial.presentation.mvi.viewmodel.HomeViewModel
import com.asr.financial.presentation.ui.components.BreadcrumbItem
import com.asr.financial.presentation.ui.responsive.WindowSizeClass
import com.asr.financial.presentation.ui.scaffold.ScreenLayout
import com.asr.financial.utils.formatCurrency
import com.asr.financial.utils.getCurrentYear
import com.asr.financial.utils.getCurrentMonth
import asr_financial.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * Home Screen matching Financial Tracking App design
 */
@Composable
fun HomeScreen(
    windowSizeClass: WindowSizeClass,
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit = {},
    viewModel: HomeViewModel = koinViewModel(),
    clock: Clock = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var selectedYear by remember { mutableStateOf(getCurrentYear(clock)) }
    var selectedMonth by remember { mutableStateOf(getCurrentMonth(clock)) } // December
    var showYearDropdown by remember { mutableStateOf(false) }
    var showMonthDropdown by remember { mutableStateOf(false) }
    
    val years = listOf(2024, 2025, 2026)
    val months = listOf(
        1 to stringResource(Res.string.month_january),
        2 to stringResource(Res.string.month_february),
        3 to stringResource(Res.string.month_march),
        4 to stringResource(Res.string.month_april),
        5 to stringResource(Res.string.month_may),
        6 to stringResource(Res.string.month_june),
        7 to stringResource(Res.string.month_july),
        8 to stringResource(Res.string.month_august),
        9 to stringResource(Res.string.month_september),
        10 to stringResource(Res.string.month_october),
        11 to stringResource(Res.string.month_november),
        12 to stringResource(Res.string.month_december)
    )
    
    val selectedMonthName = months.find { it.first == selectedMonth }?.second ?: ""
    
    ScreenLayout(
        windowSizeClass = windowSizeClass,
        breadcrumbItems = listOf(BreadcrumbItem(stringResource(Res.string.nav_home))),
        selectedMonth = selectedMonthName,
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
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Box {
                                    OutlinedCard(
                                        onClick = { showYearDropdown = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.outlinedCardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
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
                                        expanded = showYearDropdown,
                                        onDismissRequest = { showYearDropdown = false }
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
                                                    selectedYear = year
                                                    showYearDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            
                            // Month selector
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(Res.string.home_month),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Box {
                                    OutlinedCard(
                                        onClick = { showMonthDropdown = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.outlinedCardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
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
                                        expanded = showMonthDropdown,
                                        onDismissRequest = { showMonthDropdown = false }
                                    ) {
                                        months.forEach { (monthNum, monthName) ->
                                            DropdownMenuItem(
                                                text = { 
                                                    Text(
                                                        text = monthName,
                                                        style = MaterialTheme.typography.bodyLarge
                                                    ) 
                                                },
                                                onClick = {
                                                    selectedMonth = monthNum
                                                    showMonthDropdown = false
                                                }
                                            )
                                        }
                                    }
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
                        // Filter transactions by selected year and month
                        val filteredTransactions = state.transactions.filter { transaction ->
                            transaction.getYear() == selectedYear && transaction.getMonth() == selectedMonth
                        }
                        
                        val filteredIncome = filteredTransactions
                            .filter { it.type == TransactionType.INCOME }
                            .sumOf { it.amount }
                        
                        val filteredExpenses = filteredTransactions
                            .filter { it.type == TransactionType.EXPENSE }
                            .sumOf { it.amount }
                        
                        val filteredBalance = filteredIncome - filteredExpenses
                        
                        // Calculate year totals
                        val yearTransactions = state.transactions.filter { it.getYear() == selectedYear }
                        val yearIncome = yearTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                        val yearExpenses = yearTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                        val yearBalance = yearIncome - yearExpenses
                        
                        // Check for missing congregations in selected month
                        val allCongregations = listOf(
                            "Congregația A", "Congregația B", "Congregația C", 
                            "Congregația D", "Congregația E", "Congregația F",
                            "Congregația G", "Congregația H"
                        )
                        val expectedAmountPerCongregation = 500.0 // Expected monthly donation
                        
                        val congregationDonations = filteredTransactions
                            .filter { it.type == TransactionType.INCOME && it.congregationName != null }
                            .groupBy { it.congregationName }
                            .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
                        
                        val missingCongregations = allCongregations.mapNotNull { congName ->
                            val donated = congregationDonations[congName] ?: 0.0
                            if (donated < expectedAmountPerCongregation) {
                                Triple(congName, donated, expectedAmountPerCongregation - donated)
                            } else null
                        }
                        
                        StatisticsCards(
                            totalExpenses = filteredExpenses,
                            totalIncome = filteredIncome,
                            balance = filteredBalance,
                            selectedMonth = selectedMonthName,
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
            
            // Missing Congregations Alert
            item {
                when (val state = uiState) {
                    is HomeState.Success -> {
                        val filteredTransactions = state.transactions.filter { transaction ->
                            transaction.getYear() == selectedYear && transaction.getMonth() == selectedMonth
                        }
                        
                        val allCongregations = listOf(
                            "Congregația A", "Congregația B", "Congregația C", 
                            "Congregația D", "Congregația E", "Congregația F",
                            "Congregația G", "Congregația H"
                        )
                        val expectedAmountPerCongregation = 500.0
                        
                        val congregationDonations = filteredTransactions
                            .filter { it.type == TransactionType.INCOME && it.congregationName != null }
                            .groupBy { it.congregationName }
                            .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
                        
                        val missingCongregations = allCongregations.mapNotNull { congName ->
                            val donated = congregationDonations[congName] ?: 0.0
                            if (donated < expectedAmountPerCongregation) {
                                Triple(congName, donated, expectedAmountPerCongregation - donated)
                            } else null
                        }
                        
                        if (missingCongregations.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    2.dp,
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = stringResource(Res.string.alert_missing_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(Res.string.alert_missing_description, selectedMonthName, selectedYear),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    
                                    missingCongregations.forEach { (congName, donated, missing) ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surface
                                            ),
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = congName,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text(
                                                        text = stringResource(Res.string.alert_missing_amount, missing.formatCurrency()),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Text(
                                                        text = stringResource(Res.string.alert_expected_amount, expectedAmountPerCongregation.formatCurrency()),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }
            
            // Annual Summary and General Info
            item {
                when (val state = uiState) {
                    is HomeState.Success -> {
                        val yearTransactions = state.transactions.filter { it.getYear() == selectedYear }
                        val yearIncome = yearTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                        val yearExpenses = yearTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                        val yearBalance = yearIncome - yearExpenses
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Annual Summary
                            Card(modifier = Modifier.weight(1f)) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = stringResource(Res.string.annual_summary_title, selectedYear),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    
                                    SummaryRow(
                                        label = stringResource(Res.string.annual_total_expenses),
                                        value = yearExpenses.formatCurrency(),
                                        valueColor = MaterialTheme.colorScheme.error
                                    )
                                    SummaryRow(
                                        label = stringResource(Res.string.annual_total_donations),
                                        value = yearIncome.formatCurrency(),
                                        valueColor = MaterialTheme.colorScheme.tertiary
                                    )
                                    
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                    
                                    SummaryRow(
                                        label = stringResource(Res.string.annual_balance),
                                        value = yearBalance.formatCurrency(),
                                        valueColor = if (yearBalance >= 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                                        isLarge = true
                                    )
                                    
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                    
                                    SummaryRow(
                                        label = stringResource(Res.string.annual_per_publisher),
                                        value = (yearExpenses / 785).formatCurrency(),
                                        valueColor = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            
                            // General Information
                            Card(modifier = Modifier.weight(1f)) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = stringResource(Res.string.general_info_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    
                                    SummaryRow(
                                        label = stringResource(Res.string.general_total_publishers),
                                        value = "785"
                                    )
                                    SummaryRow(
                                        label = stringResource(Res.string.general_total_congregations),
                                        value = "8"
                                    )
                                    SummaryRow(
                                        label = stringResource(Res.string.general_avg_per_congregation),
                                        value = "${785 / 8}"
                                    )
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    isLarge: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = if (isLarge) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = if (isLarge) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
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
