package com.asr.financial.presentation.ui.components.period

import androidx.compose.runtime.*
import com.asr.financial.platform.Clock
import com.asr.financial.utils.calculatePreviousMonth
import com.asr.financial.utils.getCurrentMonth
import com.asr.financial.utils.getCurrentYear
import com.asr.financial.utils.getMonthsList
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Composable hook for managing period selector state and logic.
 * 
 * This hook encapsulates the common pattern of:
 * - Calculating default year/month (previous month)
 * - Managing selected year/month state
 * - Managing dropdown visibility state
 * - Getting month names
 * - Handling period changes via LaunchedEffect
 * 
 * @param clock Clock instance for getting current date
 * @param onPeriodChange Callback when period changes (year, month)
 * @param initialLoadEvent Optional event to trigger on initial load
 * @param skipInitialChange If true, skips triggering onPeriodChange for the initial default values
 * @return PeriodSelectorState containing all state and helpers
 */
@Composable
fun usePeriodSelectorState(
    clock: Clock,
    onPeriodChange: (Int, Int) -> Unit,
    initialLoadEvent: (() -> Unit)? = null,
    skipInitialChange: Boolean = false
): PeriodSelectorState {
    // Calculate previous month for default selection
    val (defaultYear, defaultMonth) = remember {
        val year = getCurrentYear(clock)
        val month = getCurrentMonth(clock)
        if (month == 1) {
            Pair(year - 1, 12)
        } else {
            Pair(year, month - 1)
        }
    }

    var selectedYear by remember { mutableStateOf(defaultYear) }
    var selectedMonth by remember { mutableStateOf(defaultMonth) }
    var showYearDropdown by remember { mutableStateOf(false) }
    var showMonthDropdown by remember { mutableStateOf(false) }

    val months = remember { getMonthsList() }
    val selectedMonthName = months.find { it.first == selectedMonth }?.second?.let { stringResource(it) } ?: ""

    // Initial load
    LaunchedEffect(Unit) {
        initialLoadEvent?.invoke()
        if (!skipInitialChange) {
            onPeriodChange(selectedYear, selectedMonth)
        }
    }

    // Notify when period changes
    LaunchedEffect(selectedYear, selectedMonth) {
        if (skipInitialChange && selectedYear == defaultYear && selectedMonth == defaultMonth) {
            // Skip the initial change if requested (for screens that only trigger on user changes)
            return@LaunchedEffect
        }
        onPeriodChange(selectedYear, selectedMonth)
    }

    return PeriodSelectorState(
        selectedYear = selectedYear,
        selectedMonth = selectedMonth,
        selectedMonthName = selectedMonthName,
        showYearDropdown = showYearDropdown,
        showMonthDropdown = showMonthDropdown,
        months = months,
        onYearSelected = { selectedYear = it },
        onMonthSelected = { selectedMonth = it },
        onYearDropdownChange = { showYearDropdown = it },
        onMonthDropdownChange = { showMonthDropdown = it }
    )
}

/**
 * State object returned by usePeriodSelectorState
 */
data class PeriodSelectorState(
    val selectedYear: Int,
    val selectedMonth: Int,
    val selectedMonthName: String,
    val showYearDropdown: Boolean,
    val showMonthDropdown: Boolean,
    val months: List<Pair<Int, StringResource>>,
    val onYearSelected: (Int) -> Unit,
    val onMonthSelected: (Int) -> Unit,
    val onYearDropdownChange: (Boolean) -> Unit,
    val onMonthDropdownChange: (Boolean) -> Unit
)
