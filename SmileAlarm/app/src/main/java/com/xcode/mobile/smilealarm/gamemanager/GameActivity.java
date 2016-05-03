package com.xcode.mobile.smilealarm.gamemanager;

import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;
import java.util.UUID;

import com.xcode.mobile.smilealarm.ActivityConstant;
import com.xcode.mobile.smilealarm.R;
import com.xcode.mobile.smilealarm.listmanager.ViewToDoListActivity;
import com.xcode.mobile.smilealarm.tunemanager.RecommendedTunesHandler;
import com.xcode.mobile.smilealarm.tunemanager.Tune;

/**
 * Created by an.nguyen on 11/17/2015.
 */
public class GameActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private UUID _tuneId;
    private Boolean _isFadeIn;
    private TextView countTV;
    private TextView mathTV;
    private TextView resultTV;
    private int _returnCode;
    private int _currentBackgroundColor;
    private GameHandler currentGame;
    private RelativeLayout bgGame;
    private final int[] color = {R.color.orange, R.color.purple, R.color.green};

    protected void onCreate(Bundle saveInstancedState) {
        super.onCreate(saveInstancedState);
        setContentView(R.layout.activity_game);

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
                    mediaPlayer = MediaPlayer.create(this, tune.get_resId());
                } else {
                    Uri uri = Uri.parse(tune.get_path());
                    mediaPlayer = MediaPlayer.create(this, uri);
                }
                mediaPlayer.start();
                mediaPlayer.setLooping(true);
            }

            if (_isFadeIn) {
            }
        }

        initGameWorld();
    }

    private void initGameWorld() {
        _returnCode = GameConstant.CORRECT_CODE;

        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/HandmadeTypewriter.ttf");
        currentGame = GameHandler.getInstance();
        countTV = (TextView) findViewById(R.id.countGame);
        countTV.setText(String.valueOf(currentGame.get_count()));
        countTV.setTypeface(face);
        mathTV = (TextView) findViewById(R.id.math);
        mathTV.setText(currentGame.get_currentMath().get_factor_1()
                + (currentGame.get_currentMath().isAddOperation() ? " + " : " - ")
                + currentGame.get_currentMath().get_factor_2());
        mathTV.setTypeface(face);
        resultTV = (TextView) findViewById(R.id.result);
        resultTV.setText("=" + currentGame.get_currentMath().get_fakeResult());
        resultTV.setTypeface(face);

        ImageButton correctBtn = (ImageButton) findViewById(R.id.btnCorrect);
        correctBtn.setBackgroundResource(R.mipmap.ic_yes);
        correctBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                _returnCode = currentGame.processInput(true);
                updateGameWorld();
            }

        });
        ImageButton wrongBtn = (ImageButton) findViewById(R.id.btnWrong);
        wrongBtn.setBackgroundResource(R.mipmap.ic_no);
        wrongBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                _returnCode = currentGame.processInput(false);
                updateGameWorld();
            }
        });

        _currentBackgroundColor = 1; // purple
        bgGame = (RelativeLayout) findViewById(R.id.bgGame);
        bgGame.setBackgroundResource(color[(_currentBackgroundColor) % color.length]);
    }

    private void updateGameWorld() {

        if (_returnCode == GameConstant.WIN_GAME_CODE) {
            winGame();
        } else {
            if (_returnCode == GameConstant.CORRECT_CODE) {
                Random rd = new Random();
                int newBackgroundColor = _currentBackgroundColor;
                while (newBackgroundColor == _currentBackgroundColor)
                    newBackgroundColor = rd.nextInt(3) + 1;
                _currentBackgroundColor = newBackgroundColor;
                
                bgGame.setBackgroundResource(color[(_currentBackgroundColor) % color.length]);
            }

            countTV.setText(String.valueOf(currentGame.get_count()));
            mathTV.setText(currentGame.get_currentMath().get_factor_1()
                    + (currentGame.get_currentMath().isAddOperation() ? " + " : " - ")
                    + currentGame.get_currentMath().get_factor_2());
            resultTV.setText("=" + currentGame.get_currentMath().get_fakeResult());
        }
    }

    private void winGame() {
        FinishActivity();
        Intent todoListActivity = new Intent(this, ViewToDoListActivity.class);
        startActivity(todoListActivity);
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "You cannot stop it. Try more, guy. Never give up!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (_returnCode != GameConstant.WIN_GAME_CODE) {
            FinishActivity();
            Intent i = new Intent();
            i.setClassName("ttcnpm2015.team23.risingearly", "ttcnpm2015.team23.risingearly.gamemanager.GameActivity");
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setAction(ActivityConstant.ACTION_START_GAME);
            i.putExtra(ActivityConstant.VALUE_NAME_AP_TUNE_ID, _tuneId);
            i.putExtra(ActivityConstant.VALUE_NAME_AP_TUNE_ATTR, _isFadeIn);
            i.putExtra(ActivityConstant.ACTION_RESTART, true);

            startActivity(i);
        }
    }

    private void FinishActivity() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            // after release, we cannot test isPlaying
            mediaPlayer = null;
        }
        finish();
    }
}
