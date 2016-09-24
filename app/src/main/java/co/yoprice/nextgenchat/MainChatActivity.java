package co.yoprice.nextgenchat;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import cafe.adriel.androidstreamable.callback.NewVideoCallback;
import cafe.adriel.androidstreamable.model.NewVideo;
import co.yoprice.nextgenchat.data.callbacks.FileCallback;
import co.yoprice.nextgenchat.data.callbacks.NGUImgurCallback;
import co.yoprice.nextgenchat.data.services.MediaService;
import co.yoprice.nextgenchat.ui.custom.MessageBox;
import co.yoprice.nextgenchat.ui.fragments.ChatFragment;
import co.yoprice.nextgenchat.helpers.DocumentHelper;
import co.yoprice.nextgenchat.helpers.IntentHelper;
import co.yoprice.nextgenchat.data.imgurmodel.ImageResponse;
import co.yoprice.nextgenchat.data.models.Message;
import co.yoprice.nextgenchat.singleton.NotifSingleton;
import co.yoprice.nextgenchat.utils.NGUClient;
import co.yoprice.nextgenchat.utils.aLog;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Response;

public class MainChatActivity extends AppCompatActivity implements NGUClient.NGUCallBackListener, NavigationView.OnNavigationItemSelectedListener, NGUImgurCallback {

    private DrawerLayout mDrawerLayout;
    private WebView webView;
    private Toolbar mToolbar;
    private ActionBarDrawerToggle mToggle;
    private NavigationView mNav;


