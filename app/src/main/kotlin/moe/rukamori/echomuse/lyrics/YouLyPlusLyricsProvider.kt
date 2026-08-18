/*
 * Echomuse (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.echomuse.lyrics

import android.content.Context
import android.util.Log
import moe.rukamori.echomuse.constants.EnableYouLyPlusLyricsKey
import moe.rukamori.echomuse.utils.GlobalLog
import moe.rukamori.echomuse.utils.dataStore
import moe.rukamori.echomuse.utils.get
import moe.rukamori.archivetune.youlyplus.YouLyPlus

object YouLyPlusLyricsProvider : LyricsProvider {
    init {
        YouLyPlus.logger = { message ->
            GlobalLog.append(Log.INFO, "YouLyPlus", message)
        }
    }

    override val name = "YouLyPlus"

    override fun isEnabled(context: Context): Boolean = context.dataStore[EnableYouLyPlusLyricsKey] ?: true

    override suspend fun getLyrics(
        id: String,
        title: String,
        artist: String,
        album: String?,
        duration: Int,
    ): Result<String> =
        YouLyPlus.getLyrics(
            title = title,
            artist = artist,
            album = album,
            durationSeconds = duration,
        )

    override suspend fun getAllLyrics(
        id: String,
        title: String,
        artist: String,
        album: String?,
        duration: Int,
        callback: (String) -> Unit,
    ) {
        YouLyPlus.getAllLyrics(
            title = title,
            artist = artist,
            album = album,
            durationSeconds = duration,
            callback = callback,
        )
    }
}
