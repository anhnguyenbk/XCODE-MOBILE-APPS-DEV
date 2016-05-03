package com.xcode.mobile.smilealarm.tunemanager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.xcode.mobile.smilealarm.DataHelper;
import com.xcode.mobile.smilealarm.R;

public class RecommendedTunesHandler {
    public static final String UUID_01 = "878c61c7-e9f1-4931-8824-fb69e3c3344b";
    public static final String UUID_02 = "046b6c7f-0b8a-43b9-b35d-6489e6daee91";
    public static final String UUID_03 = "38400000-8cf0-11bd-b23e-10b96e4ef00d";
    private static final int NUMBER_OF_RAW_FILES = 3;
    private static final RecommendedTunesHandler _instance = new RecommendedTunesHandler();
    private List<Tune> _recommendTunes;
    private UUID _defaultTuneId;
    private Tune _defaultTune;

    private RecommendedTunesHandler() {
        _recommendTunes = new ArrayList<Tune>();
        _recommendTunes.add(new Tune("Amazing Morning", R.raw.t1, UUID.fromString(UUID_01)));
        _recommendTunes.add(new Tune("Cool Alarm Buzz", R.raw.t2, UUID.fromString(UUID_02)));
        _recommendTunes.add(new Tune("Awesome day", R.raw.t3, UUID.fromString(UUID_03)));

        List<Tune> userTunes = DataHelper.getInstance().getUserTunesAndDefaultKey();
        set_defaultTune(userTunes.get(0).get_keyId());
        _recommendTunes.addAll(userTunes.subList(1, userTunes.size()));

    }

    public static RecommendedTunesHandler getInstance() {
        return _instance;
    }

    public Tune get_defaultTune() {
        return _defaultTune;
    }

    public void set_defaultTune(UUID _defaultTuneId) {
        if (_defaultTuneId != null) {
            this._defaultTuneId = _defaultTuneId;
            _defaultTune = getTuneFromRecommendList(_defaultTuneId);
            DataHelper.getInstance().changeDefault(_defaultTuneId);
        }
    }

    public UUID get_defaultTuneId() {
        return _defaultTuneId;
    }

    public List<Tune> get_recommendTunes() {
        return _recommendTunes;
    }

    public void addTuneToRecommendList(Tune tune) {
        if (tune != null) {
            _recommendTunes.add(tune);
            DataHelper.getInstance().addTuneToData(tune);
        }
    }

    public void removeTuneToRecommendList(int position) {
        Tune tune = _recommendTunes.get(position);
        // SHOULDN'T REMOVE RAWS
        if (tune.isRecommend())
            return;

        if (tune.get_keyId().compareTo(get_defaultTuneId()) == 0) {
            // change Default
            // because there are always some raws => don't care about out of size
            if (position == 0)
                set_defaultTune(_recommendTunes.get(1).get_keyId());
            else
                set_defaultTune(_recommendTunes.get(0).get_keyId());
        }

        _recommendTunes.remove(position);
        DataHelper.getInstance().removeTuneFromData(position - NUMBER_OF_RAW_FILES);
    }

    public Tune getTuneFromRecommendList(UUID tuneId) {
        for (Tune tune : _recommendTunes) {
            if (tune.get_keyId().compareTo(tuneId) == 0)
                return tune;
        }
        return null;
    }

}
