package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// ZYNE OFFERS - ROYAL BLUE & WHITE FINTECH DESIGN TOKENS
// (Matching Primary Reference Image file_000000002d5482079bd4945accd073fb.png)
// ==========================================

// Surfaces & Canvas
val ZyneBackground = Color(0xFFFFFFFF)           // Pure crisp white canvas
val ZyneSurface = Color(0xFFF8FAFC)              // Soft slate-50 surface
val ZyneCard = Color(0xFFFFFFFF)                 // Pure white card
val ZyneCardAlt = Color(0xFFF8FAFC)              // Slate-50 card alternate
val ZyneBorder = Color(0xFFE2E8F0)               // Slate-200 subtle border
val ZyneBorderSubtle = Color(0xFFF1F5F9)         // Slate-100 very soft border

// Primary Royal Blue Accent Palette
val ZyneBlue = Color(0xFF2563EB)                 // Royal Blue 600 (Primary Brand)
val ZyneBlueDark = Color(0xFF1D4ED8)             // Royal Blue 700
val ZyneBlueDeep = Color(0xFF1E3A8A)             // Navy Blue 900
val ZyneBlueLight = Color(0xFFEFF6FF)            // Blue 50 background tint
val ZyneBlueBorder = Color(0xFFBFDBFE)           // Blue 200 border
val ZyneBlueText = Color(0xFF1D4ED8)             // High-contrast blue text

// Typography
val ZyneTextPrimary = Color(0xFF0F172A)          // Slate 900 (High contrast bold headings)
val ZyneTextSecondary = Color(0xFF334155)        // Slate 700 (Body & labels)
val ZyneTextMuted = Color(0xFF64748B)            // Slate 500 (Subtitles & placeholders)
val ZyneTextSubtle = Color(0xFF94A3B8)           // Slate 400 (Captions & timestamps)

// Status & Feedback Colors
val ZyneGreen = Color(0xFF059669)                // Emerald 600 (Completed / Rewards)
val ZyneGreenBg = Color(0xFFECFDF5)              // Emerald 50
val ZyneGreenBorder = Color(0xFFA7F3D0)          // Emerald 200

val ZyneRed = Color(0xFFDC2626)                  // Red 600 (Hot / Rejected / Error)
val ZyneRedBg = Color(0xFFFEF2F2)                // Red 50
val ZyneRedBorder = Color(0xFFFECACA)            // Red 200

val ZyneAmber = Color(0xFFD97706)                // Amber 600 (Pending)
val ZyneAmberBg = Color(0xFFFFFBEB)              // Amber 50
val ZyneAmberBorder = Color(0xFFFDE68A)          // Amber 200

val ZynePurple = Color(0xFF7C3AED)               // Violet 600 (Popular badge)
val ZynePurpleBg = Color(0xFFF5F3FF)             // Violet 50
val ZynePurpleBorder = Color(0xFFDDD6FE)         // Violet 200

// Bradients
val ZyneHeroGradient = Brush.horizontalGradient(
    listOf(Color(0xFF1D4ED8), Color(0xFF2563EB))
)

val ZyneWalletGradient = Brush.linearGradient(
    listOf(Color(0xFF1E3A8A), Color(0xFF1D4ED8), Color(0xFF2563EB))
)

val ZyneAvatarGradient = Brush.linearGradient(
    listOf(Color(0xFF2563EB), Color(0xFF3B82F6))
)

val ZyneTelegramGradient = Brush.horizontalGradient(
    listOf(Color(0xFF1D4ED8), Color(0xFF2563EB))
)

// Shadcn Compatibility Aliases
val ShadcnBackground = ZyneBackground
val ShadcnForeground = ZyneTextPrimary
val ShadcnMutedText = ZyneTextMuted
val ShadcnMutedBackground = ZyneSurface
val ShadcnBorder = ZyneBorder
val ShadcnBorderSubtle = ZyneBorderSubtle
val ShadcnCard = ZyneCard
val ShadcnCardHover = ZyneSurface
val ShadcnPrimary = ZyneBlue
val ShadcnPrimaryForeground = Color.White
val ShadcnSecondary = ZyneSurface
val ShadcnSecondaryForeground = ZyneTextPrimary
val ShadcnSuccess = ZyneGreen
val ShadcnSuccessBg = ZyneGreenBg
val ShadcnSuccessBorder = ZyneGreenBorder
val ShadcnWarning = ZyneAmber
val ShadcnWarningBg = ZyneAmberBg
val ShadcnWarningBorder = ZyneAmberBorder
val ShadcnDestructive = ZyneRed
val ShadcnDestructiveBg = ZyneRedBg
val ShadcnDestructiveBorder = ZyneRedBorder

