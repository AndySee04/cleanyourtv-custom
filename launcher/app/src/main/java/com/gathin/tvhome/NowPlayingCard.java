package com.gathin.tvhome;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;

/**
 * A frosted "Now Playing" card under the clock: album art, title, artist and Prev / Play-Pause / Next for the
 * first active media session that has a title. Refreshed when Home resumes and on each clock tick; hidden when
 * nothing plays or MediaNoteService has no listener access. From github.com/mosami11-11/cleanyourtv-custom.
 */
public class NowPlayingCard {
    private LinearLayout card;
    private ImageView art;
    private TextView title, artist;
    private ImageView btnPlay;
    private MediaController activeCtrl;
    private final Activity act;
    private final float d;
    /** The playing app's own track and state changes, so Prev / Next show the new title at once, without polling. */
    private final MediaController.Callback onChange = new MediaController.Callback() {
        @Override
        public void onMetadataChanged(MediaMetadata metadata) {
            refresh();
        }

        @Override
        public void onPlaybackStateChanged(PlaybackState state) {
            refresh();
        }

        @Override
        public void onSessionDestroyed() {
            refresh();
        }
    };

    public NowPlayingCard(Activity a, FrameLayout root) {
        this.act = a;
        this.d = a.getResources().getDisplayMetrics().density;
        build(root);
    }

    private ImageView makeBtn(int icon, Runnable r) {
        ImageView b = new ImageView(act);
        b.setImageResource(icon);
        b.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        int ip = (int) (8 * d);
        b.setPadding(ip, ip, ip, ip);
        b.setColorFilter(0xCCFFFFFF);
        b.setFocusable(true);
        b.setClickable(true);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(18 * d);
        bg.setColor(0x1AFFFFFF);
        b.setBackground(bg);
        b.setOnFocusChangeListener((v, f) -> {
            bg.setColor(f ? 0xFFFFFFFF : 0x1AFFFFFF);
            b.setColorFilter(f ? 0xFF000000 : 0xCCFFFFFF);
            v.animate().scaleX(f ? 1.12f : 1f).scaleY(f ? 1.12f : 1f).setDuration(140).start();
        });
        b.setOnClickListener(v -> r.run());
        return b;
    }

    private void build(FrameLayout root) {
        card = new LinearLayout(act);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        int p = (int) (7 * d);
        card.setPadding(p, p, (int) (14 * d), p);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(16 * d);
        bg.setColor(0x5518181C);
        bg.setStroke((int) (1 * d), 0x33FFFFFF);
        card.setBackground(bg);

        // Art and titles form one box: Left from Prev selects it, OK opens the playing app.
        LinearLayout info = new LinearLayout(act);
        info.setOrientation(LinearLayout.HORIZONTAL);
        info.setGravity(Gravity.CENTER_VERTICAL);
        info.setId(View.generateViewId());
        info.setNextFocusLeftId(info.getId());
        info.setFocusable(true);
        info.setClickable(true);
        GradientDrawable ibg = new GradientDrawable();
        ibg.setCornerRadius(12 * d);
        ibg.setColor(0x00FFFFFF);
        info.setBackground(ibg);
        info.setOnFocusChangeListener((v, f) -> {
            ibg.setColor(f ? 0x33FFFFFF : 0x00FFFFFF);
            ibg.setStroke(f ? (int) (2 * d) : 0, 0xCCFFFFFF);
        });
        info.setOnClickListener(v -> openPlayer());
        card.addView(info, new LinearLayout.LayoutParams(-2, -2));

        art = new ImageView(act);
        art.setScaleType(ImageView.ScaleType.CENTER_CROP);
        art.setClipToOutline(true);
        GradientDrawable abg = new GradientDrawable();
        abg.setCornerRadius(10 * d);
        art.setBackground(abg);
        info.addView(art, new LinearLayout.LayoutParams((int) (42 * d), (int) (42 * d)));

        LinearLayout meta = new LinearLayout(act);
        meta.setOrientation(LinearLayout.VERTICAL);
        meta.setPadding((int) (12 * d), 0, (int) (12 * d), 0);
        title = new TextView(act);
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(14f);
        title.setSingleLine(true);
        title.setEllipsize(TextUtils.TruncateAt.END);
        artist = new TextView(act);
        artist.setTextColor(0xAAFFFFFF);
        artist.setTextSize(11.5f);
        artist.setSingleLine(true);
        artist.setEllipsize(TextUtils.TruncateAt.END);
        meta.addView(title);
        meta.addView(artist);
        info.addView(meta, new LinearLayout.LayoutParams((int) (155 * d), -2));

        int bs = (int) (34 * d);
        int bm = (int) (4 * d);
        ImageView bPrev = makeBtn(R.drawable.ic_np_prev, () -> {
            if (activeCtrl != null) activeCtrl.getTransportControls().skipToPrevious();
        });
        btnPlay = makeBtn(R.drawable.ic_np_pause, () -> {
            if (activeCtrl == null) return;
            PlaybackState ps = activeCtrl.getPlaybackState();
            if (ps != null && ps.getState() == PlaybackState.STATE_PLAYING) activeCtrl.getTransportControls().pause();
            else activeCtrl.getTransportControls().play();
            card.postDelayed(this::refresh, 300);
        });
        ImageView bNext = makeBtn(R.drawable.ic_np_next, () -> {
            if (activeCtrl != null) activeCtrl.getTransportControls().skipToNext();
        });
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(bs, bs);
        lp1.setMargins(bm, 0, bm, 0);
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(bs, bs);
        lp2.setMargins(bm, 0, bm, 0);
        LinearLayout.LayoutParams lp3 = new LinearLayout.LayoutParams(bs, bs);
        lp3.setMargins(bm, 0, 0, 0);
        card.addView(bPrev, lp1);
        card.addView(btnPlay, lp2);
        card.addView(bNext, lp3);

        FrameLayout.LayoutParams clp = new FrameLayout.LayoutParams(-2, -2, Gravity.TOP | Gravity.START);
        clp.setMarginStart((int) (58 * d));
        clp.topMargin = (int) (110 * d);
        card.setVisibility(View.GONE);
        root.addView(card, clp);
    }

