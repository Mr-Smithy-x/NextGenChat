package co.yoprice.nextgenchat.utils;

import android.content.Context;

import java.util.List;

import co.yoprice.nextgenchat.data.callbacks.OnMessageListener;
import co.yoprice.nextgenchat.data.models.Message;

/**
 * Created by cj on 4/30/16.
 */
public class SmartParse {
    public static void parse(List<Message> messageList, Context context, OnMessageListener onMessageListener){
        for(Message message : messageList){
            if(doesContainName(message)){
                onMessageListener.OnMyNameDiscussed(message,context);
            }else{
                if(!message.getMessage().isEmpty() && !message.getMessage().equals(":"))
                    onMessageListener.OnMessageReceived(message,context);
            }
        }
    }

    public static boolean doesContainName(Message message){
        String user = NGUClient.getInstance().getUserName();
        if(user != null){
            if(message.getMessage().contains(user) && !message.getUser().contains(user)) return true;
            else return false;
        }else return false;
    }
}
