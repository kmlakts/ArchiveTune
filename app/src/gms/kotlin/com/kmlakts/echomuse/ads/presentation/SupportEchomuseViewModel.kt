/*
 * Echomuse (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.kmlakts.echomuse.ads.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import com.kmlakts.echomuse.ads.domain.OpenSupportPageUseCase
import com.kmlakts.echomuse.ads.domain.SupportPageOpenResult
import javax.inject.Inject

internal sealed interface SupportEchomuseScreenState {
    @Immutable
    data object Loading : SupportEchomuseScreenState

    @Immutable
    data object Success : SupportEchomuseScreenState

    @Immutable
    data object Empty : SupportEchomuseScreenState

    @Immutable
    data class Error(
        val reason: SupportEchomuseError,
    ) : SupportEchomuseScreenState
}

internal enum class SupportEchomuseError {
    PageUnavailable,
}

internal enum class SupportEchomuseUiEvent {
    OpenFailed,
}

@HiltViewModel
internal class SupportEchomuseViewModel
    @Inject
    constructor(
        private val openSupportPage: OpenSupportPageUseCase,
    ) : ViewModel() {
        private val _screenState =
            MutableStateFlow<SupportEchomuseScreenState>(SupportEchomuseScreenState.Success)
        val screenState: StateFlow<SupportEchomuseScreenState> = _screenState.asStateFlow()

        private val eventChannel = Channel<SupportEchomuseUiEvent>(Channel.BUFFERED)
        val events = eventChannel.receiveAsFlow()

        fun onSupportEchomuseClick() {
            if (_screenState.value is SupportEchomuseScreenState.Loading) return
            _screenState.value = SupportEchomuseScreenState.Loading
            when (openSupportPage()) {
                SupportPageOpenResult.Opened -> {
                    _screenState.value = SupportEchomuseScreenState.Success
                }

                SupportPageOpenResult.Unavailable -> {
                    _screenState.value =
                        SupportEchomuseScreenState.Error(SupportEchomuseError.PageUnavailable)
                    eventChannel.trySend(SupportEchomuseUiEvent.OpenFailed)
                }
            }
        }
    }
