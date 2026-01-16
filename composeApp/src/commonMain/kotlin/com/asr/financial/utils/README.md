# Number Utilities

Extensii Kotlin pentru formatare și calcule numerice în Kotlin Multiplatform.

## Funcții Disponibile

### Formatare

```kotlin
// Formatare cu zecimale specifice
val number = 123.456
number.formatDecimal(2) // "123.46"
number.formatDecimal(0) // "123"

// Formatare ca monedă RON
val amount = 1234.56
amount.formatCurrency() // "1234.56 RON"
```

### Rotunjire

```kotlin
val number = 123.456
number.roundTo(2) // 123.46
number.roundTo(1) // 123.5
```

### Calcule

```kotlin
val a = 100.0
val b = 30.0

// Operații de bază
a.add(b)      // 130.0
a.subtract(b) // 70.0
a.multiply(b) // 3000.0
a.divide(b)   // 3.33...

// Procente
b.percentOf(a) // 30.0 (30% din 100)
```

### Siguranță

- `divide()` returnează 0.0 dacă împărțitorul este 0
- `percentOf()` returnează 0.0 dacă totalul este 0
- Toate funcțiile sunt null-safe

## Exemple de Utilizare

```kotlin
// În UI
Text(text = totalExpenses.formatCurrency())

// Calcule
val balance = income.subtract(expenses)
val percentage = expenses.percentOf(income)
val perPerson = totalAmount.divide(numberOfPeople.toDouble())

// Afișare
Text("Balanță: ${balance.formatCurrency()}")
Text("Procent: ${percentage.formatDecimal(1)}%")
```

## Avantaje

✅ Funcționează pe toate platformele (Android, iOS, Desktop)
✅ Null-safe și division-by-zero safe
✅ Ușor de citit și întreținut
✅ Consistent în toată aplicația