// Legacy Aliases for seamless compilation
val ZyneDeepNavy = ZyneBackground
val ZyneRoyalPurple = ZyneBlue
val ZyneBrightPurple = ZyneBlue
val ZyneWhite = ZyneTextPrimary
val ZyneLightBackground = ZyneBackground
val ZyneRewardGold = ZyneBlue
val ZyneSuccess = ZyneGreen
val ZyneDarkCard = ZyneCard
val ZyneDarkCardHover = ZyneSurface
val ZyneDarkBorder = ZyneBorder
val ZyneLightCard = ZyneCard
val ZyneLightCardBorder = ZyneBorder
val ZynePurpleContainer = ZyneBlueLight
val ZynePurpleLight = ZyneTextMuted
val ZyneGoldLight = ZyneAmberBg
val ZyneGoldDark = ZyneAmber
val ZyneBrandGradient = ZyneHeroGradient
val ZynePurpleVerticalGradient = ZyneHeroGradient
val ZyneNavyGradient = Brush.verticalGradient(listOf(ZyneBackground, ZyneBackground))
val ZyneGoldGradient = ZyneHeroGradient
val ZyneCtaGradient = ZyneHeroGradient

val BadgeNewGradient = ZyneHeroGradient
val BadgeHotGradient = Brush.horizontalGradient(listOf(ZyneRed, ZyneRed))
val BadgeSpecialGradient = ZyneHeroGradient

val TextHeadingDark = ZyneTextPrimary
val TextBodyDark = ZyneTextSecondary
val TextSubtleDark = ZyneTextMuted
val TextCaptionDark = ZyneTextSubtle
val TextHeadingLight = ZyneTextPrimary
val TextBodyLight = ZyneTextSecondary
val TextSubtleLight = ZyneTextMuted
val TextPrimaryDark = ZyneTextPrimary
val TextSecondaryDark = ZyneTextSecondary
val TextMutedDark = ZyneTextMuted

val DarkCanvas = ZyneBackground
val DarkSurface = ZyneCard
val DarkSurfaceVariant = ZyneSurface
val DarkBorder = ZyneBorder
val DarkBorderLight = ZyneBorder
val DarkNavBackground = ZyneBackground

val RoyalBluePrimary = ZyneBlue
val RoyalBlueDark = ZyneBlueDark
val RoyalBlueLight = ZyneBlueLight
val RoyalBlueAccent = ZyneBlue
val RoyalBlueSky = ZyneTextMuted
val RoyalBlueDeep = ZyneBlueDeep
val RoyalBlueMidnight = ZyneTextPrimary
val RoyalBlueContainer = ZyneBlueLight
val RoyalBlueGradientStart = ZyneBlueDark
val RoyalBlueGradientEnd = ZyneBlue
val RoyalBlueGradient = ZyneHeroGradient
val RoyalBlueVibrantGradient = ZyneHeroGradient
val SophisticatedDarkGradient = ZyneNavyGradient

val GoldSecondary = ZyneBlue
val GoldLight = ZyneAmberBg
val GoldCoinLight = ZyneBlueLight
val GoldCoinPrimary = ZyneBlue
val GoldCoinDark = ZyneBlueDark
val GoldCoinBorder = ZyneBlueBorder
val GoldCoinGradient = ZyneHeroGradient

val ErrorRed = ZyneRed
val SuccessBlue = ZyneBlue
val SuccessGreen = ZyneGreen
val InfoBlue = ZyneBlue

val EmeraldPrimary = ZyneGreen
val EmeraldAccent = ZyneGreen
val EmeraldDark = ZyneGreen
val EmeraldTealDeep = ZyneGreen
val EmeraldLight = ZyneGreenBg
val EmeraldContainer = ZyneGreenBg
val EmeraldGradient = Brush.horizontalGradient(listOf(ZyneGreen, ZyneGreen))
val TealPrimary = ZyneBlue
val TealDark = ZyneBlueDark

val DarkSlateBackground = ZyneBackground
val DarkSlateSurface = ZyneCard
val DarkSlateSurfaceVariant = ZyneSurface
val LightBackground = ZyneBackground
val LightSurface = ZyneCard
val LightSurfaceVariant = ZyneSurface

val AquaCanvasStart = ZyneBackground
val AquaCanvasMid1 = ZyneCard
val AquaCanvasMid2 = ZyneSurface
val AquaCanvasEnd = ZyneBackground
val AquaCanvasUltraLight = ZyneBackground
val AquaGradientBackground = ZyneNavyGradient
val HeroTealGlassGradient = ZyneHeroGradient
val TelegramBannerGradient = ZyneTelegramGradient
val CtaTealButtonGradient = ZyneHeroGradient
val GreenStatusButtonGradient = EmeraldGradient
val GlassCardBackground = ZyneCard
val GlassCardBackgroundHover = ZyneSurface
val GlassCardBorder = ZyneBorder
val GlassCardBorderTeal = ZyneBorder
val GlassCardInnerShadow = Color.Transparent
