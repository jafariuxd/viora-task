package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.PersonAddAlt1
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.User
import com.example.ui.theme.SFProDisplayFontFamily
import com.example.ui.theme.VioraNeonLime
import com.example.viewmodel.SearchViewModel
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onBack: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Search Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(Color(0xFF2A2A2A))
                .padding(start = 4.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(4.dp))
            
            BasicTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 24.sp,
                    fontFamily = SFProDisplayFontFamily,
                    fontWeight = FontWeight.Normal
                ),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(VioraNeonLime),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search...",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 24.sp,
                                fontFamily = SFProDisplayFontFamily
                            )
                        }
                        innerTextField()
                    }
                },
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .fillMaxWidth()
            )
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
        ) {
            items(searchResults) { user ->
                UserRow(user)
            }
        }
    }
}

@Composable
fun UserRow(user: User) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        ) {
            if (user.avatarRes != null) {
                Image(
                    painter = painterResource(id = user.avatarRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = user.name,
                color = Color.White,
                fontSize = 18.sp,
                fontFamily = SFProDisplayFontFamily,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = user.username,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp,
                fontFamily = SFProDisplayFontFamily
            )
        }
        
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(VioraNeonLime)
                .clickable { /* Add user */ },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.PersonAddAlt1,
                contentDescription = "Add User",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
