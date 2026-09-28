package org.example.desky.core.presentation.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import org.example.desky.R
import org.example.desky.core.presentation.theme.DeskyTheme

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int? = null,
    iconTint: Color = MaterialTheme.colorScheme.error,
    confirmButtonColor: Color = MaterialTheme.colorScheme.error,
    confirmContentColor: Color = MaterialTheme.colorScheme.onError,
    dismissText: String = "Cancel",
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = iconRes?.let { resId ->
            {
                Icon(
                    painter = painterResource(id = resId),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = confirmButtonColor,
                    contentColor = confirmContentColor
                )
            ) {
                Text(
                    text = confirmText,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = dismissText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
    )
}

@PreviewLightDark
@Composable
private fun PreviewConfirmationDialog() {
    DeskyTheme {
        ConfirmationDialog(
            title = "Shut Down Workstation?",
            message = "Are you sure you want to turn off the laptop? Any unsaved work will be lost.",
            confirmText = "Shut Down",
            iconRes = R.drawable.power,
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun PreviewConfirmationDialogAmber() {
    DeskyTheme {
        ConfirmationDialog(
            title = "Restart Workstation?",
            message = "Are you sure you want to reboot the system? All active applications will close.",
            confirmText = "Restart",
            iconRes = R.drawable.refresh,
            iconTint = MaterialTheme.colorScheme.tertiary,
            confirmButtonColor = MaterialTheme.colorScheme.tertiary,
            confirmContentColor = MaterialTheme.colorScheme.onTertiary,
            onConfirm = {},
            onDismiss = {}
        )
    }
}
