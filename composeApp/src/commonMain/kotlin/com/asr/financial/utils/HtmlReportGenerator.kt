package com.asr.financial.utils

/**
 * Generate HTML report for congregations
 */
fun generateCongregationsHtml(
    title: String,
    month: String,
    year: Int,
    congregations: List<CongregationReportData>,
    totalDonated: Double,
    totalExpected: Double,
    totalMissing: Double,
    generatedTimestamp: String = ""
): String {
    return """
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>$title</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 20px;
            color: #333;
        }
        h1 {
            color: #1976D2;
            text-align: center;
            margin-bottom: 10px;
        }
        .subtitle {
            text-align: center;
            color: #666;
            margin-bottom: 30px;
            font-size: 18px;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            margin-bottom: 20px;
        }
        th, td {
            border: 1px solid #ddd;
            padding: 12px;
            text-align: left;
        }
        th {
            background-color: #1976D2;
            color: white;
            font-weight: bold;
        }
        tr:nth-child(even) {
            background-color: #f9f9f9;
        }
        .amount {
            text-align: right;
        }
        .total-row {
            background-color: #e3f2fd !important;
            font-weight: bold;
        }
        .footer {
            text-align: center;
            margin-top: 30px;
            color: #666;
            font-size: 12px;
        }
    </style>
</head>
<body>
    <h1>$title</h1>
    <div class="subtitle">$month $year</div>

    <table>
        <thead>
            <tr>
                <th>Congregație</th>
                <th class="amount">Donat (RON)</th>
                <th class="amount">Așteptat (RON)</th>
                <th class="amount">Lipsă (RON)</th>
            </tr>
        </thead>
        <tbody>
${congregations.joinToString("\n") { cong ->
    """            <tr>
                <td>${cong.name}</td>
                <td class="amount">${cong.donated.formatCurrency()}</td>
                <td class="amount">${cong.expected.formatCurrency()}</td>
                <td class="amount">${cong.missing.formatCurrency()}</td>
            </tr>"""
}}
            <tr class="total-row">
                <td>TOTAL</td>
                <td class="amount">${totalDonated.formatCurrency()}</td>
                <td class="amount">${totalExpected.formatCurrency()}</td>
                <td class="amount">${totalMissing.formatCurrency()}</td>
            </tr>
        </tbody>
    </table>

    <div class="footer">
        ${if (generatedTimestamp.isNotEmpty()) "Generat: $generatedTimestamp" else ""}
    </div>
</body>
</html>
    """.trimIndent()
}

data class CongregationReportData(
    val name: String,
    val donated: Double,
    val expected: Double,
    val missing: Double
)
