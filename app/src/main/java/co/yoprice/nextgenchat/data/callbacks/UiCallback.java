package co.yoprice.nextgenchat.data.callbacks;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;

import java.io.File;

import co.yoprice.nextgenchat.R;
import co.yoprice.nextgenchat.data.imgurmodel.ImageResponse;
import co.yoprice.nextgenchat.helpers.IntentHelper;
import co.yoprice.nextgenchat.ui.fragments.ChatFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by cj on 5/16/16.
 */
public class UiCallback<T> implements Callback<T> {

    public File file;
    public FileCallback<T> callback;


    private UiCallback() {

    }

    public UiCallback(File file, FileCallback<T> callback) {
        this.file = file;
        this.callback = callback;
    }

    private String TAG = UiCallback.class.getSimpleName();

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        callback.onResponse(call, response,file);
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        callback.onFailure(call, t,file);

    }
}
