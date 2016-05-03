package com.xcode.mobile.smilealarm.tunemanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.xcode.mobile.smilealarm.ActivityConstant;
import com.xcode.mobile.smilealarm.R;
import com.xcode.mobile.smilealarm.ViewHelper;

public class RecommendedTunesActivity extends AppCompatActivity {
    private ListView tuneList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommended_tunes);
        ViewHelper.setupToolbar(this);

        RecommendedTunesAdapter adapter = new RecommendedTunesAdapter(
                RecommendedTunesHandler.getInstance().get_recommendTunes(), this);

        tuneList = (ListView) findViewById(R.id.list);
        tuneList.setAdapter(adapter);
        Intent i = getIntent();
        Boolean _isModifyMode = i.getAction() != null && i.getAction().compareTo(ActivityConstant.ACTION_MODIFY_TUNES) == 0;

        if (_isModifyMode) {
            // Change Default Tune of Recommended List
            tuneList.setOnItemLongClickListener(new OnItemLongClickListener() {
                public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
                    Tune newDefaultTune = (Tune) parent.getItemAtPosition(position);
                    RecommendedTunesHandler.getInstance().set_defaultTune(newDefaultTune.get_keyId());
                    ((RecommendedTunesAdapter) tuneList.getAdapter()).notifyDataSetChanged();
                    Toast.makeText(getApplicationContext(), "Default Tune is changed", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        } else {
            // Choose Tune for Alarm Point
            tuneList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent();
                    intent.putExtra(ActivityConstant.VALUE_NAME_APINFO_RECOMLIST, position);
                    RecommendedTunesActivity.this.setResult(RESULT_OK, intent);

                    ((RecommendedTunesAdapter) tuneList.getAdapter()).stopPlayingMusic();

                    finish();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ((RecommendedTunesAdapter) tuneList.getAdapter()).stopPlayingMusic();
    }

}
