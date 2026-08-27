package com.example.ui.screens

import android.Manifest
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.model.viora.UserResponseDto
import com.example.ui.theme.SFProDisplayFontFamily
import com.example.viewmodel.VioraTaskViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.util.concurrent.Executors

class QRCodeImageAnalyzer(
    private val onQrCodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader().apply {
        setHints(mapOf(DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE)))
    }

    private var isScanning = true

    fun resumeScanning() {
        isScanning = true
    }

    override fun analyze(imageProxy: ImageProxy) {
        if (!isScanning) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val buffer = mediaImage.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            val source = PlanarYUVLuminanceSource(
                bytes,
                imageProxy.width,
                imageProxy.height,
                0,
                0,
                imageProxy.width,
                imageProxy.height,
                false
            )
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            try {
                val result = reader.decode(binaryBitmap)
                if (result != null && result.text.isNotEmpty()) {
                    isScanning = false
                    onQrCodeScanned(result.text)
                }
            } catch (_: Exception) {
                // Decoding failed for current frame, move next
            } finally {
                imageProxy.close()
            }
        } else {
            imageProxy.close()
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ScannerCameraOverlay(
    viewModel: VioraTaskViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val scannedUser by viewModel.scannedUser.collectAsState()
    val isLoading by viewModel.isScannedUserLoading.collectAsState()
    val teams = viewModel.fullTeamsList.filter { !it.isArchived }

    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedTeamId by remember { mutableStateOf(teams.firstOrNull()?.id ?: "") }

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    val analyzer = remember {
        QRCodeImageAnalyzer { rawResult ->
            // Extract user id or handle from viora://recruit/handle or raw text
            val cleanedQuery = if (rawResult.contains("viora://recruit/")) {
                rawResult.substringAfter("viora://recruit/")
            } else {
                rawResult
            }
            viewModel.fetchScannedUser(cleanedQuery)
        }
    }

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    LaunchedEffect(scannedUser) {
        if (scannedUser != null) {
            showBottomSheet = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (cameraPermissionState.status.isGranted) {
            // Camera Preview View
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val executor = ContextCompat.getMainExecutor(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also {
                                it.setAnalyzer(Executors.newSingleThreadExecutor(), analyzer)
                            }

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, executor)

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Permission missing info view
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Camera Permission Required",
                    color = Color.White,
                    fontFamily = SFProDisplayFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Please grant camera permission to scan QR codes with your device.",
                    color = Color.Gray,
                    fontFamily = SFProDisplayFontFamily,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { cameraPermissionState.launchPermissionRequest() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB4FF00)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(text = "Grant Permission", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Camera Framing View Overlay
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .border(4.dp, Color(0xFFB4FF00), RoundedCornerShape(28.dp))
                    .clickable {
                        // Interactive tap to test scan directly if testing in emulator/mock
                        viewModel.fetchScannedUser("mock-user-id-123")
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isLoading) "Loading profile..." else "Align QR Code",
                    color = Color.White.copy(alpha = 0.8f),
                    fontFamily = SFProDisplayFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Align QR code inside the frame to scan",
                color = Color.White,
                fontFamily = SFProDisplayFontFamily,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
            if (isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(color = Color(0xFFB4FF00))
            }
        }

        // Top Close Button
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .padding(top = 48.dp, start = 16.dp)
                .background(Color(0xFF1C1C1E).copy(alpha = 0.8f), CircleShape)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }
    }

    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
    if (showBottomSheet && scannedUser != null) {
        ModalBottomSheet(
            sheetState = sheetState,
            shape = com.example.ui.utils.dynamicBottomSheetShape(sheetState, 28.dp),
            onDismissRequest = {
                showBottomSheet = false
                viewModel.clearScannedUser()
                analyzer.resumeScanning()
            },
            containerColor = Color(0xFF1C1C1E)
        ) {
            ScannedProfileContent(
                user = scannedUser!!,
                teams = teams,
                selectedTeamId = selectedTeamId,
                onTeamSelected = { selectedTeamId = it },
                onRecruit = {
                    if (selectedTeamId.isNotEmpty()) {
                        viewModel.addScannedUserToTeam(selectedTeamId, scannedUser!!.username)
                        showBottomSheet = false
                        viewModel.clearScannedUser()
                        onNavigateBack()
                    }
                }
            )
        }
    }
}

@Composable
fun ScannedProfileContent(
    user: UserResponseDto,
    teams: List<com.example.model.Team>,
    selectedTeamId: String,
    onTeamSelected: (String) -> Unit,
    onRecruit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        if (user.avatar != null) {
            AsyncImage(
                model = user.avatar,
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFB4FF00)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.fullName.take(1).uppercase(),
                    color = Color.Black,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SFProDisplayFontFamily
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = user.fullName,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = SFProDisplayFontFamily
        )
        Text(
            text = "@${user.username}",
            color = Color.Gray,
            fontSize = 14.sp,
            fontFamily = SFProDisplayFontFamily
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Team Selection
        Text(
            text = "Select Alliance",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = SFProDisplayFontFamily,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            teams.forEach { team ->
                val isSelected = team.id == selectedTeamId
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Color(0xFFB4FF00) else Color(0xFF2C2C2E))
                        .clickable { onTeamSelected(team.id) }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = team.name,
                        color = if (isSelected) Color.Black else Color.White,
                        fontFamily = SFProDisplayFontFamily,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRecruit,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB4FF00)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Recruit Member",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SFProDisplayFontFamily
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
