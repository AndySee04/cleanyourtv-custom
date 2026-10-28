package com.gathin.tvhome;

import android.service.notification.NotificationListenerService;

/**
 * Lets NowPlayingCard read the active media sessions (MediaSessionManager.getActiveSessions needs an enabled
 * notification listener). It reads no notification itself. Enable it once with
 * `cmd notification allow_listener com.gathin.tvhome/com.gathin.tvhome.MediaNoteService`.
 * From github.com/mosami11-11/cleanyourtv-custom.
 */
public class MediaNoteService extends NotificationListenerService {
}
