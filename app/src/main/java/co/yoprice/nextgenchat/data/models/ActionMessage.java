package co.yoprice.nextgenchat.data.models;

import java.io.Serializable;

/**
 * Created by cj on 5/13/16.
 */
public class ActionMessage implements Serializable{
    private boolean isActionMessage = false;
    public boolean isActionMessage() {
        return isActionMessage;
    }
    protected void setActionMessage(boolean actionMessage) {
        this.isActionMessage = actionMessage;
    }
}
