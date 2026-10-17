package com.example.boji.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.sp

/** 小时 / 分钟二选一的时间选择器（分钟步进 5） */
@Composable
fun TimePickerDialog(
    title: String,
    initialMinute: Int,
    tint: Color = MaterialTheme.colorScheme.primary,
    onDismiss: () -> Unit,
    onConfirm: (minuteOfDay: Int) -> Unit
) {
    var hour by remember { mutableIntStateOf(initialMinute / 60) }
    var minute by remember { mutableIntStateOf((initialMinute % 60) / 5 * 5) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NumberColumn(
                    value = hour,
                    range = 0..23,
                    tint = tint,
                    onChange = { hour = it }
                )
                Text(
                    ":",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                NumberColumn(
                    value = minute,
                    range = 0..55,
                    step = 5,
                    tint = tint,
                    onChange = { minute = it }
                )
            }
        },
        confirmButton = {
            FilledTonalButton(onClick = { onConfirm(hour * 60 + minute) }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
private fun NumberColumn(
    value: Int,
    range: IntRange,
    step: Int = 1,
    tint: Color,
    onChange: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = {
            val next = value - step
            onChange(if (next < range.first) range.last else next)
        }) {
            Icon(Icons.Filled.Remove, contentDescription = "减", tint = tint)
        }
        Text(
            text = String.format("%02d", value),
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(72.dp),
            textAlign = TextAlign.Center
        )
        IconButton(onClick = {
            val next = value + step
            onChange(if (next > range.last) range.first else next)
        }) {
            Icon(Icons.Filled.Add, contentDescription = "加", tint = tint)
        }
    }
}

/** 自定义数值输入 */
@Composable
fun NumberInputDialog(
    title: String,
    initialValue: String,
    unit: String,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.filter { c -> c.isDigit() }.take(6) },
                singleLine = true,
                suffix = { Text(unit) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(14.dp)
            )
        },
        confirmButton = {
            FilledTonalButton(onClick = {
                val v = text.toIntOrNull()
                if (v != null && v > 0) onConfirm(v)
            }) { Text("确定") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
