package com.xcode.mobile.smilealarm.tunemanager;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import com.xcode.mobile.smilealarm.R;

public class RecommendedTunesAdapter extends BaseAdapter {

    private List<Tune> _tunes;
    private Context _context;
    private MediaPlayer _mediaPlayer;
    private int _currentPositionPlaying;
    private Button _currentButtonPlaying;

    public RecommendedTunesAdapter(List<Tune> _tunes, Context _context) {
        super();
        this._tunes = _tunes;
        this._context = _context;
        _currentButtonPlaying = null;
        _currentPositionPlaying = -1;
    }

    public int getCount() {
        return _tunes.size();
    }

    public Object getItem(int arg0) {
        return _tunes.get(arg0);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;
        LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.tune_item_layout, parent, false);
            holder = new ViewHolder();

            holder.nameView = (TextView) convertView.findViewById(R.id.tune_name);
            holder.playBtn = (Button) convertView.findViewById(R.id.tune_play_btn);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Tune tune = _tunes.get(position);
        if (RecommendedTunesHandler.getInstance().get_defaultTuneId().equals(tune.get_keyId())) {
            holder.nameView.setTextColor(Color.GREEN);
        } else {
            holder.nameView.setTextColor(Color.BLACK);
        }

        holder.nameView.setText(tune.get_name());

        if (_mediaPlayer != null && _currentPositionPlaying == position) {
            holder.playBtn.setText(R.string.stop_tune);
        } else {
            holder.playBtn.setText(R.string.play_tune);
        }

        holder.playBtn.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {

                Tune tune = (Tune) getItem(position);
                Boolean isStopped = false;

                if (_mediaPlayer != null && _mediaPlayer.isPlaying()) {
                    _mediaPlayer.stop();
                    _mediaPlayer.release();
                    // after release, we cannot test isPlaying
                    _mediaPlayer = null;
                    isStopped = true;
                    ((Button) v).setText(R.string.play_tune);
                }

                // For toggling between play and stop audio with the change of text
                if (_currentPositionPlaying != position || !isStopped) {
                    if (tune == null)
                        return;

                    if (tune.isRecommend()) {
                        _mediaPlayer = MediaPlayer.create(_context, tune.get_resId());
                        _mediaPlayer.start();
                    } else {
                        Uri uri = Uri.parse(tune.get_path());
                        _mediaPlayer = MediaPlayer.create(_context, uri);
                        _mediaPlayer.start();
                    }

                    // After playing completely
                    _mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

                        public void onCompletion(MediaPlayer arg0) {
                            _currentButtonPlaying.setText(R.string.play_tune);
                            _currentButtonPlaying = null;
                            _currentPositionPlaying = -1;

                        }
                    });

                    if (_currentButtonPlaying != null && _currentPositionPlaying != position) {
                        _currentButtonPlaying.setText(R.string.play_tune);
                    }
                    _currentPositionPlaying = position;
                    _currentButtonPlaying = ((Button) v);
                    ((Button) v).setText(R.string.stop_tune);
                }

            }
        });
        return convertView;
    }

    public void stopPlayingMusic() {
        if (_mediaPlayer != null && _mediaPlayer.isPlaying()) {
            _mediaPlayer.stop();
            _mediaPlayer.release();
            _mediaPlayer = null;
            _currentButtonPlaying = null;
            _currentPositionPlaying = -1;
        }
    }

    // For more performances
    static class ViewHolder {
        TextView nameView;
        Button playBtn;
    }

}
