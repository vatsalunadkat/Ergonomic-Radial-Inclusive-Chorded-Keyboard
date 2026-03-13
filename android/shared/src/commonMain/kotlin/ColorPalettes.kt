package com.vatoo.erick.shared

enum class ColorPaletteType {
    DEFAULT, OKABE_ITO, DEUTERANOPIA, PROTANOPIA, TRITANOPIA
}

data class ColorEntry(
    val name: String,
    val hexColor: Long  // e.g. 0xFFE69F00
)

object ColorPalettes {

    fun getPalette(type: ColorPaletteType): List<ColorEntry> {
        return when (type) {
            ColorPaletteType.DEFAULT      -> defaultPalette
            ColorPaletteType.OKABE_ITO    -> okabeItoPalette
            ColorPaletteType.DEUTERANOPIA -> deuteranopiaPlette
            ColorPaletteType.PROTANOPIA   -> protanopiaPalette
            ColorPaletteType.TRITANOPIA   -> tritanopiaPalette
        }
    }

    // 1. Default (Standard Rainbow)
    private val defaultPalette = listOf(
        ColorEntry("Red",    0xFFE60012),
        ColorEntry("Orange", 0xFFF39800),
        ColorEntry("Yellow", 0xFFFFF100),
        ColorEntry("Green",  0xFF009944),
        ColorEntry("Blue",   0xFF0068B7),
        ColorEntry("Indigo", 0xFF1D2088),
        ColorEntry("Violet", 0xFF920783),
        ColorEntry("Black",  0xFF000000)
    )

    // 2. Okabe-Ito Universal (safe for all CVD types)
    private val okabeItoPalette = listOf(
        ColorEntry("Orange",         0xFFE69F00),
        ColorEntry("Sky Blue",       0xFF56B4E9),
        ColorEntry("Bluish Green",   0xFF009E73),
        ColorEntry("Yellow",         0xFFF0E442),
        ColorEntry("Blue",           0xFF0072B2),
        ColorEntry("Vermillion",     0xFFD55E00),
        ColorEntry("Reddish Purple", 0xFFCC79A7),
        ColorEntry("Black",          0xFF000000)
    )

    // 3. Deuteranopia-Friendly (optimised for green-blind users)
    private val deuteranopiaPlette = listOf(
        ColorEntry("Blue",       0xFF0072B2),
        ColorEntry("Orange",     0xFFE69F00),
        ColorEntry("Light Blue", 0xFF56B4E9),
        ColorEntry("Yellow",     0xFFF0E442),
        ColorEntry("Dark Red",   0xFFCC3311),
        ColorEntry("Teal",       0xFF009988),
        ColorEntry("Pink",       0xFFEE7733),
        ColorEntry("Black",      0xFF000000)
    )

    // 4. Protanopia-Friendly (optimised for red-blind users)
    private val protanopiaPalette = listOf(
        ColorEntry("Blue",    0xFF0077BB),
        ColorEntry("Cyan",    0xFF33BBEE),
        ColorEntry("Teal",    0xFF009988),
        ColorEntry("Yellow",  0xFFEE7733),
        ColorEntry("Orange",  0xFFCC3311),
        ColorEntry("Magenta", 0xFFEE3377),
        ColorEntry("Grey",    0xFFBBBBBB),
        ColorEntry("Black",   0xFF000000)
    )

    // 5. Tritanopia-Friendly (optimised for blue-blind users)
    private val tritanopiaPalette = listOf(
        ColorEntry("Red",     0xFFCC3311),
        ColorEntry("Blue",    0xFF0077BB),
        ColorEntry("Yellow",  0xFFEECC66),
        ColorEntry("Cyan",    0xFF33BBEE),
        ColorEntry("Magenta", 0xFFEE3377),
        ColorEntry("Teal",    0xFF009988),
        ColorEntry("Grey",    0xFFBBBBBB),
        ColorEntry("Black",   0xFF000000)
    )
}