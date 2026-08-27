import re

with open('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'r') as f:
    content = f.read()

def inject_graphics_layer(start_str):
    global content
    idx = content.find(start_str)
    if idx != -1:
        card_idx = content.find('Card(', idx)
        if card_idx != -1:
            mod_idx = content.find('modifier = Modifier', card_idx)
            if mod_idx != -1:
                content = content[:mod_idx] + 'modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale }' + content[mod_idx+19:]

inject_graphics_layer('fun NextTaskCard')
inject_graphics_layer('fun TaskListItemCard')

# We also need to add import for collectIsPressedAsState
if 'import androidx.compose.foundation.interaction.collectIsPressedAsState' not in content:
    content = content.replace('import androidx.compose.foundation.interaction.MutableInteractionSource',
                              'import androidx.compose.foundation.interaction.MutableInteractionSource\nimport androidx.compose.foundation.interaction.collectIsPressedAsState')

with open('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'w') as f:
    f.write(content)
