package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.SFProDisplayFontFamily
import com.example.ui.utils.QRCodeGenerator
import com.example.viewmodel.VioraTaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VioraPassBottomSheet(
    viewModel: VioraTaskViewModel,
    onDismissRequest: () -> Unit,
    onNavigateToScanner: () -> Unit
) {
    val userName by viewModel.userName.collectAsState()
    val userHandle by viewModel.userHandle.collectAsState()
    val avatarUri by viewModel.userAvatarUri.collectAsState()

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = com.example.ui.utils.dynamicBottomSheetShape(sheetState = sheetState, defaultRadius = 28.dp),
        containerColor = Color.White,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD1D1D6))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Description header as in requested reference layout
            Text(
                text = "Here is your unique Viora QR code that will transfer information about your profile.",
                color = Color(0xFF1C1C1E),
                fontFamily = SFProDisplayFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // User Info Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF2F2F7))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (avatarUri != null) {
                    AsyncImage(
                        model = avatarUri,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFB4FF00)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.take(1).uppercase(),
                            color = Color.Black,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = SFProDisplayFontFamily
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = userName,
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SFProDisplayFontFamily
                    )
                    Text(
                        text = "@$userHandle",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontFamily = SFProDisplayFontFamily
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // QR Code Box
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE5E5EA), RoundedCornerShape(24.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                val qrBitmap = remember(userHandle) {
                    QRCodeGenerator.generateQRCode("viora://recruit/$userHandle", 600)
                }
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR Code for $userName",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = "Failed to load",
                        color = Color.Black,
                        fontFamily = SFProDisplayFontFamily
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Buttons Row (Matching the reference layout)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Button 1: QR Code (Selected Active Tab)
                Surface(
                    onClick = { /* Already showing QR code */ },
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1C1C1E),
                    modifier = Modifier
                        .weight(1f)
                        .height(72.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "QR Code",
                            tint = Color(0xFFB4FF00),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "QR Code",
                            color = Color.White,
                            fontFamily = SFProDisplayFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                // Button 2: Scan QR Code
                Surface(
                    onClick = {
                        onDismissRequest()
                        onNavigateToScanner()
                    },
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFF2F2F7),
                    modifier = Modifier
                        .weight(1f)
                        .height(72.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan QR Code",
                            tint = Color(0xFF1C1C1E),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Scan Qr Code",
                            color = Color(0xFF1C1C1E),
                            fontFamily = SFProDisplayFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VioraPassScreen(
    viewModel: VioraTaskViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToScanner: () -> Unit
) {
    VioraPassBottomSheet(
        viewModel = viewModel,
        onDismissRequest = onNavigateBack,
        onNavigateToScanner = onNavigateToScanner
    )
}
