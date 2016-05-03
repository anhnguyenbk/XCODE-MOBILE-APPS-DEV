package com.xcode.mobile.smilealarm.listmanager;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.UUID;

import com.xcode.mobile.smilealarm.ActivityConstant;
import com.xcode.mobile.smilealarm.R;
import com.xcode.mobile.smilealarm.ViewHelper;
import com.xcode.mobile.smilealarm.tunemanager.RecommendedTunesHandler;
import com.xcode.mobile.smilealarm.tunemanager.Tune;

/**
 * Created by an.nguyen on 11/17/2015.
 */
public class ViewVLTCheckListActivity extends AppCompatActivity implements CheckListAdapter.OnAllItemsCheckedListener {
    private ArrayList<String> vltList;
    private Button turnOffAlarmBtn;
    private MediaPlayer mediaPlayer;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vlt_checklist);
        ViewHelper.setupToolbar(this);

        // Setup RingTone
        Intent i = getIntent();
        if (i.getAction().compareTo(ActivityConstant.ACTION_START_VLT) == 0) {
            UUID tuneId = (UUID) i.getExtras().getSerializable(ActivityConstant.VALUE_NAME_AP_TUNE_ID);
            Boolean isFadeIn = i.getBooleanExtra(ActivityConstant.VALUE_NAME_AP_TUNE_ATTR, true);

            Tune tune = null;
            if (tuneId != null)
                tune = RecommendedTunesHandler.getInstance().getTuneFromRecommendList(tuneId);

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

            if (isFadeIn) {
            }

            turnOffAlarmBtn = (Button) findViewById(R.id.btnTurnoffAlarm);
            turnOffAlarmBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    turnOffRingtone();
                    turnOffAlarmBtn.setVisibility(View.GONE);
                }
            });
        }


        // Setup Create TodoList Button
        Button createToDoListBtn = (Button) findViewById(R.id.btnCreateTodoList);
        createToDoListBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent createToDoListActivity = new Intent(ViewVLTCheckListActivity.this, CreateToDoListActivity.class);
                ViewVLTCheckListActivity.this.startActivity(createToDoListActivity);
            }
        });

        // Setup Very Last Thing CheckList
        ListView vltLV = (ListView) findViewById(R.id.checklist);
        setupAdviceList();

        CheckListAdapter chkLstAdpt = new CheckListAdapter(this, R.layout.check_list, vltList);
        vltLV.setAdapter(chkLstAdpt);
    }

    private void setupAdviceList() {
        vltList = new ArrayList<>();
        vltList.add("Turn off the laptop");
        vltList.add("Turn of wifi");
        vltList.add("Drink some milk");
        vltList.add("Brush your teeth, wash your face");
    }

    public void onBackPressed() {
        FinishActivity();
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        FinishActivity();
    }

    @Override
    public void onAllItemsChecked() {
        FinishActivity();
    }

    private void turnOffRingtone() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            // after release, we cannot test isPlaying
            mediaPlayer = null;
        }
    }

    private void FinishActivity() {
        turnOffRingtone();
        finish();
    }
}
