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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.VioraNeonLime
import com.example.viewmodel.UserProfileViewModel

@Composable
fun UserProfileScreen(
    viewModel: UserProfileViewModel,
    onBack: () -> Unit,
    onNavigateToEditProfile: () -> Unit
) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    var expandedMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar & Avatar
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Back Button
            Box(
                modifier = Modifier
                    .padding(start = 20.dp, top = 20.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }

            // More Button
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 20.dp, top = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                        .clickable { expandedMenu = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = Color.White
                    )
                }

                DropdownMenu(
                    expanded = expandedMenu,
                    onDismissRequest = { expandedMenu = false },
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(8.dp)
                ) {
                    DropdownMenuItem(
                        text = { Text("Share Profile", color = Color.Black, fontSize = 16.sp) },
                        onClick = { expandedMenu = false },
                        leadingIcon = { Icon(Icons.Default.QrCode, contentDescription = null, tint = Color.Black) }
                    )
                    DropdownMenuItem(
                        text = { Text("Notification Settings", color = Color.Black, fontSize = 16.sp) },
                        onClick = { expandedMenu = false },
                        leadingIcon = { Icon(Icons.Default.NotificationsNone, contentDescription = null, tint = Color.Black) }
                    )
                    DropdownMenuItem(
                        text = { Text("Logout", color = Color.Red, fontSize = 16.sp) },
                        onClick = { expandedMenu = false },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color.Red) }
                    )
                }
            }

            // Avatar Profile
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 57.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(125.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                ) {
                    profile?.profileImageRes?.let { res ->
                        Image(
                            painter = painterResource(id = res),
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                
                // Camera Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(VioraNeonLime)
                        .clickable { /* Handle change photo */ },
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
        }

        profile?.let { userProfile ->
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = userProfile.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = userProfile.username,
                fontSize = 16.sp,
                color = VioraNeonLime
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onNavigateToEditProfile,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(text = "Edit profile", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Activities Section
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Activities",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    ActivityCard(
                        title = "Assigned Tasks",
                        count = userProfile.stats.assignedTasks.toString(),
                        unit = "Tasks",
                        pillColor = Color(0xFFD3E3FC), // Light Blue
                        modifier = Modifier.weight(1f)
                    )
                    ActivityCard(
                        title = "Tasks Completed",
                        count = userProfile.stats.completedTasks.toString(),
                        unit = "Tasks",
                        pillColor = Color(0xFFCEF4AC), // Light Green
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    ActivityCard(
                        title = "Overdue",
                        count = userProfile.stats.overdueTasks.toString(),
                        unit = "Tasks",
                        pillColor = Color(0xFFFFD9D9), // Light Red
                        modifier = Modifier.weight(1f)
                    )
                    ActivityCard(
                        title = "Active Teams",
                        count = userProfile.stats.activeTeams.toString(),
                        unit = "Teams",
                        pillColor = Color(0xFFFDE293), // Light Yellow
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                // Default Deadline Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp)
                        .height(72.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .height(23.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F3F4))
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Default Deadline",
                            fontSize = 12.sp,
                            color = Color.Black
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = userProfile.stats.defaultDeadlineDays.toString(),
                            fontSize = 36.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Days",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F3F4))
                                .clickable { /* Edit deadline */ },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Deadline",
                                tint = Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Member since ${userProfile.joinDate}",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ActivityCard(
    title: String,
    count: String,
    unit: String,
    pillColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(101.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .height(23.dp)
                .clip(CircleShape)
                .background(pillColor)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color.Black
            )
        }
        
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = count,
                fontSize = 40.sp,
                color = Color.Black,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.alignByBaseline()
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = unit,
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.alignByBaseline()
            )
        }
    }
}
