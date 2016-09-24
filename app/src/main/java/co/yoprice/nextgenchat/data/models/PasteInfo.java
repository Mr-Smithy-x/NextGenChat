package co.yoprice.nextgenchat.data.models;

import java.io.Serializable;

/**
 * Created by cj on 5/3/16.
 */
public class PasteInfo implements Serializable{
    private String link;
    private String content;

    private PasteInfo(){}

    public static PasteInfo Builder(){
        return new PasteInfo();
    }

    public String getLink() {
        return link;
    }

    public PasteInfo setLink(String link) {
        this.link = link;
        return this;
    }

    public String getContent() {
        return content;
    }

    public PasteInfo setContent(String content) {
        this.content = content;
        return this;
    }
}
