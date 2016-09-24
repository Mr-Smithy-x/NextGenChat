package co.yoprice.nextgenchat.ui.custom;

import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.view.View;

import co.yoprice.nextgenchat.R;

/**
 * Created by cj on 5/16/16.
 */
public class Snackbar {
    public final static int LENGTH_LONG = android.support.design.widget.Snackbar.LENGTH_LONG;
    public final static int LENGTH_SHORT = android.support.design.widget.Snackbar.LENGTH_SHORT;
    public final static int LENGTH_INDEFINITE = android.support.design.widget.Snackbar.LENGTH_INDEFINITE;


    public static android.support.design.widget.Snackbar make(View view, String string, int time, @ColorInt int color, @ColorInt int actioncolor){
        android.support.design.widget.Snackbar snackbar = android.support.design.widget.Snackbar.make(view, string, time);
        snackbar.getView().setBackgroundColor(color);
        snackbar.setActionTextColor(actioncolor);
        return snackbar;
    }
}
