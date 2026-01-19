package com.asr.financial.presentation.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.asr.financial.presentation.theme.DarkColorScheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.asr.financial.presentation.ui.components.TwoLevelHouseIcon
import com.asr.financial.presentation.mvi.effect.LoginEffect
import com.asr.financial.presentation.mvi.event.LoginEvent
import com.asr.financial.presentation.mvi.state.LoginState
import com.asr.financial.presentation.mvi.viewmodel.LoginViewModel
import com.asr.financial.presentation.navigation.Routes
import asr_financial.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * Login Screen - User authentication
 */
@Composable
fun LoginScreen(
    onNavigate: (String) -> Unit,
    viewModel: LoginViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    
    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is LoginEffect.NavigateToHome -> {
                    onNavigate(Routes.HOME)
                }
                is LoginEffect.ShowToast -> {
                    // Toast handling can be implemented here if needed
                    // For now, errors are shown in the UI
                }
            }
        }
    }
    
    // Use dark mode background color from theme for login screen (always dark)
    val loginBackgroundColor = DarkColorScheme.background
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(loginBackgroundColor)
            .padding(LoginConstants.HORIZONTAL_PADDING_DP.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Splash screen content (house icon + text)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            TwoLevelHouseIcon(
                modifier = Modifier.size(100.dp),
                // Login screen: Both light and dark mode = white house with black windows/door
                color = Color.White,
                windowColor = Color.Black
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                text = stringResource(Res.string.app_title),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = DarkColorScheme.onBackground // Always use light text for dark background
            )
            
            Spacer(Modifier.height(4.dp))
            
            Text(
                text = stringResource(Res.string.app_subtitle),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                color = DarkColorScheme.onBackground.copy(alpha = 0.7f) // Always use light text for dark background
            )
        }
        
        // Login Card
        Card(
            modifier = Modifier
                .width(LoginConstants.INPUT_FIELD_WIDTH_DP.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = LoginConstants.CARD_ELEVATION_DP.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(LoginConstants.CARD_PADDING_DP.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(LoginConstants.VERTICAL_SPACING_DP.dp)
            ) {
                // Title
                Text(
                    text = stringResource(Res.string.login_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Email field
                OutlinedTextField(
                    value = (uiState as? LoginState.Ready)?.email ?: "",
                    onValueChange = { viewModel.handleEvent(LoginEvent.EmailChanged(it)) },
                    label = { Text(stringResource(Res.string.login_email)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !((uiState as? LoginState.Ready)?.isLoading ?: false),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    isError = (uiState as? LoginState.Ready)?.errorMessage != null
                )
                
                // Password field
                OutlinedTextField(
                    value = (uiState as? LoginState.Ready)?.password ?: "",
                    onValueChange = { viewModel.handleEvent(LoginEvent.PasswordChanged(it)) },
                    label = { Text(stringResource(Res.string.login_password)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !((uiState as? LoginState.Ready)?.isLoading ?: false),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { viewModel.handleEvent(LoginEvent.Login) }
                    ),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) {
                                    Icons.Filled.Visibility
                                } else {
                                    Icons.Filled.VisibilityOff
                                },
                                contentDescription = if (passwordVisible) {
                                    stringResource(Res.string.login_hide_password)
                                } else {
                                    stringResource(Res.string.login_show_password)
                                }
                            )
                        }
                    },
                    isError = (uiState as? LoginState.Ready)?.errorMessage != null
                )
                
                // Error message
                (uiState as? LoginState.Ready)?.errorMessage?.let { errorKey ->
                    Text(
                        text = getErrorMessage(errorKey),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Login button
                Button(
                    onClick = { viewModel.handleEvent(LoginEvent.Login) },
                    modifier = Modifier
                        .width(LoginConstants.BUTTON_WIDTH_DP.dp)
                        .height(LoginConstants.BUTTON_HEIGHT_DP.dp),
                    enabled = !((uiState as? LoginState.Ready)?.isLoading ?: false)
                ) {
                    if ((uiState as? LoginState.Ready)?.isLoading == true) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(stringResource(Res.string.login_button))
                    }
                }
            }
        }
    }
}

/**
 * Get error message from error key
 */
@Composable
private fun getErrorMessage(errorKey: String): String {
    return when (errorKey) {
        "login_error_empty_email" -> stringResource(Res.string.login_error_empty_email)
        "login_error_empty_password" -> stringResource(Res.string.login_error_empty_password)
        "login_error_invalid_email" -> stringResource(Res.string.login_error_invalid_email)
        "login_error_wrong_password" -> stringResource(Res.string.login_error_wrong_password)
        "login_error_invalid_credential" -> stringResource(Res.string.login_error_invalid_credential)
        "login_error_user_not_found" -> stringResource(Res.string.login_error_user_not_found)
        "login_error_user_disabled" -> stringResource(Res.string.login_error_user_disabled)
        "login_error_too_many_requests" -> stringResource(Res.string.login_error_too_many_requests)
        "login_error_operation_not_allowed" -> stringResource(Res.string.login_error_operation_not_allowed)
        "login_error_network" -> stringResource(Res.string.login_error_network)
        "login_error_unknown" -> stringResource(Res.string.login_error_unknown)
        else -> stringResource(Res.string.login_error_unknown)
    }
}
