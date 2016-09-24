package co.yoprice.nextgenchat.data.models;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by cj on 5/1/16.
 */
public class StreamableVideo implements Serializable{
    private String jpg;
    private String title;
    private String message;
    private boolean init;

    public StreamableVideo setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public StreamableVideo setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public boolean isInit() {
        return init;
    }

    public StreamableVideo setInit(boolean value){
        this.init = value;
        return this;
    }

    public enum VIDEO_TYPE{
        mp4,mp4_mobile,webm, webm_mobile, flv, flv_mobile
    }

    HashMap<String,String> videos = new HashMap<String, String>();

    public static StreamableVideo Builder(){
        return new StreamableVideo();
    }

    private StreamableVideo(){}

    public StreamableVideo addVideo(VIDEO_TYPE type, String url){
        videos.put(type.name(),url);
        return this;
    }

    public String getVideo(VIDEO_TYPE video_type) {
        String vid = videos.get(video_type.name());
        if(vid.startsWith("//")) return "https:" + vid;
        else return vid;
    }

    public boolean hasVideoType(VIDEO_TYPE video_type){
        return videos.containsKey(video_type.name());
    }

    public String getJpg() {
        if(jpg.startsWith("//")) return "https:" + jpg;
        else return jpg;
    }

    public StreamableVideo setJpg(String jpg) {
        this.jpg = jpg;
        return this;
    }
}
