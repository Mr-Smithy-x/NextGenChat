package co.yoprice.nextgenchat.helpers;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;

import java.io.File;
import java.io.FileOutputStream;

public class IntentHelper {
    public final static int FILE_PICK = 1001;

    public static void shareBitmap(Activity activity, Bitmap bitmap, String fileName) {
        try {
            File file = new File(activity.getCacheDir(), fileName + ".png");
            FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
            file.setReadable(true, false);
            final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            intent.setType("image/png");
            activity.startActivity(Intent.createChooser(intent, "Share using?"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void shareText(Context activity, String text) {
        try {
            Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_TEXT, text);
            intent.setType("text/plain");
            activity.startActivity(Intent.createChooser(intent, "Share using?"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void launchUrlIntent(String url,Activity activity){
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        activity.startActivityForResult(i, 300);
    }

    public static void chooseFileIntent(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        activity.startActivityForResult(intent, FILE_PICK);
    }

    public static void choseFileIntentMedia(Activity activity){
        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        if (Build.VERSION.SDK_INT < 19) photoPickerIntent.setType("image/* video/*");
        else photoPickerIntent.setType("*/*");

        activity.startActivityForResult(photoPickerIntent, FILE_PICK);
    }
}
