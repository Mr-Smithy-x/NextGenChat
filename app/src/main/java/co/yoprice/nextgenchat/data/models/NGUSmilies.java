package co.yoprice.nextgenchat.data.models;

/**
 * Created by cj on 5/13/16.
 */
public class NGUSmilies{
    String url;
    String file_name;
    String short_url;

    public static NGUSmilies Builder(){
        return new NGUSmilies();
    }

    public String getUrl() {
        return url;
    }

    public NGUSmilies setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getFile_name() {
        return file_name;
    }

    public NGUSmilies setFile_name(String file_name) {
        this.file_name = file_name;
        return this;
    }

    public String getShort_url() {
        return short_url;
    }

    public NGUSmilies setShort_url(String short_url) {
        this.short_url = short_url;
        return this;
    }
}


