package com.asr.financial.presentation.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import asr_financial.composeapp.generated.resources.*
import com.asr.financial.AppConfig
import com.asr.financial.presentation.navigation.Routes
import com.asr.financial.presentation.ui.components.TwoLevelHouseIcon
import org.jetbrains.compose.resources.stringResource

/**
 * Navigation items for the app
 */
sealed class NavigationItem(
    val route: String,
    val titleRes: org.jetbrains.compose.resources.StringResource,
    val icon: ImageVector
) {
    data object Home : NavigationItem(Routes.HOME, Res.string.nav_home, Icons.Default.Home)
    data object Congregations : NavigationItem(Routes.CONGREGATIONS, Res.string.nav_congregations, Icons.Default.People)
    data object Expenses : NavigationItem(Routes.EXPENSES, Res.string.nav_expenses, Icons.Default.Receipt)
    data object Utilities : NavigationItem(Routes.UTILITIES, Res.string.nav_utilities, Icons.Default.Bolt)
    data object Yearly : NavigationItem(Routes.YEARLY, Res.string.nav_yearly, Icons.AutoMirrored.Filled.TrendingUp)
    data object Calculator : NavigationItem(Routes.CALCULATOR, Res.string.nav_calculator, Icons.Default.Calculate)
    data object AsrExpenses : NavigationItem(Routes.ASR_EXPENSES, Res.string.nav_asr_expenses, Icons.Default.AccountBalance)
    data object Upload : NavigationItem(Routes.UPLOAD, Res.string.nav_upload, Icons.Default.Upload)
}

val navigationItems = listOf(
    NavigationItem.Home,
    NavigationItem.Congregations,
    NavigationItem.Expenses,
    NavigationItem.Utilities,
    NavigationItem.Yearly,
    NavigationItem.Calculator,
    NavigationItem.AsrExpenses,
    NavigationItem.Upload
)

/**
 * Drawer Navigation Content - matches Financial Tracking App design
 */
@Composable
fun DrawerNavigationContent(
    selectedRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Header with gradient background - fixed height to match AppHeader
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TwoLevelHouseIcon(
                    modifier = Modifier.size(56.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                
                Column {
                    Text(
                        text = stringResource(Res.string.app_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = stringResource(Res.string.app_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
            }
        }
        
        // Menu Items
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 16.dp, horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            navigationItems.forEach { item ->
                val isSelected = selectedRoute == item.route
                
                Button(
                    onClick = { onNavigate(item.route) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.tertiary else Color.Transparent,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    elevation = if (isSelected) ButtonDefaults.buttonElevation(4.dp) else ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = stringResource(item.titleRes),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        
        // Footer with stats and version
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp)
        ) {
            HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp))
            
            // Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(Res.string.footer_publishers_label),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "785",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(Res.string.footer_congregations_label),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "8",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Version
            Text(
                text = stringResource(Res.string.version, AppConfig.VERSION_NAME),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

/**
 * Permanent Navigation Drawer for Expanded screens
 */
@Composable
fun PermanentNavigationDrawer(

    selectedRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    PermanentDrawerSheet(
        modifier = modifier.width(240.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
    ) {
        DrawerNavigationContent(
            selectedRoute = selectedRoute,
            onNavigate = onNavigate
        )
    }
}

/**
 * Navigation Rail for Medium screens
 */
@Composable
fun AppNavigationRail(
    selectedRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Spacer(Modifier.height(16.dp))
        
        navigationItems.forEach { item ->
            NavigationRailItem(
                icon = { Icon(item.icon, contentDescription = stringResource(item.titleRes)) },
                label = { Text(stringResource(item.titleRes)) },
                selected = selectedRoute == item.route,
                onClick = { onNavigate(item.route) }
            )
        }
    }
}
