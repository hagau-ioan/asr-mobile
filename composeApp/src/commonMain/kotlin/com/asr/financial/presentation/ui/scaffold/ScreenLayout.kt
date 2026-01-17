package com.asr.financial.presentation.ui.scaffold

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.asr.financial.presentation.ui.components.AppHeader
import com.asr.financial.presentation.ui.components.Breadcrumb
import com.asr.financial.presentation.ui.components.BreadcrumbItem
import com.asr.financial.presentation.ui.responsive.WindowSizeClass

/**
 * Common screen layout with header, breadcrumb, and scrollable content
 */
@Composable
fun ScreenLayout(
    windowSizeClass: WindowSizeClass,
    breadcrumbItems: List<BreadcrumbItem>,
    selectedMonth: String = "Decembrie",
    selectedYear: Int = 2025,
    showRefreshButton: Boolean = false,
    isRefreshing: Boolean = false,
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit = {},
    onRefreshClick: () -> Unit = {},
    content: LazyListScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Gradient Header
        AppHeader(
            totalPublishers = 785,
            totalCongregations = 8,
            selectedMonth = selectedMonth,
            selectedYear = selectedYear,
            showMenuButton = windowSizeClass == WindowSizeClass.Compact,
            showRefreshButton = showRefreshButton,
            isRefreshing = isRefreshing,
            onMenuClick = onMenuClick,
            onRefreshClick = onRefreshClick
        )
        
        // Breadcrumb (hide if only one item)
        if (breadcrumbItems.size > 1) {
            Breadcrumb(
                items = breadcrumbItems,
                onNavigate = onNavigate
            )
            
            HorizontalDivider()
        }
        
        // Scrollable content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            content()
        }
    }
}
