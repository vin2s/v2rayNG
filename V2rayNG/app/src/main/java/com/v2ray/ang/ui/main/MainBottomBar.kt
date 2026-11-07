package com.v2ray.ang.ui.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.v2ray.ang.R
import com.v2ray.ang.ui.compose.AppDivider
import com.v2ray.ang.ui.compose.colorFabActive
import com.v2ray.ang.ui.compose.colorFabInactiveDark
import com.v2ray.ang.ui.compose.colorFabInactiveLight
import kotlinx.coroutines.launch

@Composable
fun MainBottomBar(
    displayText: String,
    isRunning: Boolean,
    isTesting: Boolean,
    isDarkTheme: Boolean,
    onAction: (MainAction) -> Unit
) {
    val scope = rememberCoroutineScope()
    val rotationAnim = remember { Animatable(0f) }
    val inactiveFabColor = if (isDarkTheme) colorFabInactiveDark else colorFabInactiveLight

    LaunchedEffect(isRunning) {
        if (!isRunning) {
            rotationAnim.snapTo(0f)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        AppDivider()
        Text(
            text = displayText,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = { onAction(MainAction.TestCurrentServer) })
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .semantics {
                    contentDescription = displayText
                }
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomBarFab(
                iconRes = R.drawable.ic_speed_24dp,
                labelRes = R.string.title_real_ping_all_server,
                containerColor = if (isTesting) colorFabActive else inactiveFabColor,
                onClick = { onAction(MainAction.TestRealAllServers) }
            )
            BottomBarFab(
                iconRes = if (isRunning) R.drawable.ic_stop_24dp else R.drawable.ic_play_24dp,
                labelRes = if (isRunning) R.string.acc_stop else R.string.acc_start,
                containerColor = if (isRunning) colorFabActive else inactiveFabColor,
                iconRotation = rotationAnim.value,
                onClick = {
                    if (!isRunning) {
                        scope.launch {
                            rotationAnim.animateTo(
                                targetValue = 720f,
                                animationSpec = tween(durationMillis = 3000)
                            )
                        }
                    }
                    onAction(MainAction.ToggleService)
                }
            )
            BottomBarFab(
                iconRes = R.drawable.ic_sort_24dp,
                labelRes = R.string.title_sort_by_test_results,
                containerColor = inactiveFabColor,
                onClick = { onAction(MainAction.SortByTestResults) }
            )
        }
    }
}

@Composable
private fun BottomBarFab(
    @DrawableRes iconRes: Int,
    @StringRes labelRes: Int,
    containerColor: Color,
    onClick: () -> Unit,
    iconRotation: Float = 0f,
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = containerColor
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = stringResource(labelRes),
            tint = Color.White,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer { rotationZ = iconRotation }
        )
    }
}
