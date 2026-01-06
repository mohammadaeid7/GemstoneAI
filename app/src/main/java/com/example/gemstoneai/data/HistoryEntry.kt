package com.example.gemstoneai.data

data class HistoryEntry(
    val timestampMillis: Long,
    val gemTypeAr: String,
    val weightCarat: Double,
    val quality: String,
    val purity: String,
    val currency: String,
    val estimatedValue: Double,
    val perCarat: Double
) {
    fun toCsv(): String = listOf(
        timestampMillis.toString(),
        gemTypeAr.replace(",", " "),
        weightCarat.toString(),
        quality,
        purity,
        currency,
        estimatedValue.toString(),
        perCarat.toString()
    ).joinToString(",")

    companion object {
        fun fromCsv(line: String): HistoryEntry? {
            val parts = line.split(",")
            if (parts.size < 8) return null
            return try {
                HistoryEntry(
                    timestampMillis = parts[0].toLong(),
                    gemTypeAr = parts[1],
                    weightCarat = parts[2].toDouble(),
                    quality = parts[3],
                    purity = parts[4],
                    currency = parts[5],
                    estimatedValue = parts[6].toDouble(),
                    perCarat = parts[7].toDouble()
                )
            } catch (_: Throwable) {
                null
            }
        }
    }
}