    public final static String TAG = MainChatActivity.class.getSimpleName();
    private File chosenFile; //chosen file from intent
    private NewVideoCallback streamableUpload = new NewVideoCallback() {
        @Override
        public void onSuccess(int i, NewVideo newVideo) {
            ((ChatFragment)getSupportFragmentManager().findFragmentById(R.id.chat_fragment)).setTextBox("https://streamable.com/"+newVideo.getShortCode());
            co.yoprice.nextgenchat.ui.custom.Snackbar.make(getWindow().getDecorView(),"Video Uploaded!", co.yoprice.nextgenchat.ui.custom.Snackbar.LENGTH_SHORT, ContextCompat.getColor(MainChatActivity.this, R.color.md_green_700), ContextCompat.getColor(MainChatActivity.this,R.color.md_white_1000)).show();
        }

        @Override
        public void onFailure(int i, Throwable throwable) {
            co.yoprice.nextgenchat.ui.custom.Snackbar.make(getWindow().getDecorView(),"Failed To Upload Video", co.yoprice.nextgenchat.ui.custom.Snackbar.LENGTH_INDEFINITE, ContextCompat.getColor(MainChatActivity.this, R.color.md_red_700), ContextCompat.getColor(MainChatActivity.this,R.color.md_white_1000)).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_chat);
        setSupportActionBar(mToolbar = (Toolbar) findViewById(R.id.main_toolbar));
        mDrawerLayout = (DrawerLayout) findViewById(R.id.mDrawerLayout);
        mNav = (NavigationView) findViewById(R.id.main_navigation);
        mNav.setNavigationItemSelectedListener(this);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.app_name, R.string.app_name);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        webView = (WebView) findViewById(R.id.webView);
        NGUClient.initialize(webView);
        NGUClient.getInstance().addListener(MainChatActivity.this);
        NGUClient.getInstance().startClient(new Handler());
    }

    @Override
    public void OnShouted(String text) {
        aLog.i("Text Sent", text);
        if (getSupportFragmentManager().findFragmentById(R.id.chat_fragment) != null) {
            ((ChatFragment) getSupportFragmentManager().findFragmentById(R.id.chat_fragment)).clearTxtBox();
        }
    }

    @Override
    public void OnLoggedIn() {
        if (getSupportFragmentManager().findFragmentById(R.id.chat_fragment) != null) {
            getSupportFragmentManager().beginTransaction().show(getSupportFragmentManager().findFragmentById(R.id.chat_fragment)).commitAllowingStateLoss();
            findViewById(R.id.webViewHolder).setVisibility(View.GONE);
        } else {
            aLog.e("OnLoggedIn", "Cannot find fragment");
        }
        if (!isDestroyed()) {
            aLog.i("User Icon", NGUClient.getInstance().generateUserIcon());
            Picasso.with(this).load(NGUClient.getInstance().generateUserIcon()).into((CircleImageView) mNav.getHeaderView(0).findViewById(R.id.nav_img));
            ((AppCompatTextView) mNav.getHeaderView(0).findViewById(R.id.nav_id)).setText(NGUClient.getInstance().getUserId());
            ((AppCompatTextView) mNav.getHeaderView(0).findViewById(R.id.nav_user)).setText(NGUClient.getInstance().getUserName());
        }
    }

    @Override
    public void OnNotLoggedIn() {
        if (!isDestroyed())
            getSupportFragmentManager().beginTransaction().hide(getSupportFragmentManager().findFragmentById(R.id.chat_fragment)).commitAllowingStateLoss();
        //startActivityForResult(new Intent(this, FullScreenLoginActivity.class), FullScreenLoginActivity.LOGIN_REQ);
    }

    @Override
    public void OnMessageRetrieved(ArrayList<Message> messages) {
        if (getSupportFragmentManager().findFragmentById(R.id.chat_fragment) != null) {
            ((ChatFragment) getSupportFragmentManager().findFragmentById(R.id.chat_fragment)).update(messages);
        }
    }

    @Override
    public void OnNewLogin() {
        Intent intent = new Intent(this, MainChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_news:
                break;
            case R.id.nav_about:
                MessageBox.Show(this,"Unofficial NGU Shoutbox","This app was developed by Mr Smithy x. I hope you enjoy :)\n");
                break;
            case R.id.nav_setting:
                break;
            case R.id.nav_clear:
                webView.clearCache(true);
                webView.clearFormData();
                webView.clearMatches();
                webView.clearSslPreferences();
                break;
            case R.id.nav_clear_reset:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    CookieManager.getInstance().removeAllCookies(new ValueCallback<Boolean>() {
                        @Override
                        public void onReceiveValue(Boolean value) {
                            aLog.w("COOKIES CLEARED?", String.valueOf(value.booleanValue()));
                        }
                    });
                } else {
                    CookieManager.getInstance().removeAllCookie();
                }
                OnNewLogin();
                break;
            case R.id.nav_help:
                break;
            case R.id.nav_rev:
                MessageBox.Show(this,"Revisions","-Embedded Images Hosting Website\n" +
                        "-Video Sharing Websites\n" +
                        "-Ability to Upload Images & Videos\n" +
                        "-Take a screenshot\n" +
                        "-Copy Shoutbox Text & Link\n" +
                        "-Upload Shoutbox Bubble for the memories\n" +
                        "-Show Memes in the shoutbox!");
                break;
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        int grpid = item.getGroupId();
        switch (grpid) {
            case R.id.grp1:
                item.setChecked(!item.isChecked());
                switch (id) {
                    case R.id.toolbar_notif:
                        NotifSingleton.getInstance().setCanNotify(item.isChecked());
                        break;
                    case R.id.toolbar_vib:
                        NotifSingleton.getInstance().setCanVibrate(item.isChecked());
                        break;
                    case R.id.toolbar_constant:
                        NotifSingleton.getInstance().setConstantNotif(item.isChecked());
                        break;
                }
                break;
            default:
                switch (id) {
                    case R.id.toolbar_ss:
                        try {
                            File file = MediaService.getAnSaveScreenShot(getWindow().getDecorView());
                            MediaService.uploadImage(this,file,shareScreenShot);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        NotifSingleton.getInstance().setOnFront(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        NotifSingleton.getInstance().setOnFront(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        NotifSingleton.getInstance().setOnFront(false);
    }

    @Override
    protected void onStop() {
        NotifSingleton.getInstance().setOnFront(false);
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 5000) {
            if (resultCode == RESULT_OK) {
                NGUClient.getInstance().Login(data.getStringExtra("user"), data.getStringExtra("pass"));
                if (getSupportFragmentManager().findFragmentById(R.id.chat_fragment) != null) {
                    getSupportFragmentManager().beginTransaction().hide(getSupportFragmentManager().findFragmentById(R.id.chat_fragment)).commit();
                    findViewById(R.id.webViewHolder).setVisibility(View.GONE);
                } else {
                    aLog.e("OnNotLoggedIn", "Cannot find fragment");
                }
            }
        }
        if (requestCode == 300) {
            NotifSingleton.getInstance().setOnMediaActivityOpened(false);
            //Snackbar.make(getWindow().getDecorView(),String.format("%s - %s", requestCode, resultCode),Snackbar.LENGTH_LONG).show();
            return;
        }

        if (requestCode != IntentHelper.FILE_PICK) return;
        if (resultCode != RESULT_OK) return;
        Uri returnUri = data.getData();
        if(returnUri == null) return;
        String filePath = DocumentHelper.getPath(this, returnUri);
        if (filePath == null || filePath.isEmpty()) return;
        chosenFile = new File(filePath);
        if(chosenFile.getName().toLowerCase().endsWith(".mp4") ||
                chosenFile.getName().toLowerCase().endsWith(".avi") ||
                chosenFile.getName().toLowerCase().endsWith(".flv") ||
                chosenFile.getName().toLowerCase().endsWith(".webm") ||
                chosenFile.getName().toLowerCase().endsWith(".mkv")){
            try {
                MediaService.uploadVideo(chosenFile, streamableUpload);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }else {
            MediaService.uploadImage(this, chosenFile, galleryUpload);
        }
    }

    public void onChooseImage() {
       // IntentHelper.chooseFileIntent(this);
        onChooseVideo();
    }

    public void onChooseVideo(){
        IntentHelper.choseFileIntentMedia(this);
    }

    @Override
    public void OnSelectImageClicked() {
        if (doPermission(Manifest.permission.READ_EXTERNAL_STORAGE, UPLOAD_FILE)) {
            if (doPermission(Manifest.permission.ACCESS_NETWORK_STATE, UPLOAD_FILE)) {
                onChooseImage();
            }
        }
    }

    final int UPLOAD_FILE = 4711;

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case UPLOAD_FILE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    OnSelectImageClicked();
                } else {
                    Snackbar.make(getWindow().getDecorView(), "File Upload Was Canceled", Snackbar.LENGTH_LONG).show();
                }
            }
        }
    }

    public boolean doPermission(String permission, int permission_code) {
        if (ContextCompat.checkSelfPermission(this, permission) != PermissionChecker.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                ActivityCompat.requestPermissions(this, new String[]{permission}, permission_code);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{permission}, permission_code);
            }
        } else {
            return true;
        }
        return false;
    }

    private FileCallback<ImageResponse> shareScreenShot = new FileCallback<ImageResponse>() {
        @Override
        public void onResponse(Call<ImageResponse> call, Response<ImageResponse> response, File file) {
            final ImageResponse imageResponse = response.body();
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (imageResponse != null) {
                clipboardManager.setPrimaryClip(ClipData.newPlainText("ImageResponse", imageResponse.data.link));
                Snackbar snackbar = Snackbar.make(getWindow().getDecorView(), "Copied to clipboard!", Snackbar.LENGTH_LONG);
                snackbar.getView().setBackgroundColor(ContextCompat.getColor(MainChatActivity.this, R.color.md_green_700));
                snackbar.setActionTextColor(ContextCompat.getColor(MainChatActivity.this, R.color.md_white_1000));
                snackbar.setAction("Share", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        IntentHelper.shareText(v.getContext(), imageResponse.data.link);
                    }
                });
                snackbar.show();
            } else {
                onFailure(null, null, file);
            }
        }

        @Override
        public void onFailure(Call<ImageResponse> call, Throwable throwable, final File file) {
            Snackbar snackbar = Snackbar.make(getWindow().getDecorView(), "Unable to upload image", Snackbar.LENGTH_LONG);
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(MainChatActivity.this, R.color.md_red_700));
            snackbar.setActionTextColor(ContextCompat.getColor(MainChatActivity.this, R.color.md_white_1000));
            snackbar.setAction("Retry", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MediaService.uploadImage(MainChatActivity.this,file,shareScreenShot);
                }
            });
            snackbar.show();
        }
    };



    FileCallback<ImageResponse> galleryUpload = new FileCallback<ImageResponse>() {
        @Override
        public void onResponse(Call<ImageResponse> call, Response<ImageResponse> response, File file) {
            ImageResponse imageResponse = response.body();
            String s = imageResponse.data.link;
            aLog.w(TAG, s);
            ((ChatFragment) getSupportFragmentManager().findFragmentById(R.id.chat_fragment)).setTextBox(s);
        }

        @Override
        public void onFailure(Call<ImageResponse> call, Throwable throwable, File file) {
            if (call != null && throwable != null)
                aLog.w(TAG, throwable.getMessage());
            Snackbar.make(getWindow().getDecorView(), "No internet connection", Snackbar.LENGTH_SHORT).show();
        }
    };

}
