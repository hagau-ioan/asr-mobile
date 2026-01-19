package com.asr.financial.presentation.screens.decont

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import asr_financial.composeapp.generated.resources.Res
import asr_financial.composeapp.generated.resources.cd_back
import asr_financial.composeapp.generated.resources.decont_back_to_expenses
import asr_financial.composeapp.generated.resources.decont_col_amount
import asr_financial.composeapp.generated.resources.decont_col_company
import asr_financial.composeapp.generated.resources.decont_col_date
import asr_financial.composeapp.generated.resources.decont_col_receipt
import asr_financial.composeapp.generated.resources.decont_description
import asr_financial.composeapp.generated.resources.decont_summary
import asr_financial.composeapp.generated.resources.decont_title_with_period
import asr_financial.composeapp.generated.resources.decont_total_amount
import asr_financial.composeapp.generated.resources.decont_total_items
import asr_financial.composeapp.generated.resources.nav_asr_expenses
import asr_financial.composeapp.generated.resources.nav_home
import asr_financial.composeapp.generated.resources.symbol_bullet
import com.asr.financial.domain.models.Decont
import com.asr.financial.domain.models.DecontExpense
import com.asr.financial.presentation.mvi.event.DecontEvent
import com.asr.financial.presentation.mvi.state.DecontState
import com.asr.financial.presentation.mvi.viewmodel.DecontViewModel
import com.asr.financial.presentation.navigation.Routes
import com.asr.financial.presentation.screens.decont.DecontConstants.COL_AMOUNT_WIDTH_DP
import com.asr.financial.presentation.screens.decont.DecontConstants.COL_COMPANY_WIDTH_DP
import com.asr.financial.presentation.screens.decont.DecontConstants.COL_DATE_WIDTH_DP
import com.asr.financial.presentation.screens.decont.DecontConstants.COL_RECEIPT_WIDTH_DP
import com.asr.financial.presentation.theme.SuccessGreen
import com.asr.financial.presentation.ui.components.BreadcrumbItem
import com.asr.financial.presentation.ui.components.cards.SummaryInfoCard
import com.asr.financial.presentation.ui.components.states.ErrorContent
import com.asr.financial.presentation.ui.components.states.LoadingContent
import com.asr.financial.presentation.ui.components.table.DataTable
import com.asr.financial.presentation.ui.components.table.TableCell
import com.asr.financial.presentation.ui.components.table.TableColumn
import com.asr.financial.presentation.ui.components.table.TableHeaderCell
import com.asr.financial.presentation.ui.constants.AppConstants
import com.asr.financial.presentation.ui.responsive.WindowSizeClass
import com.asr.financial.presentation.ui.scaffold.ScreenLayout
import com.asr.financial.utils.formatCurrency
import com.asr.financial.utils.getMonthNameResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DecontScreen(
    windowSizeClass: WindowSizeClass,
    year: Int,
    month: Int,
    onNavigate: (String) -> Unit,
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit = {},
    viewModel: DecontViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Load decont data when screen is shown
    LaunchedEffect(year, month) {
        viewModel.handleEvent(DecontEvent.LoadDecont(year, month))
    }

    val monthNameResource = getMonthNameResource(month)
    val monthName = monthNameResource?.let { stringResource(it) } ?: ""

    ScreenLayout(
        windowSizeClass = windowSizeClass,
        breadcrumbItems = listOf(
            BreadcrumbItem(stringResource(Res.string.nav_home), Routes.HOME),
            BreadcrumbItem(stringResource(Res.string.nav_asr_expenses), Routes.ASR_EXPENSES),
            BreadcrumbItem(stringResource(Res.string.decont_title_with_period, monthName, year))
        ),
        onNavigate = onNavigate,
        onMenuClick = onMenuClick
    ) {
        item {
            // Back button row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(Res.string.cd_back),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = stringResource(Res.string.decont_back_to_expenses),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        when (val state = uiState) {
            is DecontState.Loading -> {
                item {
                    LoadingContent()
                }
            }
            is DecontState.Success -> {
                item {
                    DecontSuccessContent(
                        decont = state.decont,
                        totalAmount = state.totalAmount,
                        monthName = monthName,
                        year = year
                    )
                }
            }
            is DecontState.Error -> {
                item {
                    ErrorContent(message = state.message)
                }
            }
        }
    }
}

@Composable
private fun DecontSuccessContent(
    decont: Decont,
    totalAmount: Double,
    monthName: String,
    year: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(Res.string.decont_title_with_period, monthName, year),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(Res.string.decont_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        // Summary Card
        SummaryInfoCard(
            title = stringResource(Res.string.decont_summary),
            totalItemsLabel = stringResource(Res.string.decont_total_items),
            totalItemsValue = decont.expenses.size.toString(),
            totalAmountLabel = stringResource(Res.string.decont_total_amount),
            totalAmountValue = totalAmount.formatCurrency(),
            totalAmountColor = MaterialTheme.colorScheme.error
        )

        // Expenses Table
        val columns = listOf(
            TableColumn(stringResource(Res.string.decont_col_date), COL_DATE_WIDTH_DP.dp),
            TableColumn(stringResource(Res.string.decont_col_company), COL_COMPANY_WIDTH_DP.dp),
            TableColumn(stringResource(Res.string.decont_col_receipt), COL_RECEIPT_WIDTH_DP.dp),
            TableColumn(stringResource(Res.string.decont_col_amount), COL_AMOUNT_WIDTH_DP.dp, TextAlign.End)
        )

        DataTable(
            columns = columns,
            headerContent = {
                columns.forEach { column ->
                    TableHeaderCell(
                        text = column.header,
                        width = column.width,
                        textAlign = column.textAlign
                    )
                }
            }
        ) {
            decont.expenses.forEach { expense ->
                DecontExpenseRow(expense)
            }
        }
    }
}

@Composable
private fun DecontExpenseRow(expense: DecontExpense) {
    val isDarkMode = isSystemInDarkTheme()
    val borderColor = if (isDarkMode) Color.White.copy(alpha = AppConstants.UI.ALPHA_DISABLED) else Color.LightGray.copy(alpha = AppConstants.UI.ALPHA_DISABLED)

    // Draw border at bottom of entire row (including products)
    Column(
        modifier = Modifier.drawBehind {
            val strokeWidth = 0.5.dp.toPx()
            val y = size.height - strokeWidth / 2
            drawLine(
                color = borderColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = strokeWidth
            )
        }
    ) {
        // Table cells row without border
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TableCell(
                text = expense.date,
                width = COL_DATE_WIDTH_DP.dp
            )
            TableCell(
                text = expense.company,
                width = COL_COMPANY_WIDTH_DP.dp
            )
            TableCell(
                text = expense.receipt_nr,
                width = COL_RECEIPT_WIDTH_DP.dp
            )
            TableCell(
                text = expense.amount.formatCurrency(),
                width = COL_AMOUNT_WIDTH_DP.dp,
                textAlign = TextAlign.End,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Medium
            )
        }
        // Show products as a vertical list if available
        if (expense.product_names.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 5.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                expense.product_names.forEach { product ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.symbol_bullet),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
