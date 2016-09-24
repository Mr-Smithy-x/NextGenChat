package co.yoprice.nextgenchat.data.jsinterface;

import android.webkit.JavascriptInterface;

import java.io.IOException;

/**
 * Created by cj on 4/23/16.
 */
public abstract class NGUCallBack {

    @JavascriptInterface
    public abstract void processHTML(String html);

    @JavascriptInterface
    public abstract void processShout(String html) throws IOException;

    @JavascriptInterface
    public abstract void processUnidle(String html);

    @JavascriptInterface
    public abstract void processLoginUsername(String html);

    @JavascriptInterface
    public abstract void processLoginPassword(String html);


    @JavascriptInterface
    public abstract void processLogin(String html);

}
