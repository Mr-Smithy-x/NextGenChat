package co.yoprice.nextgenchat.ui.custom;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

/**
 * Created by cj on 5/17/16.
 */
public class MessageBox {
    public static void Show(Context context, String title, String message){
        new AlertDialog.Builder(context).setTitle(title).setMessage(message).show();
    }

    public static void Show(Context context, String title, String message, String positive, DialogInterface.OnClickListener onClickListener){
        new AlertDialog.Builder(context).setTitle(title).setMessage(message).setPositiveButton(positive,onClickListener).show();
    }
}
