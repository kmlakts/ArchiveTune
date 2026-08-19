/*
 * Echomuse (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.kmlakts.echomuse.playback

import androidx.media3.common.ForwardingSimpleBasePlayer
import androidx.media3.common.Player

/**
 * A [Player] facade with a STABLE identity: this object is created once and
 * bound to [androidx.media3.session.MediaSession] once, and that binding is
 * never changed again. Internally it can be repointed to a different
 * delegate [Player] (one of our two crossfade decks) via [switchTo], which
 * uses ForwardingSimpleBasePlayer's officially supported player-replacement
 * mechanism (media3 1.8.0+, verified present via javap against the actual
 * media3-common-1.10.1 dependency this app builds against).
 *
 * This exists specifically so MediaSession never needs its own player
 * reference reassigned at runtime — which is not something we've found to
 * be reliable — while still letting us swap which underlying ExoPlayer deck
 * is actually driving playback with zero seeks on the audible deck.
 */
class DeckSwitchingPlayer(
    initialPlayer: Player,
) : ForwardingSimpleBasePlayer(initialPlayer) {
    fun switchTo(newPlayer: Player) {
        if (newPlayer === player) return
        setPlayer(newPlayer)
    }
}
