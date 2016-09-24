package co.yoprice.nextgenchat.data.services;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import cafe.adriel.androidstreamable.AndroidStreamable;
import cafe.adriel.androidstreamable.callback.NewVideoCallback;
import cafe.adriel.androidstreamable.callback.VideoCallback;
import cafe.adriel.androidstreamable.model.NewVideo;
import cafe.adriel.androidstreamable.model.Video;
import co.yoprice.nextgenchat.MainChatActivity;
import co.yoprice.nextgenchat.data.callbacks.FileCallback;
import co.yoprice.nextgenchat.data.callbacks.UiCallback;
import co.yoprice.nextgenchat.data.imgurmodel.ImageResponse;
import co.yoprice.nextgenchat.data.imgurmodel.Upload;

/**
 * Created by cj on 5/16/16.
 */
public class MediaService {
    public static void uploadImage(Context context, final File file, FileCallback<ImageResponse> callback) {
        new UploadService(context).Execute(Upload.createUpload(file), new UiCallback<ImageResponse>(file, callback));
    }

    public static Bitmap getScreenShot(View view){
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache(true);
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }

    @SuppressLint("SetWorldReadable")
    public static File saveBitmap(View view, Bitmap bitmap) throws IOException {
        File file = new File(view.getContext().getCacheDir(), String.valueOf(System.currentTimeMillis()) + ".png");
        FileOutputStream fOut = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        fOut.flush();
        fOut.close();
        file.setReadable(true, false);
        view.setDrawingCacheEnabled(false);
        return file;
    }
    public static File getAnSaveScreenShot(View view) throws IOException {
        Bitmap bitmap = getScreenShot(view);
        return saveBitmap(view,bitmap);
    }

    public static void uploadVideo(File file, NewVideoCallback newVideoCallback) throws FileNotFoundException {
        String title = "NGU Chat Video Upload";
        AndroidStreamable.uploadVideo(file, title, newVideoCallback);
    }
}
