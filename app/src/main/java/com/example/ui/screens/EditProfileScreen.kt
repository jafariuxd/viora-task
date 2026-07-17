package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DefaultDeadlineSelector
import com.example.ui.theme.SFProDisplayFontFamily
import com.example.ui.theme.VioraBackground
import com.example.ui.theme.VioraNeonLime
import com.example.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    var fullName by remember { mutableStateOf("Mohammad Mahdi Jafari") }
    var username by remember { mutableStateOf("jafariuxd") }
    var email by remember { mutableStateOf("jafariuxd@gmail.com") }
    var defaultDeadline by remember { mutableStateOf("Weekly") }
    var customDays by remember { mutableStateOf(3) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VioraBackground)
            .navigationBarsPadding()
            .imePadding()
            .padding(top = 48.dp) // Status bar padding
            .verticalScroll(rememberScrollState())
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .border(1.dp, Color(0xFF4A4A4A), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
            
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = VioraNeonLime, contentColor = Color.Black),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Save",
                    fontSize = 16.sp,
                    fontFamily = SFProDisplayFontFamily,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Avatar
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(120.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_profile_mohammad_1783672402325),
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
            
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(VioraNeonLime)
                    .clickable { /* Change Photo */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Change Photo",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Text Fields
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ProfileTextField(
                label = "Full name",
                value = fullName,
                onValueChange = { fullName = it },
                isFocusedMock = true
            )
            
            ProfileTextField(
                label = "Username",
                value = username,
                onValueChange = { username = it },
                prefix = "@"
            )
            
            ProfileTextField(
                label = "Email",
                value = email,
                onValueChange = { email = it }
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Default Deadline
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Default Deadline",
                color = Color.White,
                fontSize = 14.sp,
                fontFamily = SFProDisplayFontFamily,
                fontWeight = FontWeight.Normal
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            com.example.ui.components.DefaultDeadlineSelector(
                selectedOption = defaultDeadline,
                onOptionSelected = { defaultDeadline = it },
                customDays = customDays,
                onCustomDaysChanged = { customDays = it },
                textColor = Color.White,
                unselectedTextColor = Color.White,
                borderColor = Color.White,
                selectedBackgroundColor = VioraNeonLime,
                selectedItemTextColor = Color.Black,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "* Used when no deadline is defined on task, list or team level.",
                color = Color(0xFFAAAAAA),
                fontSize = 13.sp,
                fontFamily = SFProDisplayFontFamily,
                lineHeight = 20.sp
            )
            
            if (defaultDeadline == "Custom") {
                Spacer(modifier = Modifier.height(32.dp))
                // Number Picker Mock
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("1", color = Color(0xFF333333), fontSize = 32.sp, fontWeight = FontWeight.Bold, fontFamily = SFProDisplayFontFamily)
                        Text("2", color = Color(0xFF666666), fontSize = 32.sp, fontWeight = FontWeight.Bold, fontFamily = SFProDisplayFontFamily)
                        Text("3", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold, fontFamily = SFProDisplayFontFamily)
                        Text("4", color = Color(0xFF666666), fontSize = 32.sp, fontWeight = FontWeight.Bold, fontFamily = SFProDisplayFontFamily)
                        Text("5", color = Color(0xFF333333), fontSize = 32.sp, fontWeight = FontWeight.Bold, fontFamily = SFProDisplayFontFamily)
                    }
                    
                    Spacer(modifier = Modifier.width(24.dp))
                    
                    Text("Days", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold, fontFamily = SFProDisplayFontFamily)
                }
                
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    prefix: String? = null,
    isFocusedMock: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontFamily = SFProDisplayFontFamily) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VioraNeonLime,
            unfocusedBorderColor = if (isFocusedMock) VioraNeonLime else Color.White,
            focusedLabelColor = VioraNeonLime,
            unfocusedLabelColor = Color.White,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = VioraNeonLime,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        ),
        modifier = Modifier.fillMaxWidth().height(64.dp),
        shape = RoundedCornerShape(32.dp),
        singleLine = true,
        leadingIcon = if (prefix != null) {
            {
                Text(
                    text = prefix,
                    color = VioraNeonLime,
                    fontSize = 18.sp,
                    fontFamily = SFProDisplayFontFamily,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        } else null,
        textStyle = TextStyle(fontSize = 18.sp, fontFamily = SFProDisplayFontFamily)
    )
}
