with open("app/src/main/java/com/example/ui/screens/ScannerCameraOverlay.kt", "r") as f:
    content = f.read()

mock_str = """
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .border(4.dp, Color(0xFFB4FF00), RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Camera Preview\n(Not available in emulator)",
                        color = Color.White,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.fetchScannedUser("e3db2b0b-8d19-4c8a-9a84-0a3c9b740441") },
                        colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.VioraNeonLime, contentColor = Color.Black)
                    ) {
                        Text("Simulate QR Scan")
                    }
                }
            }
"""
import re
content = re.sub(r'            Box\([\s\S]*?contentAlignment = Alignment\.Center\n            \) {\n                Text\(\n                    text = "Camera Preview\\n\(Not available in emulator\)",\n                    color = Color\.White\n                \)\n            }', mock_str.strip(), content)

with open("app/src/main/java/com/example/ui/screens/ScannerCameraOverlay.kt", "w") as f:
    f.write(content)
