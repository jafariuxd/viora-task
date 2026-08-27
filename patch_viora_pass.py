import re

with open("app/src/main/java/com/example/ui/screens/VioraPassScreen.kt", "r") as f:
    content = f.read()

# Add imports
imports = """import androidx.compose.foundation.Image
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import com.example.ui.utils.QRCodeGenerator
"""
content = content.replace("import androidx.compose.runtime.Composable", imports + "import androidx.compose.runtime.Composable")

# Replace AsyncImage logic
old_qr = """                        AsyncImage(
                            model = "https://api.qrserver.com/v1/create-qr-code/?size=500x500&data=viora://recruit/${userHandle}",
                            contentDescription = "QR Code for ${userName}",
                            modifier = Modifier.fillMaxSize()
                        )"""

new_qr = """                        val qrBitmap = remember(userHandle) { QRCodeGenerator.generateQRCode("viora://recruit/${userHandle}", 500) }
                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "QR Code for ${userName}",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = "Failed to load",
                                color = Color.Black,
                                fontFamily = SFProDisplayFontFamily
                            )
                        }"""

content = content.replace(old_qr, new_qr)

with open("app/src/main/java/com/example/ui/screens/VioraPassScreen.kt", "w") as f:
    f.write(content)
