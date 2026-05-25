package br.com.triskin.presentation.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector

fun weatherIcon(code: Int): ImageVector = when (code) {
    0, 1 -> Icons.Filled.WbSunny
    2 -> Icons.Filled.WbCloudy
    3, in 45..48 -> Icons.Filled.Cloud
    in 51..67, in 80..82 -> Icons.Filled.Grain
    in 71..77, in 85..86 -> Icons.Filled.AcUnit
    in 95..99 -> Icons.Filled.Thunderstorm
    else -> Icons.Filled.Cloud
}
