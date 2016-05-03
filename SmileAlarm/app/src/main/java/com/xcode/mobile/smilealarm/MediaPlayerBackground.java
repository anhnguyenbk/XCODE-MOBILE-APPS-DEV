package com.xcode.mobile.smilealarm;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

/**
 * Created by annguyen on 5/3/16.
 */
public class MediaPlayerBackground {
    public static MediaPlayer player;

    public static void SoundPlayer(Context ctx, int raw_id){
        player = MediaPlayer.create(ctx, raw_id);
        player.setLooping(true); // Set looping

        //player.release();
        player.start();
    }

    public static void SoundPlayer(Context ctx, Uri uri){
        player = MediaPlayer.create(ctx, uri);
        player.setLooping(true); // Set looping

        //player.release();
        player.start();
    }
}
