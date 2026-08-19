/*
 * Echomuse (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.kmlakts.echomuse.viewmodels

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.kmlakts.echomuse.constants.AutoPlaylistSongSortDescendingKey
import com.kmlakts.echomuse.constants.AutoPlaylistSongSortType
import com.kmlakts.echomuse.constants.AutoPlaylistSongSortTypeKey
import com.kmlakts.echomuse.constants.HideExplicitKey
import com.kmlakts.echomuse.constants.HideVideoKey
import com.kmlakts.echomuse.constants.SongSortType
import com.kmlakts.echomuse.db.MusicDatabase
import com.kmlakts.echomuse.extensions.filterExplicit
import com.kmlakts.echomuse.extensions.toEnum
import com.kmlakts.echomuse.utils.SyncUtils
import com.kmlakts.echomuse.utils.dataStore
import com.kmlakts.echomuse.utils.get
import com.kmlakts.echomuse.utils.reportException
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AutoPlaylistViewModel
    @Inject
    constructor(
        @ApplicationContext context: Context,
        database: MusicDatabase,
        savedStateHandle: SavedStateHandle,
        private val syncUtils: SyncUtils,
    ) : ViewModel() {
        val playlist = savedStateHandle.get<String>("playlist")!!

        private val _isRefreshing = MutableStateFlow(false)
        val isRefreshing = _isRefreshing.asStateFlow()

        private fun AutoPlaylistSongSortType.toSongSortType(): SongSortType =
            when (this) {
                AutoPlaylistSongSortType.CREATE_DATE -> SongSortType.CREATE_DATE
                AutoPlaylistSongSortType.NAME -> SongSortType.NAME
                AutoPlaylistSongSortType.ARTIST -> SongSortType.ARTIST
                AutoPlaylistSongSortType.PLAY_TIME -> SongSortType.PLAY_TIME
            }

        @OptIn(ExperimentalCoroutinesApi::class)
        val likedSongs =
            context.dataStore.data
                .map {
                    Triple(
                        it[AutoPlaylistSongSortTypeKey].toEnum(AutoPlaylistSongSortType.CREATE_DATE) to (
                            it[AutoPlaylistSongSortDescendingKey]
                                ?: true
                        ),
                        it[HideExplicitKey] ?: false,
                        it[HideVideoKey] ?: false,
                    )
                }.distinctUntilChanged()
                .flatMapLatest { (sortDesc, hideExplicit, hideVideo) ->
                    val (sortType, descending) = sortDesc
                    val songSortType = sortType.toSongSortType()
                    when (playlist) {
                        "liked" -> {
                            database.likedSongs(songSortType, descending, hideVideo).map { it.filterExplicit(hideExplicit) }
                        }

                        else -> {
                            MutableStateFlow(emptyList())
                        }
                    }
                }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        fun refresh() {
            if (_isRefreshing.value) return
            viewModelScope.launch(Dispatchers.IO) {
                _isRefreshing.value = true
                try {
                    when (playlist) {
                        "liked" -> syncUtils.syncLikedSongs()
                        else -> Unit
                    }
                } catch (e: Exception) {
                    reportException(e)
                } finally {
                    _isRefreshing.value = false
                }
            }
        }

        fun syncLikedSongs() {
            refresh()
        }
    }
