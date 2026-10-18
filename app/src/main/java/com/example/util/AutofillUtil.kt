package com.example.util

import android.content.Context
import android.os.Build
import android.view.autofill.AutofillManager
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree

object AutofillUtil {
    fun commitAutofill(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val autofillManager = context.getSystemService(AutofillManager::class.java)
                autofillManager?.commit()
            } catch (e: Exception) {
                // Ignore autofill commit errors
            }
        }
    }

    fun cancelAutofill(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val autofillManager = context.getSystemService(AutofillManager::class.java)
                autofillManager?.cancel()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.autofill(
    autofillTypes: List<AutofillType>,
    onFill: ((String) -> Unit),
): Modifier = composed {
    val autofill = LocalAutofill.current
    val autofillTree = LocalAutofillTree.current
    val autofillNode = remember {
        try {
            AutofillNode(onFill = onFill, autofillTypes = autofillTypes)
        } catch (e: Exception) {
            null
        }
    }
    
    if (autofillNode != null && autofillTree != null) {
        try {
            autofillTree.plusAssign(autofillNode)
        } catch (e: Exception) {
            // Ignore tree registration failure
        }
    }

    this
        .onGloballyPositioned {
            try {
                autofillNode?.boundingBox = it.boundsInWindow()
            } catch (e: Exception) {
                // Ignore
            }
        }
        .onFocusChanged { focusState ->
            try {
                autofill?.run {
                    autofillNode?.let { node ->
                        if (focusState.isFocused) {
                            requestAutofillForNode(node)
                        } else {
                            cancelAutofillForNode(node)
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
}
