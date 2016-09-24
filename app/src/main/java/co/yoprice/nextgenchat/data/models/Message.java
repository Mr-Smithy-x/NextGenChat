package co.yoprice.nextgenchat.data.models;

import android.util.Log;

import org.jsoup.nodes.Element;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cj on 4/24/16.
 */
public class Message extends ActionMessage implements Serializable {
    private String user, message, time;
    private Color user_color, messageColor;
    private ArrayList<String> smilies;
    private String link;
    private String userId;
    private String messageElement;
    private String userElement;

    private Message() {

    }

    public Message toggleActionMessage(boolean bool){
        super.setActionMessage(bool);
        return this;
    }

    public String getLink() {
        return link;
    }

    @Override
    public String toString() {
        return String.format("%s - %s", user, message);
    }

    public void print() {
        Log.e("MESSAGE", toString());
    }

    public static Message Builder() {
        return new Message();
    }

    public String getUser() {
        return user;
    }

    public Message setUser(String user) {
        this.user = user;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public Message setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getTime() {
        return time;
    }

    public Message setTime(String time) {
        this.time = time;
        return this;
    }

    public Color getUser_color() {
        return user_color;
    }

    public Message setUser_color(Color user_color) {
        this.user_color = user_color;
        return this;
    }

    public Color getMessageColor() {
        return messageColor;
    }

    public Message setMessageColor(Color messageColor) {
        this.messageColor = messageColor;
        return this;
    }

    public Message setSmilies(ArrayList<String> smilies) {
        this.smilies = smilies;
        return this;
    }


    public List<String> getSmilies() {
        return smilies;
    }

    public Message setLink(String link) {
        this.link = link;
        return this;
    }

    public Message setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public String getMessageElement() {
        return messageElement;
    }

    public Message setMessageElement(Element messageElement) {
        if(messageElement != null) this.messageElement = fix(messageElement).html();
        else this.messageElement = null;
        return this;
    }

    private Element fix(Element messageElement) {
        for(Element element : messageElement.getElementsByTag("img")){
           String src = element.attr("src");
            if(src.startsWith("http")) continue;
            else if(src.startsWith("images/smilies")){
                src = "http://nextgenupdate.com/forums/" + src;
                element.attr("src",src);
            }
        }
        return messageElement;
    }

    public String getUserElement() {
        return userElement;
    }

    public Message setUserElement(Element userElement) {
        this.userElement = userElement.html();
        return this;
    }
}
