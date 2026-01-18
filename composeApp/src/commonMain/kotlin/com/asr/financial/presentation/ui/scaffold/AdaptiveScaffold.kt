package com.asr.financial.presentation.ui.scaffold

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.asr.financial.presentation.ui.navigation.*
import com.asr.financial.presentation.ui.responsive.WindowSizeClass
import kotlinx.coroutines.launch

/**
 * Adaptive Scaffold that changes layout based on window size
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveScaffold(
    windowSizeClass: WindowSizeClass,
    selectedRoute: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit = {},
    currentUserEmail: String? = null,
    content: @Composable (PaddingValues, onMenuClick: () -> Unit) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val onMenuClick: () -> Unit = {
        scope.launch { drawerState.open() }
    }
    
    when (windowSizeClass) {
        WindowSizeClass.Compact -> {
            // Phone: Modal drawer only (no bottom nav)
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet {
                        DrawerNavigationContent(
                            selectedRoute = selectedRoute,
                            onNavigate = { route ->
                                scope.launch { drawerState.close() }
                                onNavigate(route)
                            },
                            onLogout = {
                                scope.launch { drawerState.close() }
                                onLogout()
                            },
                            currentUserEmail = currentUserEmail
                        )
                    }
                }
            ) {
                Scaffold { paddingValues ->
                    content(paddingValues, onMenuClick)
                }
            }
        }
        
        WindowSizeClass.Medium -> {
            // Tablet: Navigation rail
            Row {
                AppNavigationRail(
                    selectedRoute = selectedRoute,
                    onNavigate = onNavigate
                )
                
                Scaffold {  paddingValues ->
                    content(paddingValues) {}
                }
            }
        }
        
        WindowSizeClass.Expanded -> {
            // Large tablet/Desktop: Permanent drawer
            Row {
                PermanentNavigationDrawer(
                    selectedRoute = selectedRoute,
                    onNavigate = onNavigate,
                    onLogout = onLogout,
                    currentUserEmail = currentUserEmail
                )
                
                Scaffold { paddingValues ->
                    content(paddingValues) {}
                }
            }
        }
    }
}
