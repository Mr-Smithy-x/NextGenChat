package co.yoprice.nextgenchat;

import android.app.ActionBar;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import co.yoprice.nextgenchat.R;
import co.yoprice.nextgenchat.data.models.Message;
import co.yoprice.nextgenchat.data.models.PasteInfo;
import co.yoprice.nextgenchat.utils.NGUClient;
import de.hdodenhof.circleimageview.CircleImageView;

public class PasteActivity extends AppCompatActivity {

    public static final String PASTE_INFO = "PASTE";
    private PasteInfo pasteInfo;
    private FloatingActionButton fab;
    private Toolbar toolbar;
    public static final String MESSAGE_INFO = "MESSAGE";
    private Message messageInfo;
    private Palette.Builder palette;
    private CollapsingToolbarLayout coordinatorLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paste);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getIntent() != null) {
            if (getIntent().hasExtra(PASTE_INFO))
                this.pasteInfo = (PasteInfo) getIntent().getSerializableExtra(PASTE_INFO);
            if (getIntent().hasExtra(MESSAGE_INFO))
                this.messageInfo = (Message) getIntent().getSerializableExtra(MESSAGE_INFO);
            init();
        } else finish();
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void init() {
        coordinatorLayout = (CollapsingToolbarLayout) findViewById(R.id.paste_page_coord);
        CircleImageView ngu = (CircleImageView) findViewById(R.id.ngu_user_icon);
        AppCompatTextView title = (AppCompatTextView) findViewById(R.id.ngu_user_name);
        AppCompatTextView date = (AppCompatTextView) findViewById(R.id.ngu_user_date);
        AppCompatTextView message = (AppCompatTextView) findViewById(R.id.ngu_user_message);
        AppCompatTextView link = (AppCompatTextView) findViewById(R.id.ngu_paste_link);
        AppCompatTextView content = (AppCompatTextView) findViewById(R.id.ngu_paste_bin_content);
        Picasso.with(this).load(NGUClient.getInstance().generateUserIcon(messageInfo.getUserId())).error(R.mipmap.ngu).into(ngu);
        Picasso.with(this).load(NGUClient.getInstance().generateUserIcon(messageInfo.getUserId())).error(R.mipmap.ngu).into((ImageView) findViewById(R.id.paste_page_header), new Callback() {
            @Override
            public void onSuccess() {
                setPalette(Palette.from(((BitmapDrawable) ((ImageView) findViewById(R.id.paste_page_header)).getDrawable()).getBitmap()));

            }

            @Override
            public void onError() {

            }
        });

        title.setText(messageInfo.getUser());
        date.setText(messageInfo.getTime());
        message.setText(messageInfo.getMessage());
        link.setText(pasteInfo.getLink());
        content.setText(pasteInfo.getContent());
    }

    public void setPalette(Palette.Builder palette) {
        palette.generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                fab.setRippleColor(palette.getVibrantColor(ContextCompat.getColor(PasteActivity.this,R.color.colorAccent)));
                coordinatorLayout.setStatusBarScrimColor(palette.getMutedColor(ContextCompat.getColor(PasteActivity.this,R.color.colorPrimary)));
                coordinatorLayout.setContentScrimColor(palette.getMutedColor(ContextCompat.getColor(PasteActivity.this,R.color.colorPrimary)));

            }
        });
    }
}
