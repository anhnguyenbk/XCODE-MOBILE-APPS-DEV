package com.xcode.mobile.smilealarm.tunemanager;

import java.io.Serializable;
import java.util.Arrays;
import java.util.UUID;

@SuppressWarnings("serial")
public class Tune implements Serializable {

    private UUID _keyId; // for save to database and get from AlarmPoint
    private String _name;
    private boolean _isRecommend;
    private int _resId;
    private String _path;

    public Tune(String name, int resId) {
        this._name = getDefaultNiceFileName(name);
        this._resId = resId;
        this._isRecommend = true;
        this._keyId = UUID.randomUUID();
    }

    public Tune(String name, int resId, UUID keyId) {
        this._name = getDefaultNiceFileName(name);
        this._resId = resId;
        this._isRecommend = true;
        this._keyId = keyId;
    }

    public Tune(String name, String path) {
        this._name = getDefaultNiceFileName(name);
        this._path = path;
        this._isRecommend = false;
        this._keyId = UUID.randomUUID();
    }

    public Tune(String name, String path, UUID keyId) {
        this._name = getDefaultNiceFileName(name);
        this._path = path;
        this._isRecommend = false;
        this._keyId = keyId;
    }

    public UUID get_keyId() {
        return UUID.fromString(_keyId.toString());
    }

    public String get_name() {
        return _name;
    }

    public boolean isRecommend() {
        return _isRecommend;
    }

    public int get_resId() {
        return _resId;
    }

    public String get_path() {
        return _path;
    }

    private String getDefaultNiceFileName(String filename) {
        String result = "";

        //remove extension
        int i = filename.lastIndexOf(".");
        if (i != -1)
            result = filename.substring(0, i);
        else result = filename;

        // Separate Text
        if (!result.contains(" ")) {
            //remove _
            result = result.replaceAll("_", " ");

            //remove +
            result = result.replaceAll("\\+", " ");

            //remove -
            result = result.replaceAll("-", " ");

            //split Upcase words
            result = Arrays.toString(result.split("(?=\\p{Upper})")).replace(", ", " ");
            result = result.substring(2, result.length() - 1); // remove [ ]
        }

        // Trim
        result = result.trim();

        return result;
    }

}
