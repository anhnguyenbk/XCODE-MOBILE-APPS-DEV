package com.xcode.mobile.smilealarm.listmanager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import java.util.ArrayList;

import com.xcode.mobile.smilealarm.R;
import com.xcode.mobile.smilealarm.ViewHelper;

/**
 * Created by an.nguyen on 11/17/2015.
 */
public class ViewVFTCheckListActivity extends AppCompatActivity implements CheckListAdapter.OnAllItemsCheckedListener {
    private ArrayList<String> vftList;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vft_checklist);
        ViewHelper.setupToolbar(this);

        ListView vftLV = (ListView) findViewById(R.id.checklist);
        setupAdviceList();

        CheckListAdapter chkLstAdpt = new CheckListAdapter(this, R.layout.check_list, vftList);
        vftLV.setAdapter(chkLstAdpt);
    }

    private void setupAdviceList() {
        vftList = new ArrayList<String>();
        vftList.add("Wash your face");
        vftList.add("Brush your teeth");
        vftList.add("Drink some water");
        vftList.add("Do some exercises");
    }

    @Override
    public void onAllItemsChecked() {
        finish();
    }
}
