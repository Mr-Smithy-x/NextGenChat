package co.yoprice.nextgenchat;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.squareup.picasso.Picasso;

import co.yoprice.nextgenchat.singleton.NotifSingleton;
import co.yoprice.nextgenchat.data.models.StreamableVideo;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;

public class StreamableAcitivity extends AppCompatActivity {

    private JCVideoPlayer jcVideoPlayerStandard;
    private FloatingActionButton fab;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streamable_acitivity);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        NotifSingleton.getInstance().setOnMediaActivityOpened(true);
        jcVideoPlayerStandard = (JCVideoPlayer) findViewById(R.id.custom_videoplayer_standard);
        try {
            if (getIntent() != null) {
                if (getIntent().hasExtra("video")) {
                    StreamableVideo streamableVideo = (StreamableVideo) getIntent().getSerializableExtra("video");
                    for (StreamableVideo.VIDEO_TYPE video_type : StreamableVideo.VIDEO_TYPE.values()) {
                        if (streamableVideo.hasVideoType(video_type)) {
                            Log.e(StreamableAcitivity.class.getSimpleName(), streamableVideo.getVideo(video_type));
                            ((AppCompatTextView) findViewById(R.id.video_message)).setText(streamableVideo.getMessage());
                            ((AppCompatTextView) findViewById(R.id.video_title)).setText(streamableVideo.getTitle());
                            getSupportActionBar().setTitle(streamableVideo.getTitle());
                            if(streamableVideo.getJpg() != null)
                            Picasso.with(this).load(streamableVideo.getJpg()).into(jcVideoPlayerStandard.ivThumb);
                            jcVideoPlayerStandard.setUp(streamableVideo.getVideo(video_type), streamableVideo.getTitle());
                            break;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            finish();
        }
        NotifSingleton.getInstance().setOnFront(true);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onBackPressed() {
        closeVideo();
        super.onBackPressed();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                closeVideo();
                NavUtils.navigateUpFromSameTask(this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void closeVideo() {
        try {
            jcVideoPlayerStandard.setState(JCVideoPlayer.CURRENT_STATE_PAUSE);
            JCVideoPlayer.releaseAllVideos();
        }catch (Throwable t){
            t.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        NotifSingleton.getInstance().setOnMediaActivityOpened(false);
        closeVideo();
        super.onDestroy();
    }
}
