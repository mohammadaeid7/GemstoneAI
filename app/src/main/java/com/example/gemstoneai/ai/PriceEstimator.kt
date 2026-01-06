package com.example.gemstoneai.ai

import kotlin.math.max

enum class GemType(val displayNameAr: String) {
    DIAMOND("ألماس"),
    RUBY("ياقوت"),
    SAPPHIRE("ياقوت أزرق"),
    EMERALD("زمرد"),
    AMETHYST("جمشت"),
    OPAL("أوبال"),
    TOPAZ("توباز"),
    UNKNOWN("غير محدد")
}

enum class QualityGrade(val label: String, val multiplier: Double) {
    A("A", 1.15),
    B("B", 1.00),
    C("C", 0.85)
}

enum class PurityGrade(val label: String, val multiplier: Double) {
    IF("IF", 1.25),
    VVS("VVS", 1.15),
    VS("VS", 1.05),
    SI("SI", 0.92),
    I1("I1", 0.80)
}

data class PriceEstimate(
    val currency: String,
    val amount: Double,
    val perCarat: Double
)

/**
 * ملاحظة مهمة:
 * هذه ليست أسعار سوقية فعلية. هي "جدول افتراضي" فقط لتشغيل التطبيق بدون أي اتصال.
 * يمكن تعديل القيم من داخل الكود أو ربطها بمصدر أسعار لاحقاً.
 */
object PriceEstimator {

    // Base USD per carat (demo values only).
    private val baseUsdPerCarat: Map<GemType, Double> = mapOf(
        GemType.DIAMOND to 6500.0,
        GemType.RUBY to 4200.0,
        GemType.SAPPHIRE to 2800.0,
        GemType.EMERALD to 3000.0,
        GemType.AMETHYST to 120.0,
        GemType.OPAL to 350.0,
        GemType.TOPAZ to 200.0,
        GemType.UNKNOWN to 250.0
    )

    fun estimate(
        gemType: GemType,
        weightCarat: Double,
        quality: QualityGrade,
        purity: PurityGrade,
        currency: String,
        currencyToUsd: Double
    ): PriceEstimate {
        val w = max(weightCarat, 0.0)
        val base = baseUsdPerCarat[gemType] ?: baseUsdPerCarat[GemType.UNKNOWN]!!

        // Weight curve: bigger stones are typically rarer.
        val weightMultiplier = when {
            w >= 5.0 -> 1.35
            w >= 2.0 -> 1.18
            w >= 1.0 -> 1.05
            else -> 1.0
        }

        val usdPerCarat = base * quality.multiplier * purity.multiplier * weightMultiplier
        val totalUsd = usdPerCarat * w

        // If user says: 1 unit of (currency) = currencyToUsd USD
        val total = if (currencyToUsd > 0) totalUsd / currencyToUsd else totalUsd
        val perCarat = if (currencyToUsd > 0) usdPerCarat / currencyToUsd else usdPerCarat

        return PriceEstimate(currency = currency, amount = total, perCarat = perCarat)
    }
}
