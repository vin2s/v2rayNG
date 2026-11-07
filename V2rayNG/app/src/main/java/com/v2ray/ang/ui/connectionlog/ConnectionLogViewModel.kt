package com.v2ray.ang.ui.connectionlog

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.v2ray.ang.R
import com.v2ray.ang.dto.ConnectionLogEntry
import com.v2ray.ang.handler.ConnectionLog
import com.v2ray.ang.ui.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConnectionLogViewModel(application: Application) : BaseViewModel(application) {
    private val _entries = MutableStateFlow<List<ConnectionLogEntry>>(emptyList())
    val entries: StateFlow<List<ConnectionLogEntry>> = _entries.asStateFlow()

    init {
        reload()
    }

    fun reload() {
        viewModelScope.launch(Dispatchers.IO) {
            _entries.value = ConnectionLog.read()
        }
    }

    fun clear() {
        viewModelScope.launch(Dispatchers.IO) {
            ConnectionLog.clear()
            _entries.value = emptyList()
            toastSuccess(R.string.toast_success)
        }
    }
}
