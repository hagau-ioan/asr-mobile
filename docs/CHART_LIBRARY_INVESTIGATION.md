# Chart Library Investigation for Compose Multiplatform

## Summary

After investigating multiple chart libraries for Compose Multiplatform, we've decided to continue using our custom Canvas-based LineChart implementation.

## Libraries Investigated

### 1. Vico (2.1.4)
- **Status**: ❌ Not compatible
- **Issue**: The multiplatform-m3 artifact doesn't properly expose Compose APIs for commonMain code despite being configured
- **Attempted**: Yes, in previous iterations
- **Conclusion**: Multiplatform support is unstable

### 2. compose-multiplatform-charts (netguru)
- **Status**: ❌ Not compatible
- **Issue**: Beta version (Beta-0.0.5) had unresolved references
- **Attempted**: Yes, in previous iterations
- **Conclusion**: Too early in development

### 3. AAY-chart (io.github.thechance101:chart)
- **Status**: ❌ Not compatible
- **Version**: 1.1.0 (latest on Maven Central)
- **Issue**: 
  - Library uses Compose Multiplatform 1.6.10
  - Our project uses Compose Multiplatform 1.10.0
  - Significant version mismatch causes compilation errors
  - All imports from `io.github.thechance` package fail to resolve
- **Attempted**: Yes, in this session
- **Maven Central**: https://central.sonatype.com/artifact/io.github.thechance101/chart
- **Conclusion**: Version incompatibility makes it unusable

## Current Solution: Custom Canvas LineChart

### Advantages
1. **Zero Dependencies**: No external library dependencies
2. **Full Control**: Complete control over rendering and behavior
3. **Cross-Platform**: Guaranteed to work on all KMP targets (Android, iOS, Desktop)
4. **Lightweight**: Minimal code footprint (~130 lines)
5. **Customizable**: Easy to modify for specific requirements
6. **Stable**: No breaking changes from external libraries

### Features
- Dual-line support for year-over-year comparison
- Grid lines for better readability
- Solid line for current year data
- Dashed line for previous year data
- Data points marked with circles
- Automatic scaling based on data range
- Material 3 color scheme integration

### Implementation
Location: `composeApp/src/commonMain/kotlin/com/asr/financial/presentation/screens/charts/LineChart.kt`

```kotlin
@Composable
fun LineChart(
    currentYearData: List<Double>,
    previousYearData: List<Double>,
    currentYearLabel: String,
    previousYearLabel: String,
    modifier: Modifier = Modifier,
    height: Int = 350
)
```

## Recommendations

### Short Term
Continue using the custom Canvas-based implementation. It's proven, stable, and meets all current requirements.

### Long Term
Monitor these libraries for maturity:
1. **Vico**: Watch for stable multiplatform releases
2. **KoalaPlot**: Mentioned in community discussions as a viable option
3. **AAY-chart**: Wait for updates to support newer Compose versions

### When to Reconsider
- If complex chart types are needed (scatter plots, candlestick, etc.)
- If interactive features become critical (zoom, pan, tooltips)
- If a library reaches stable 1.0+ with active maintenance
- If a library officially supports Compose Multiplatform 1.10+

## References
- [AAY-chart GitHub](https://github.com/TheChance101/AAY-chart)
- [AAY-chart Maven Central](https://central.sonatype.com/artifact/io.github.thechance101/chart)
- [Vico Documentation](https://github.com/patrykandpatrick/vico)
- [Compose Multiplatform Charts Guide](https://www.netguru.com/blog/compose-multiplatform-custom-charts)

## Date
January 2026
