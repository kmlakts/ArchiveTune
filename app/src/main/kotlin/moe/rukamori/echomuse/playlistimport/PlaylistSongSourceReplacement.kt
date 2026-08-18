/*
 * Echomuse (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.echomuse.playlistimport

import moe.rukamori.echomuse.db.entities.PlaylistSongMap

internal fun PlaylistSongMap.replaceSongSource(replacementSongId: String): PlaylistSongMap {
    require(replacementSongId.isNotBlank()) { "Replacement song ID must not be blank" }
    return copy(
        songId = replacementSongId,
        setVideoId = null,
    )
}
