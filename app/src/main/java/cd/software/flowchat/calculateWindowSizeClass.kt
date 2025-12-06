package cd.software.flowchat

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import cd.software.flowchat.model.WindowHeightSizeClass
import cd.software.flowchat.model.WindowSizeClass
import cd.software.flowchat.model.WindowWidthSizeClass

@Composable
fun calculateWindowSizeClass(configuration: Configuration): WindowSizeClass {
    val screenWidth = configuration.screenWidthDp
    val screenHeight = configuration.screenHeightDp

    val widthSizeClass = when {
        screenWidth < 600 -> WindowWidthSizeClass.Compact
        screenWidth < 840 -> WindowWidthSizeClass.Medium
        else -> WindowWidthSizeClass.Expanded
    }

    val heightSizeClass = when {
        screenHeight < 480 -> WindowHeightSizeClass.Compact
        screenHeight < 900 -> WindowHeightSizeClass.Medium
        else -> WindowHeightSizeClass.Expanded
    }

    return WindowSizeClass(widthSizeClass, heightSizeClass)
}
