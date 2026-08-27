with open("app/src/main/java/com/example/ui/screens/ScannerCameraOverlay.kt", "r") as f:
    content = f.read()

mock_str = """
        // Mock Camera View
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Camera Preview\n(Not available in emulator)",
                color = Color.White,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.fetchScannedUser("test_user_id_from_qr") },
                colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.VioraNeonLime, contentColor = Color.Black)
            ) {
                Text("Simulate QR Scan")
            }
        }
"""
content = content.replace("        // Mock Camera View\n        Text(\n            text = \"Camera Preview\\n(Not available in emulator)\",\n            color = Color.White,\n            modifier = Modifier.align(Alignment.Center)\n        )", mock_str)

with open("app/src/main/java/com/example/ui/screens/ScannerCameraOverlay.kt", "w") as f:
    f.write(content)
