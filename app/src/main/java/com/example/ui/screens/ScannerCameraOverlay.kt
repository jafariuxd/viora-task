package com.example.ui.screens

import android.Manifest
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.model.viora.UserResponseDto
import com.example.ui.theme.SFProDisplayFontFamily
import com.example.ui.theme.VioraNeonLime
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

    fun pauseScanning() {
        isScanning = false
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
    var cameraInstance by remember { mutableStateOf<Camera?>(null) }
    var isTorchEnabled by remember { mutableStateOf(false) }

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
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        if (cameraPermissionState.status.isGranted) {
            // 1. Camera Preview with CameraX
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
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
                            val cam = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                            cameraInstance = cam
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, executor)

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // 2. Scanning Laser Animation
            val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
            val laserYRatio by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "laser_y"
            )

            // 3. Darkened Vignette Overlay with Cutout Mask
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
                    .drawWithContent {
                        // Draw semi-transparent dark backdrop
                        drawRect(Color.Black.copy(alpha = 0.65f))

                        val scanBoxSizePx = 260.dp.toPx()
                        val left = (size.width - scanBoxSizePx) / 2f
                        val top = (size.height - scanBoxSizePx) / 2f - 40.dp.toPx()

                        // Punch transparent hole for scanner viewport
                        drawRoundRect(
                            color = Color.Transparent,
                            topLeft = Offset(left, top),
                            size = Size(scanBoxSizePx, scanBoxSizePx),
                            cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx()),
                            blendMode = BlendMode.Clear
                        )
                    }
            )

            // 4. Scanner Viewport Frame & Laser Bar
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .border(3.dp, VioraNeonLime, RoundedCornerShape(28.dp))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            // Tap scanner to test scan in emulator
                            viewModel.fetchScannedUser("mock-user-id-123")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Moving Neon Laser Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.5.dp)
                                .offset(y = 260.dp * laserYRatio)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            VioraNeonLime,
                                            Color.White,
                                            VioraNeonLime,
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    }

                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.7f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    color = VioraNeonLime,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Searching Profile...",
                                    color = Color.White,
                                    fontFamily = SFProDisplayFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Instructional hints
                Text(
                    text = "Point camera at a Viora QR code",
                    color = Color.White,
                    fontFamily = SFProDisplayFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Scan to instantly recruit members to your Alliance",
                    color = Color.White.copy(alpha = 0.5f),
                    fontFamily = SFProDisplayFontFamily,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }

        } else {
            // Clean, Beautiful Permission Request View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Icon Header Badge
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1C1C1E))
                        .border(1.5.dp, VioraNeonLime.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.QrCodeScanner,
                        contentDescription = "Camera Scanner",
                        tint = VioraNeonLime,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "Camera Access Required",
                    color = Color.White,
                    fontFamily = SFProDisplayFontFamily,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Viora needs camera permission to scan QR codes and seamlessly recruit members to your team.",
                    color = Color.White.copy(alpha = 0.6f),
                    fontFamily = SFProDisplayFontFamily,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(36.dp))

                Button(
                    onClick = { cameraPermissionState.launchPermissionRequest() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VioraNeonLime,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("grant_camera_permission_btn")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Grant Permission",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SFProDisplayFontFamily
                    )
                }
            }
        }

        // Top Navigation Bar (Back Button + Title + Torch Toggle)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1C1C1E).copy(alpha = 0.85f))
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = "QR Scanner",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SFProDisplayFontFamily
            )

            if (cameraPermissionState.status.isGranted && cameraInstance?.cameraInfo?.hasFlashUnit() == true) {
                IconButton(
                    onClick = {
                        isTorchEnabled = !isTorchEnabled
                        cameraInstance?.cameraControl?.enableTorch(isTorchEnabled)
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (isTorchEnabled) VioraNeonLime else Color(0xFF1C1C1E).copy(alpha = 0.85f)
                        )
                ) {
                    Icon(
                        imageVector = if (isTorchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Torch",
                        tint = if (isTorchEnabled) Color.Black else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(42.dp))
            }
        }
    }

    // Scanned User Profile BottomSheet
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    if (showBottomSheet && scannedUser != null) {
        ModalBottomSheet(
            sheetState = sheetState,
            shape = com.example.ui.utils.dynamicBottomSheetShape(sheetState, 28.dp),
            onDismissRequest = {
                showBottomSheet = false
                viewModel.clearScannedUser()
                analyzer.resumeScanning()
            },
            containerColor = Color(0xFF1C1C1E),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(36.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f))
                )
            }
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
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp, top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        if (!user.avatar.isNullOrBlank()) {
            AsyncImage(
                model = com.example.util.ImageUtil.resolveAvatarUrl(user.avatar),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .border(2.dp, VioraNeonLime, CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(VioraNeonLime),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.fullName.take(1).uppercase(),
                    color = Color.Black,
                    fontSize = 28.sp,
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
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 14.sp,
            fontFamily = SFProDisplayFontFamily
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Team Selection
        Text(
            text = "Select Alliance",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = SFProDisplayFontFamily,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            teams.forEach { team ->
                val isSelected = team.id == selectedTeamId
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) VioraNeonLime else Color(0xFF2C2C2E))
                        .clickable { onTeamSelected(team.id) }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = team.name,
                        color = if (isSelected) Color.Black else Color.White,
                        fontFamily = SFProDisplayFontFamily,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onRecruit,
            colors = ButtonDefaults.buttonColors(
                containerColor = VioraNeonLime,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text(
                text = "Recruit Member",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SFProDisplayFontFamily
            )
        }
    }
}

