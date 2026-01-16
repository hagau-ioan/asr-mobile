# Suport Multilingv (i18n)

Aplicația suportă română și maghiară folosind Compose Resources.

## Limbi Disponibile

- 🇷🇴 **Română** (default) - `values/strings.xml`
- 🇭🇺 **Maghiară** - `values-hu/strings.xml`

## Cum Funcționează

Sistemul de operare detectează automat limba dispozitivului și încarcă strings-urile corespunzătoare:
- Dacă limba dispozitivului este maghiară → se folosește `values-hu/strings.xml`
- Altfel → se folosește `values/strings.xml` (română)

## Utilizare în Cod

```kotlin
import asr_financial.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun MyScreen() {
    // String simplu
    Text(text = stringResource(Res.string.app_name))
    
    // String cu parametri
    Text(text = stringResource(Res.string.version, "1.0.0"))
    Text(text = stringResource(Res.string.stat_for_publishers, 785))
}
```

## Adăugare Strings Noi

### 1. Adaugă în română (`values/strings.xml`):
```xml
<string name="my_new_string">Textul meu</string>
<string name="with_param">Valoare: %s</string>
<string name="with_number">Total: %d</string>
```

### 2. Adaugă în maghiară (`values-hu/strings.xml`):
```xml
<string name="my_new_string">Az én szövegem</string>
<string name="with_param">Érték: %s</string>
<string name="with_number">Összesen: %d</string>
```

### 3. Folosește în cod:
```kotlin
Text(stringResource(Res.string.my_new_string))
Text(stringResource(Res.string.with_param, "100 RON"))
Text(stringResource(Res.string.with_number, 785))
```

## Strings Disponibile

### Navigare
- `nav_home`, `nav_congregations`, `nav_expenses`, etc.

### Header
- `app_title`, `app_subtitle`
- `header_publishers`, `header_congregations`, `header_period`

### Statistici
- `stat_monthly_expenses`, `stat_monthly_donations`
- `stat_balance`, `stat_per_publisher`
- `stat_missing_congregations`

### Luni
- `month_january` până la `month_december`

### Comune
- `menu`, `version`, `loading`, `error`, `in_development`

## Testare

### Android
Schimbă limba în Settings → System → Languages → Add language → Magyar

### iOS
Settings → General → Language & Region → Add Language → Magyar

Aplicația va reporni automat cu limba maghiară.

## Avantaje

✅ Suport nativ KMP (funcționează pe Android și iOS)
✅ Detectare automată a limbii
✅ Type-safe (erori la compilare dacă lipsește un string)
✅ Suport pentru parametri (%s, %d)
✅ Ușor de extins cu limbi noi
