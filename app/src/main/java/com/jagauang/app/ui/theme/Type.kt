package com.jagauang.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.jagauang.app.R

val DMSans = FontFamily(
    Font(R.font.dmsans_variable, FontWeight.Normal),
    Font(R.font.dmsans_variable, FontWeight.Medium),
    Font(R.font.dmsans_variable, FontWeight.SemiBold),
    Font(R.font.dmsans_variable, FontWeight.Bold),
)

val JagaUangTypography = Typography(
    displayMedium = TextStyle(fontFamily = DMSans, fontWeight = FontWeight.SemiBold, fontSize = 36.sp),
    headlineSmall = TextStyle(fontFamily = DMSans, fontWeight = FontWeight.SemiBold, fontSize = 24.sp),
    titleLarge = TextStyle(fontFamily = DMSans, fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    titleMedium = TextStyle(fontFamily = DMSans, fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    bodyLarge = TextStyle(fontFamily = DMSans, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = DMSans, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelLarge = TextStyle(fontFamily = DMSans, fontWeight = FontWeight.Medium, fontSize = 16.sp),
)
