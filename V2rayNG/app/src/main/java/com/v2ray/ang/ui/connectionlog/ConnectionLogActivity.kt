package com.v2ray.ang.ui.connectionlog

import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.v2ray.ang.R
import com.v2ray.ang.dto.ConnectionLogEntry
import com.v2ray.ang.handler.ConnectionLogEntries
import com.v2ray.ang.ui.base.BaseComponentActivity
import com.v2ray.ang.ui.compose.AppTopBar
import com.v2ray.ang.ui.compose.ItemDivider
import com.v2ray.ang.ui.compose.NavigationBarsBottomPadding
import com.v2ray.ang.ui.compose.verticalScrollbar
import java.text.DateFormat
import java.util.Date

class ConnectionLogActivity : BaseComponentActivity() {
    private val viewModel: ConnectionLogViewModel by viewModels()

    @Composable
    override fun ScreenContent() {
        ConnectionLogScreen(
            viewModel = viewModel,
            onBackClick = { finish() },
        )
    }
}

@Composable
fun ConnectionLogScreen(
    viewModel: ConnectionLogViewModel,
    onBackClick: () -> Unit,
) {
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val locale = LocalConfiguration.current.locales[0]
    val timeFormatter = remember(locale) {
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            AppTopBar(
                title = stringResource(R.string.title_connection_log),
                onBackClick = onBackClick,
                isLoading = isLoading,
                actions = {
                    IconButton(onClick = { viewModel.clear() }) {
                        Icon(
                            painterResource(R.drawable.ic_delete_24dp),
                            contentDescription = stringResource(R.string.acc_clear_connection_log)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (entries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.connection_log_empty),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScrollbar(listState),
                contentPadding = NavigationBarsBottomPadding()
            ) {
                items(items = entries, key = { it.id }) { entry ->
                    ConnectionLogRow(
                        entry = entry,
                        timeText = timeFormatter.format(Date(entry.atMillis)),
                    )
                    ItemDivider()
                }
            }
        }
    }
}

@Composable
private fun ConnectionLogRow(
    entry: ConnectionLogEntry,
    timeText: String,
) {
    val unknown = stringResource(R.string.connection_log_unknown)
    val fromName = entry.fromName.ifBlank { unknown }
    val toName = entry.toName.ifBlank { unknown }
    val message = when (entry.type) {
        ConnectionLogEntries.TYPE_START -> stringResource(R.string.connection_log_start, toName)
        ConnectionLogEntries.TYPE_STOP -> stringResource(R.string.connection_log_stop, fromName)
        ConnectionLogEntries.TYPE_FAILOVER ->
            stringResource(R.string.connection_log_failover, fromName, toName)
        ConnectionLogEntries.TYPE_AUTO_SELECT ->
            stringResource(R.string.connection_log_auto_select, toName)
        else -> toName.ifBlank { fromName }
    }
    val spoken = "$timeText. $message"
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { contentDescription = spoken }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(text = timeText, style = MaterialTheme.typography.bodySmall)
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
