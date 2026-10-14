package org.example.desky.core.presentation.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import org.example.desky.core.presentation.theme.DeskyTheme

@Composable
fun TopLevelAppBar(
    title: String,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Crossfade(
                targetState = title,
                animationSpec = tween(durationMillis = 200),
                label = "TopLevelAppBarTitleCrossfade"
            ) { targetTitle ->
                Text(
                    text = targetTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    )
}

@PreviewLightDark
@Composable
fun PreviewTopLevelAppBar() {
    DeskyTheme {
        TopLevelAppBar(
            title = "Desky"
        )
    }
}