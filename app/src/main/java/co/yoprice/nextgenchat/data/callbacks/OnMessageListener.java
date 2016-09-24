package co.yoprice.nextgenchat.data.callbacks;

import android.content.Context;

import co.yoprice.nextgenchat.data.models.Message;

/**
 * Created by cj on 4/30/16.
 */
public interface OnMessageListener {
    void OnMessageReceived(Message message, Context context);
    void OnMyNameDiscussed(Message message, Context context);
}
