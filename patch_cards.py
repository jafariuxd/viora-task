import re

with open('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'r') as f:
    content = f.read()

def patch_card(component_name):
    global content
    pattern = re.compile(
        r'(fun ' + component_name + r'\([^)]+\)\s*\{\s*val haptic = androidx\.compose\.ui\.platform\.LocalHapticFeedback\.current\s*val interactionSource = androidx\.compose\.runtime\.remember \{ androidx\.compose\.foundation\.interaction\.MutableInteractionSource\(\) \})'
    )
    
    replacement = r'''\1
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.6f, stiffness = 500f)
    )'''
    
    content = pattern.sub(replacement, content)
    
    # Now find the Card that follows this component and add .graphicsLayer
    # This is trickier with regex, but we know the Card modifier structure.
    # Let's just find the first Card after the component def.
    # Actually, we can just replace 'modifier = Modifier' with 'modifier = Modifier\n            .graphicsLayer { scaleX = scale; scaleY = scale }' 
    # but only for that specific Card.

patch_card('NextTaskCard')
patch_card('TaskListItemCard')

with open('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'w') as f:
    f.write(content)
