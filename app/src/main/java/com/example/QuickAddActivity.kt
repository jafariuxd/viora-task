package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.enableEdgeToEdge
import com.example.model.Task
import com.example.model.TaskStatus
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import android.content.Context


class QuickAddActivity : ComponentActivity() {
    

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(0, 0) // Instantly open transparent window
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val focusManager = LocalFocusManager.current
                val keyboardController = LocalSoftwareKeyboardController.current
                var titleText by remember { mutableStateOf("") }
                val focusRequester = remember { FocusRequester() }
                var isVisible by remember { mutableStateOf(false) }
                val scope = rememberCoroutineScope()
                var showSuccessState by remember { mutableStateOf(false) }
                var predictiveBackProgress by remember { mutableFloatStateOf(0f) }
                val animatedBackProgress by animateFloatAsState(
                    targetValue = predictiveBackProgress,
                    animationSpec = spring(
                        dampingRatio = 0.72f,
                        stiffness = 700f
                    ),
                    label = "quickAddSpringBack"
                )

                PredictiveBackHandler(enabled = isVisible) { progressFlow ->
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    try {
                        progressFlow.collect { backEvent ->
                            predictiveBackProgress = backEvent.progress * 0.75f
                        }
                        isVisible = false
                        scope.launch {
                            delay(320)
                            finish()
                            overridePendingTransition(0, 0)
                        }
                    } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                        predictiveBackProgress = 0f
                    }
                }

                val dismissWithAnimation: () -> Unit = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    isVisible = false
                    scope.launch {
                        delay(320)
                        finish()
                        overridePendingTransition(0, 0)
                    }
                }

                val onTaskAddedSuccess: () -> Unit = {
                    scope.launch {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        showSuccessState = true
                        delay(700)
                        dismissWithAnimation()
                    }
                }

                LaunchedEffect(Unit) {
                    isVisible = true
                }

                LaunchedEffect(isVisible) {
                    if (isVisible) {
                        var focused = false
                        var attempts = 0
                        while (!focused && attempts < 15) {
                            try {
                                focusRequester.requestFocus()
                                focused = true
                            } catch (e: Exception) {
                                delay(60)
                                attempts++
                            }
                        }
                    }
                }

                val sfProDisplayRegular = remember { FontFamily(Font(R.font.sf_pro_display_regular)) }
                val sfProDisplayMedium = remember { FontFamily(Font(R.font.sf_pro_display_medium)) }
                val sfProDisplayBold = remember { FontFamily(Font(R.font.sf_pro_display_bold)) }
                val vioraNeonLime = Color(0xFFB4FF00)

                val backgroundAlpha by animateFloatAsState(
                    targetValue = if (isVisible) 0.5f else 0.0f,
                    animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
                    label = "backgroundAlpha"
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = backgroundAlpha))
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = {
                                dismissWithAnimation()
                            })
                        },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(
                            initialOffsetY = { (it * 0.35f).toInt() }, // Snap up from 35% offset for high speed
                            animationSpec = spring(
                                dampingRatio = 0.78f, // Responsive premium spring
                                stiffness = 600f     // Ultra responsive stiffness
                            )
                        ) + fadeIn(
                            animationSpec = tween(durationMillis = 150)
                        ),
                        exit = slideOutVertically(
                            targetOffsetY = { (it * 0.35f).toInt() },
                            animationSpec = spring(
                                dampingRatio = 0.78f,
                                stiffness = 650f
                            )
                        ) + fadeOut(
                            animationSpec = tween(durationMillis = 150)
                        )
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .imePadding() // Adjust for keyboard
                                .padding(bottom = 16.dp) // Extra padding from bottom
                                .graphicsLayer {
                                    val progress = animatedBackProgress
                                    val scale = 1f - (progress * 0.12f)
                                    scaleX = scale
                                    scaleY = scale
                                    translationY = progress * 70.dp.toPx()
                                    alpha = 1f - (progress * 0.35f)
                                    shape = RoundedCornerShape((28 + progress * 8).dp)
                                    clip = true
                                }
                                .animateContentSize(), // Smoothly animate card height!
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            AnimatedContent(
                                targetState = showSuccessState,
                                transitionSpec = {
                                    (fadeIn(animationSpec = tween(150)) + slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(150)))
                                        .togetherWith(fadeOut(animationSpec = tween(100)) + slideOutVertically(targetOffsetY = { -it / 2 }, animationSpec = tween(100)))
                                },
                                label = "QuickAddSuccessTransition"
                            ) { success ->
                                if (!success) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp)
                                    ) {
                                        Text(
                                            text = "Quick Task",
                                            color = Color.Black.copy(alpha = 0.5f),
                                            fontSize = 14.sp,
                                            fontFamily = sfProDisplayMedium,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )

                                        BasicTextField(
                                            value = titleText,
                                            onValueChange = { titleText = it },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .focusRequester(focusRequester),
                                            textStyle = TextStyle(
                                                color = Color.Black,
                                                fontSize = 32.sp,
                                                fontFamily = sfProDisplayRegular,
                                                fontWeight = FontWeight.Normal
                                            ),
                                            cursorBrush = SolidColor(Color.Black),
                                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                            keyboardActions = KeyboardActions(
                                                onDone = {
                                                    if (titleText.isNotBlank()) {
                                                        saveTask(titleText) {
                                                            onTaskAddedSuccess()
                                                        }
                                                    } else {
                                                        dismissWithAnimation()
                                                    }
                                                }
                                            ),
                                            decorationBox = { innerTextField ->
                                                if (titleText.isEmpty()) {
                                                    Text(
                                                        text = "What's on your mind?",
                                                        color = Color.Black.copy(alpha = 0.4f),
                                                        fontSize = 32.sp,
                                                        fontFamily = sfProDisplayRegular,
                                                        fontWeight = FontWeight.Normal
                                                    )
                                                }
                                                innerTextField()
                                            }
                                        )

                                        Spacer(modifier = Modifier.height(24.dp))

                                        Button(
                                            onClick = { 
                                                if (titleText.isNotBlank()) {
                                                    saveTask(titleText) {
                                                        onTaskAddedSuccess()
                                                    }
                                                } else {
                                                    dismissWithAnimation()
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(56.dp),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = vioraNeonLime,
                                                contentColor = Color.Black
                                            )
                                        ) {
                                            Text(
                                                text = "Add Task",
                                                fontSize = 16.sp,
                                                fontFamily = sfProDisplayBold,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                } else {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 36.dp, horizontal = 24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.CheckCircle,
                                            contentDescription = "Success",
                                            tint = vioraNeonLime,
                                            modifier = Modifier.size(64.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Task Added",
                                            color = Color.Black,
                                            fontSize = 20.sp,
                                            fontFamily = sfProDisplayBold,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun saveTask(title: String, onSaved: () -> Unit) {
        val newTask = Task(
            id = java.util.UUID.randomUUID().toString(),
            title = title,
            client = "Viora design",
            userId = "user1",
            folder = "Unplanned Tasks",
            daysLeft = 7,
            status = TaskStatus.TODO
        )
        
        Thread {
            try {
                val prefs = getSharedPreferences("viora_task_prefs", Context.MODE_PRIVATE)
                val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
                val tasksAdapter = moshi.adapter<List<Task>>(Types.newParameterizedType(List::class.java, Task::class.java))
                
                val tasksJson = prefs.getString("tasks", null)
                val currentTasks = if (tasksJson != null) {
                    tasksAdapter.fromJson(tasksJson)?.toMutableList() ?: mutableListOf()
                } else {
                    mutableListOf()
                }
                
                currentTasks.add(0, newTask) // Add to top
                
                prefs.edit().putString("tasks", tasksAdapter.toJson(currentTasks)).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            runOnUiThread {
                onSaved()
            }
        }.start()
    }
}