    public void refresh() {
        try {
            MediaSessionManager msm = (MediaSessionManager) act.getSystemService(Context.MEDIA_SESSION_SERVICE);
            List<MediaController> cs = msm.getActiveSessions(new ComponentName(act, MediaNoteService.class));
            if (cs != null) for (MediaController c : cs) {
                MediaMetadata md = c.getMetadata();
                PlaybackState ps = c.getPlaybackState();
                if (md == null) continue;
                CharSequence t = md.getText(MediaMetadata.METADATA_KEY_TITLE);
                if (TextUtils.isEmpty(t)) continue;
                follow(c);
                title.setText(t);
                CharSequence ar = md.getText(MediaMetadata.METADATA_KEY_ARTIST);
                artist.setText(!TextUtils.isEmpty(ar) ? ar : "Now Playing");
                Bitmap bmp = md.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);
                if (bmp == null) bmp = md.getBitmap(MediaMetadata.METADATA_KEY_ART);
                if (bmp != null) art.setImageBitmap(bmp);
                else art.setImageResource(R.drawable.ic_wifi);
                boolean pl = ps != null && ps.getState() == PlaybackState.STATE_PLAYING;
                btnPlay.setImageResource(pl ? R.drawable.ic_np_pause : R.drawable.ic_np_play);
                card.setVisibility(View.VISIBLE);
                return;
            }
        } catch (Exception ignored) {
            // No listener access yet (SecurityException) or no session service: keep the card hidden.
        }
        follow(null);
        card.setVisibility(View.GONE);
    }

    /** The playing app's own player screen when its session offers one, otherwise the app's launch screen. */
    private void openPlayer() {
        if (activeCtrl == null) return;
        PendingIntent player = activeCtrl.getSessionActivity();
        if (player != null) {
            try {
                player.send();
                return;
            } catch (PendingIntent.CanceledException ignored) {
                // Stale link: fall back to launching the app.
            }
        }
        String pkg = activeCtrl.getPackageName();
        PackageManager pm = act.getPackageManager();
        Intent i = pm.getLeanbackLaunchIntentForPackage(pkg);
        if (i == null) i = pm.getLaunchIntentForPackage(pkg);
        if (i == null) return;
        try {
            act.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (ActivityNotFoundException ignored) {
            // The app went away between the session and the click.
        }
    }

    /** Listen to this session's changes instead of the previous one's; the same session keeps its callback. */
    private void follow(MediaController c) {
        if (activeCtrl != null && c != null && activeCtrl.getSessionToken().equals(c.getSessionToken())) return;
        if (activeCtrl != null) activeCtrl.unregisterCallback(onChange);
        activeCtrl = c;
        if (c != null) c.registerCallback(onChange);
    }

    /** Stop listening (the activity is going away). */
    public void release() {
        follow(null);
    }
}
