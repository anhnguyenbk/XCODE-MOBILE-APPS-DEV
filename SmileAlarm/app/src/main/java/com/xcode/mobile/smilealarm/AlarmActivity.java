package com.xcode.mobile.smilealarm;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.xcode.mobile.smilealarm.smiledetectmanager.FaceTrackerActivity;
import com.xcode.mobile.smilealarm.tunemanager.RecommendedTunesHandler;
import com.xcode.mobile.smilealarm.tunemanager.Tune;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by annguyen on 4/29/16.
 */
public class AlarmActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private Intent mediaServiceIntent;
    private UUID _tuneId;
    private Boolean _isFadeIn;
    private Button btnTurnOff;

    protected void onCreate(Bundle saveInstancedState) {
        super.onCreate(saveInstancedState);
        setContentView(R.layout.activity_ring_off);

        btnTurnOff = (Button) findViewById(R.id.btnTurnOff);

        // Setup Ringtone
        Intent _keyValue = getIntent();
        if (_keyValue.getAction().equals(ActivityConstant.ACTION_START_GAME)) {

            if (_keyValue.getBooleanExtra(ActivityConstant.ACTION_RESTART, false))
                Toast.makeText(this, "You cannot stop it. Try more, guy. Never give up!", Toast.LENGTH_LONG).show();

            _tuneId = (UUID) _keyValue.getExtras().getSerializable(ActivityConstant.VALUE_NAME_AP_TUNE_ID);
            _isFadeIn = _keyValue.getBooleanExtra(ActivityConstant.VALUE_NAME_AP_TUNE_ATTR, true);

            Tune tune = null;
            if (_tuneId != null)
                tune = RecommendedTunesHandler.getInstance().getTuneFromRecommendList(_tuneId);

            if (tune != null) {
                if (tune.isRecommend()) {
                    MediaPlayerBackground.SoundPlayer(this, tune.get_resId());
//                    mediaPlayer = MediaPlayer.create(this, tune.get_resId());
//                    mediaServiceIntent = new Intent(this, MediaService.class);
               } else {
                    Uri uri = Uri.parse(tune.get_path());
                    MediaPlayerBackground.SoundPlayer(this, uri);
//                    mediaPlayer = MediaPlayer.create(this, uri);
//                    mediaServiceIntent = new Intent(this, MediaService.class);
                }
//                mediaPlayer.start();
//                mediaPlayer.setLooping(true);
//                startService(mediaServiceIntent);
            }

            if (_isFadeIn) {
            }
        }

        btnTurnOff.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
//                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
//                    mediaPlayer.stop();
//                    mediaPlayer.release();
//                    // after release, we cannot test isPlaying
//                    mediaPlayer = null;
//                }
                Intent i = new Intent();
                i.setClassName("com.xcode.mobile.smilealarm", "com.xcode.mobile.smilealarm.smiledetectmanager.FaceTrackerActivity");
//                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                i.putExtra(ActivityConstant.VALUE_NAME_AP_TUNE_ID, _tuneId);
//                i.putExtra(ActivityConstant.VALUE_NAME_AP_TUNE_ATTR, _isFadeIn);
                AlarmActivity.this.startActivity(i);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "You cannot stop it. Try more, guy. Never give up!", Toast.LENGTH_SHORT).show();
    }

}
