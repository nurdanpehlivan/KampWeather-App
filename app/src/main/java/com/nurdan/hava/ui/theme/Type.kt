package com.nurdan.hava.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Yazı ölçeği: hava uygulamalarına özgü ince, büyük sıcaklık rakamları;
 * sade, orta kalınlıkta başlıklar. Yazı tipi telefonun kendi sans-serif'i (Roboto).
 */
private val base = Typography()

internal val HavaTypography = base.copy(
    displayLarge = base.displayLarge.copy(fontWeight = FontWeight.Thin, fontSize = 112.sp, lineHeight = 112.sp, letterSpacing = (-4).sp),
    displayMedium = base.displayMedium.copy(fontWeight = FontWeight.ExtraLight, fontSize = 56.sp, letterSpacing = (-2).sp),
    displaySmall = base.displaySmall.copy(fontWeight = FontWeight.Light, letterSpacing = (-1).sp),
    headlineLarge = base.headlineLarge.copy(fontWeight = FontWeight.SemiBold, letterSpacing = (-0.5).sp),
    headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.Medium),
    headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.Medium),
    titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    labelLarge = base.labelLarge.copy(fontWeight = FontWeight.SemiBold),
    labelMedium = base.labelMedium.copy(fontWeight = FontWeight.Medium, letterSpacing = 1.sp),
    labelSmall = base.labelSmall.copy(fontWeight = FontWeight.Medium, letterSpacing = 0.8.sp)
)
