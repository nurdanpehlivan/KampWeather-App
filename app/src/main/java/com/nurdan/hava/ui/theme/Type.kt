package com.nurdan.hava.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Yazı ölçeği: büyük başlıklar ve sıcaklıklar serif (gazete manşeti hissi),
 * gövde ve etiketler sans-serif. Etiketler geniş harf aralığıyla yazılır.
 */
private val base = Typography()
private val Headline = FontFamily.Serif

internal val HavaTypography = base.copy(
    displayLarge = base.displayLarge.copy(fontFamily = Headline, fontWeight = FontWeight.Bold, fontSize = 96.sp, lineHeight = 96.sp),
    displayMedium = base.displayMedium.copy(fontFamily = Headline, fontWeight = FontWeight.Bold),
    displaySmall = base.displaySmall.copy(fontFamily = Headline, fontWeight = FontWeight.Bold),
    headlineLarge = base.headlineLarge.copy(fontFamily = Headline, fontWeight = FontWeight.Bold),
    headlineMedium = base.headlineMedium.copy(fontFamily = Headline, fontWeight = FontWeight.Bold),
    headlineSmall = base.headlineSmall.copy(fontFamily = Headline, fontWeight = FontWeight.SemiBold),
    titleLarge = base.titleLarge.copy(fontFamily = Headline, fontWeight = FontWeight.SemiBold),
    titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    labelLarge = base.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp),
    labelMedium = base.labelMedium.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp),
    labelSmall = base.labelSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp)
)
