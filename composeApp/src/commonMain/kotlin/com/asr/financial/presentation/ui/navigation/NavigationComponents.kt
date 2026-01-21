package com.asr.financial.presentation.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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
import com.asr.financial.AppVersionInfo
import com.asr.financial.domain.models.access.AppSection
import com.asr.financial.domain.models.access.UserRole
import com.asr.financial.domain.models.access.UserRoleUtils
import com.asr.financial.domain.usecase.CheckPermissionUseCase
import org.koin.compose.koinInject
import com.asr.financial.presentation.navigation.Routes
import com.asr.financial.presentation.ui.components.TwoLevelHouseIcon
import com.asr.financial.presentation.ui.constants.AppConstants
import com.asr.financial.presentation.ui.constants.UIConstants
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Navigation items for the app
 */
sealed class NavigationItem(
    val route: String,
    val titleRes: StringResource,
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

/**
 * Navigation items organized by functional groups
 * Groups are separated visually in the UI without headers
 */
val navigationItemGroups = listOf(
    // Dashboard & Overview
    listOf(NavigationItem.Home),
    // Data Entry & Management
    listOf(
        NavigationItem.Congregations,
        NavigationItem.Expenses,
        NavigationItem.Utilities,
        NavigationItem.Upload
    ),
    // Reports & Analytics
    listOf(
        NavigationItem.Yearly,
        NavigationItem.AsrExpenses
    ),
    // Tools & Calculations
    listOf(NavigationItem.Calculator)
)

/**
 * Flattened list of all navigation items (for backward compatibility)
 */
val navigationItems = navigationItemGroups.flatten()

/**
 * Maps NavigationItem to AppSection for permission checking
 */
private fun NavigationItem.toAppSection(): AppSection {
    return when (this) {
        NavigationItem.Home -> AppSection.NAV_HOME
        NavigationItem.Congregations -> AppSection.NAV_CONGREGATIONS
        NavigationItem.Expenses -> AppSection.NAV_EXPENSES
        NavigationItem.Utilities -> AppSection.NAV_UTILITIES
        NavigationItem.Yearly -> AppSection.NAV_YEARLY
        NavigationItem.Calculator -> AppSection.NAV_CALCULATOR
        NavigationItem.AsrExpenses -> AppSection.NAV_ASR_EXPENSES
        NavigationItem.Upload -> AppSection.NAV_UPLOAD
    }
}

/**
 * Filter navigation items based on user permissions.
 * Uses CheckPermissionUseCase to check access for each section.
 * 
 * @param currentUserEmail User email address to determine role and access level
 * @param checkPermissionUseCase Use case for checking permissions
 * @return Filtered list of navigation items based on user permissions
 */
fun getFilteredNavigationItems(
    currentUserEmail: String?,
    checkPermissionUseCase: CheckPermissionUseCase
): List<NavigationItem> {
    return navigationItems.filter { item ->
        val section = item.toAppSection()
        checkPermissionUseCase.checkPermission(section, currentUserEmail)
    }
}

/**
 * Drawer Navigation Content - matches Financial Tracking App design
 */
@Composable
fun DrawerNavigationContent(
    selectedRoute: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit = {},
    currentUserEmail: String? = null,
    modifier: Modifier = Modifier,
    checkPermissionUseCase: CheckPermissionUseCase = koinInject()
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Header with gradient background - fixed height to match AppHeader
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppConstants.UI.DRAWER_HEADER_HEIGHT_DP.dp)
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
                    modifier = Modifier.size(AppConstants.UI.DRAWER_ICON_SIZE_DP.dp),
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
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = AppConstants.UI.DEFAULT_ALPHA)
                    )
                }
            }
        }
        
        // Menu Items - Scrollable on small screens, organized in groups
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp, horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val filteredItems = getFilteredNavigationItems(currentUserEmail, checkPermissionUseCase)
            
            // Group filtered items by their original groups
            val filteredGroups = navigationItemGroups.map { group ->
                group.filter { item -> filteredItems.contains(item) }
            }.filter { it.isNotEmpty() }
            
            // Render all items from all groups without separators
            filteredGroups.forEach { group ->
                group.forEach { item ->
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
                                modifier = Modifier.size(AppConstants.UI.NAVIGATION_ICON_SIZE_DP.dp)
                            )
                            Spacer(Modifier.width(AppConstants.UI.NAVIGATION_SPACING_DP.dp))
                            Text(
                                text = stringResource(item.titleRes),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            // Logout button - always visible at the end of scrollable content
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.error
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(AppConstants.UI.NAVIGATION_ICON_SIZE_DP.dp)
                    )
                    Spacer(Modifier.width(AppConstants.UI.NAVIGATION_SPACING_DP.dp))
                    Text(
                        text = stringResource(Res.string.nav_logout),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Extra padding at bottom to ensure logout button is fully visible when scrolled
            Spacer(Modifier.height(16.dp))
        }
        
        // Footer with stats, user email, and version
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp)
        ) {
            HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp))
            
            // User email
            currentUserEmail?.let { email ->
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(Res.string.user_email_label),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))
            }
            
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
                    text = UIConstants.DEFAULT_TOTAL_PUBLISHERS.toString(),
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
                    text = "8", // TODO: Replace with actual congregation count from data
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Version
            Text(
                text = stringResource(Res.string.version, AppVersionInfo.VERSION_NAME),
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
    onLogout: () -> Unit = {},
    currentUserEmail: String? = null,
    modifier: Modifier = Modifier,
    checkPermissionUseCase: CheckPermissionUseCase = koinInject()
) {
    PermanentDrawerSheet(
        modifier = modifier.width(AppConstants.UI.NAVIGATION_DRAWER_WIDTH_DP.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
    ) {
        DrawerNavigationContent(
            selectedRoute = selectedRoute,
            onNavigate = onNavigate,
            onLogout = onLogout,
            currentUserEmail = currentUserEmail,
            checkPermissionUseCase = checkPermissionUseCase
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
    currentUserEmail: String? = null,
    modifier: Modifier = Modifier,
    checkPermissionUseCase: CheckPermissionUseCase = koinInject()
) {
    NavigationRail(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Spacer(Modifier.height(16.dp))
        
        getFilteredNavigationItems(currentUserEmail, checkPermissionUseCase).forEach { item ->
            NavigationRailItem(
                icon = { Icon(item.icon, contentDescription = stringResource(item.titleRes)) },
                label = { Text(stringResource(item.titleRes)) },
                selected = selectedRoute == item.route,
                onClick = { onNavigate(item.route) }
            )
        }
    }
}
