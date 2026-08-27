import os
os.makedirs('app/src/main/java/com/example/ui/utils', exist_ok=True)
with open('app/src/main/java/com/example/ui/utils/Modifiers.kt', 'w') as f:
    f.write("""package com.example.ui.utils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

inline fun Modifier.noRippleClickable(crossinline onClick: () -> Unit): Modifier = composed {
    clickable(indication = null,
        interactionSource = remember { MutableInteractionSource() }) {
        onClick()
    }
}
""")
