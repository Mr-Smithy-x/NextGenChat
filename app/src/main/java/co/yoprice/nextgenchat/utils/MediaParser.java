package co.yoprice.nextgenchat.utils;

import android.content.Context;
import android.support.annotation.DrawableRes;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import co.yoprice.nextgenchat.R;
import co.yoprice.nextgenchat.data.models.Message;
import co.yoprice.nextgenchat.data.models.NGUSmilies;
import co.yoprice.nextgenchat.data.models.PasteInfo;
import co.yoprice.nextgenchat.data.models.StreamableVideo;

public class MediaParser {

    private static String streamable_stub = "https://api.streamable.com/videos/%s";
    private static String pastebin_stub = "http://pastebin.com/raw/%s";

    private enum ACCEPT {
        IMAGE("image/webp,image/*,*/*;q=0.8"), VIDEO("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        String accept;

        public String getAccept() {
            return accept;
        }

        ACCEPT(String accept) {
            this.accept = accept;
        }
    }

    public interface OnMediaCallBack {
        void onImageFound(String url);

        void onVideosFound(ArrayList<StreamableVideo> streamableVideos);

        void onVideoFound(StreamableVideo streamableVideo);

        void onPasteInfoFound(PasteInfo pasteInfo, Message message);

        void onNothingFound(String url);

        void onYoutubeVideoImageFound(String youtubeImage, String url);
    }

    public static void smartParse(final String url, final OnMediaCallBack onImageUrlCallback, Message message) throws IOException, JSONException {
        //region PRNTSCR
        if (url.toLowerCase().contains("prnt.sc") || url.toLowerCase().contains("prntscr.com")) {
            String s = getUrl(url, ACCEPT.IMAGE);
            Document d = Jsoup.parse(s);
            Elements img = d.select("img.image__pic.js-image-pic");
            String imageUrl = null;
            for (Element e : img) {
                if (e.hasAttr("src")) {
                    imageUrl = e.attr("src");
                    if (imageUrl.startsWith("//")) imageUrl = "http:" + imageUrl;
                    onImageUrlCallback.onImageFound(imageUrl);
                    return;
                }
            }
        } //endregion
        //region GYAZO
        else if (url.toLowerCase().contains("gyazo.com")) {
            String gyazo = String.format("https://i.gyazo.com/%s.png", url.substring(url.lastIndexOf("/") + 1));
            String gyazoJpg = String.format("https://i.gyazo.com/%s.jpg", url.substring(url.lastIndexOf("/") + 1));
            String test = getUrl(gyazo, ACCEPT.IMAGE);
            if (test != null && !test.contains("Gyazo image is not found"))
                onImageUrlCallback.onImageFound(gyazo);
            else
                onImageUrlCallback.onImageFound(gyazoJpg);
            return;
        } //endregion
        //region IMGUR
        else if (url.toLowerCase().contains("imgur.com")) {
            if (url.toLowerCase().endsWith(".gifv")) {
                ////i.imgur.com/DinJPMm.mp4
                String mp4 = String.format("http://i.imgur.com/%s.mp4", url.substring(url.lastIndexOf("/") + 1).replace(".gifv", ""));
                String webm = String.format("http://i.imgur.com/%s.webm", url.substring(url.lastIndexOf("/") + 1).replace(".gifv", ""));
                onImageUrlCallback.onVideoFound(StreamableVideo.Builder().addVideo(StreamableVideo.VIDEO_TYPE.mp4, mp4).addVideo(StreamableVideo.VIDEO_TYPE.webm, webm).setTitle(url.substring(url.lastIndexOf("/") + 1)).setMessage(url).setInit(true));
                return;
            } else if (url.toLowerCase().endsWith(".mp4")) {
                ////i.imgur.com/DinJPMm.mp4
                String mp4 = String.format("http://i.imgur.com/%s.mp4", url.substring(url.lastIndexOf("/") + 1).replace(".mp4", ""));
                String webm = String.format("http://i.imgur.com/%s.webm", url.substring(url.lastIndexOf("/") + 1).replace(".mp4", ""));
                onImageUrlCallback.onVideoFound(StreamableVideo.Builder().addVideo(StreamableVideo.VIDEO_TYPE.mp4, mp4).addVideo(StreamableVideo.VIDEO_TYPE.webm, webm).setTitle(url.substring(url.lastIndexOf("/") + 1)).setMessage(url).setInit(true));
                return;
            }else if(url.contains("?")){
                onImageUrlCallback.onImageFound(url.substring(0, url.indexOf("?")));
                return;
            }
            Document d = Jsoup.parse(new URL(url), 3000);
            Elements img = d.getElementsByClass("post-image");
            String imageUrl = null;
            for (Element e : img) {
                Elements imgs = e.getElementsByTag("img");
                for (Element i : imgs) {
                    if (i.hasAttr("src")) {
                        imageUrl = i.attr("src");
                        final String finalImageUrl = (imageUrl.startsWith("//")) ? "http:" + imageUrl : imageUrl;
                        onImageUrlCallback.onImageFound(finalImageUrl);
                        return;
                    }
                }
            }
        } //endregion
        //region SCREENCLOUD
        else if (url.toLowerCase().contains("screencloud.net/v/")) {
            Document d = Jsoup.parse(new URL(url), 3000);
            Elements e = d.select("div.screenshot");
            for (Element img : e) {
                for (Element src : img.getElementsByTag("img")) {
                    String s = src.attr("src").startsWith("//") ? "http:" + src.attr("src") : src.attr("src");
                    onImageUrlCallback.onImageFound(s);
                    return;
                }
            }
        } //endregion
        //region STREAMABLE
        else if (url.toLowerCase().contains("streamable.com")) {
            StreamableVideo streamableVideo = streamableParseJson(url);
            if (streamableVideo == null) {
                aLog.e("NULL", "VIDEO IS NULL");
            }
            if (streamableVideo != null) {
                onImageUrlCallback.onVideoFound(streamableVideo);
                return;
            } else {
                ArrayList<StreamableVideo> streamableVideos = streamableParseHtml(url);
                if (streamableVideos.size() == 1) {
                    onImageUrlCallback.onVideoFound(streamableVideos.get(0));
                } else if (streamableVideos.size() > 1) {
                    onImageUrlCallback.onVideosFound(streamableVideos);
                }
            }
            return;
        } //endregion
        //region PASTIE OR PASTEBIN
        else if (url.toLowerCase().contains("pastie.org/pastes") || url.toLowerCase().contains("pastebin.com")) {
            if ((url.toLowerCase().contains("pastebin.com") && !url.toLowerCase().contains("/raw/")) || (url.toLowerCase().contains("pastie.org/pastes") && !url.toLowerCase().endsWith("/text"))) {
                pasteBin(url, onImageUrlCallback, message);
                return;
            } else if (url.toLowerCase().contains("pastebin.com") && url.toLowerCase().contains("/raw/")) {
                String content = getUrl(url, ACCEPT.VIDEO);
                onImageUrlCallback.onPasteInfoFound(PasteInfo.Builder().setLink(url).setContent(content), message);
                return;
            } else if (url.toLowerCase().contains("pastie.org/pastes") && url.toLowerCase().endsWith("/text")) {
                String content = getUrl(url, ACCEPT.VIDEO);
                onImageUrlCallback.onPasteInfoFound(PasteInfo.Builder().setLink(url).setContent(content), message);
                return;
            }
        } //endregion
        //region Youtube
        else if (url.contains("youtube.com") || url.contains("youtu.be")) {
            String youtubeImage = yt2Image(url);
            onImageUrlCallback.onYoutubeVideoImageFound(youtubeImage, url);
            return;
        }//endregion
        //region Videos
        else if (url.toLowerCase().endsWith(".mp4")) {
            onImageUrlCallback.onVideoFound(StreamableVideo.Builder().addVideo(StreamableVideo.VIDEO_TYPE.mp4, url).setInit(true).setMessage(message.getMessage()).setTitle(message.getUser()));
            return;
        } else if (url.toLowerCase().endsWith(".webm")) {
            onImageUrlCallback.onVideoFound(StreamableVideo.Builder().addVideo(StreamableVideo.VIDEO_TYPE.webm, url).setInit(true).setMessage(message.getMessage()).setTitle(message.getUser()));
            return;
        } else if (url.toLowerCase().endsWith(".flv")) {
            onImageUrlCallback.onVideoFound(StreamableVideo.Builder().addVideo(StreamableVideo.VIDEO_TYPE.flv, url).setInit(true).setMessage(message.getMessage()).setTitle(message.getUser()));
            return;
        }//endregion
        onImageUrlCallback.onNothingFound(url);
    }

    public static String cleanseUrl(String url) {
        int index = url.indexOf("?");
        if (index > -1) {
            return url.substring(0, index);
        } else return url;
    }

    @DrawableRes
    public static int getSmliesId(NGUSmilies nguSmilies) throws IllegalAccessException {
        Class s = R.drawable.class;
        Field[] fields = s.getDeclaredFields();
        for (Field f : fields) {
            String name = f.getName();
            if (nguSmilies.getFile_name().split("\\.")[0].equalsIgnoreCase(f.getName())) {
                return f.getInt(f.getName());
            }
        }
        return R.drawable.smile;
    }

    public static NGUSmilies getSmilies(String text) {
        aLog.e("SMILEY", text);
        text = text.substring(text.lastIndexOf("/") + 1);
        aLog.i("SMILEY", text);
        aLog.i("Size", String.valueOf(NGUClient.getInstance().getSmilies().size()));
        for (NGUSmilies nguSmilies : NGUClient.getInstance().getSmilies()) {
            if (nguSmilies.getUrl().endsWith(text) || nguSmilies.getShort_url().endsWith(text) || nguSmilies.getFile_name().endsWith(text)) {
                return nguSmilies;
            }
        }
        return null;
    }

    public static ArrayList<NGUSmilies> getSmilies(Context context) {
        BufferedReader br = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.smilies)));
        StringBuilder sb = new StringBuilder();
        String s = null;
        try {
            while ((s = br.readLine()) != null) sb.append(s);
            br.close();
            return new Gson().fromJson(sb.toString(), new TypeToken<ArrayList<NGUSmilies>>() {
            }.getType());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private static String yt2Image(String url) {
        if (url.toLowerCase().contains("watch") && url.toLowerCase().contains("?v=")) {
            String youtube_vid = url.substring(url.indexOf("?v=") + 3);
            int index = youtube_vid.indexOf("&");
            if (index != -1) {
                youtube_vid = youtube_vid.substring(0, index);
            }

            return String.format("https://i.ytimg.com/vi/%s/hqdefault.jpg", cleanseUrl(youtube_vid));
        } else if (url.contains("youtu.be")) {
            String vid = url.substring(url.lastIndexOf("/") + 1);
            return String.format("https://i.ytimg.com/vi/%s/hqdefault.jpg", cleanseUrl(vid));
        }
        return null;
    }

    private static String getUrl(String Url, ACCEPT accept) throws IOException {
        URL url = new URL(Url);
        String s = null;
        if (Url.startsWith("https")) {
            HttpsURLConnection httpURLConnection = (HttpsURLConnection) url.openConnection();
            httpURLConnection.setRequestProperty("Accept", accept.getAccept());
            httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.86 Safari/537.36");
            httpURLConnection.connect();
            if (httpURLConnection.getResponseCode() == 200) {
                s = readInput(httpURLConnection.getInputStream());
            }
            httpURLConnection.disconnect();
            return s;
        } else {
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestProperty("Accept", accept.getAccept());
            httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.86 Safari/537.36");
            httpURLConnection.connect();
            if (httpURLConnection.getResponseCode() == 200) {
                s = readInput(httpURLConnection.getInputStream());
            }
            httpURLConnection.disconnect();
            return s;
        }
    }

    private static String getUrlLine(String Url, ACCEPT accept) throws IOException {
        URL url = new URL(Url);
        String s = null;
        if (Url.startsWith("https")) {
            HttpsURLConnection httpURLConnection = (HttpsURLConnection) url.openConnection();
            httpURLConnection.setRequestProperty("Accept", accept.getAccept());
            httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.86 Safari/537.36");
            httpURLConnection.connect();
            if (httpURLConnection.getResponseCode() == 200) {
                s = readInputLine(httpURLConnection.getInputStream());
            }
            httpURLConnection.disconnect();
            return s;
        } else {
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestProperty("Accept", accept.getAccept());
            httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.86 Safari/537.36");
            httpURLConnection.connect();
            if (httpURLConnection.getResponseCode() == 200) {
                s = readInputLine(httpURLConnection.getInputStream());
            }
            httpURLConnection.disconnect();
            return s;
        }
    }

    private static String readInput(InputStream inputStream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        String stub = null;
        while ((stub = br.readLine()) != null) sb.append(stub);
        br.close();
        return sb.toString();
    }

    private static String readInputLine(InputStream inputStream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        String stub = null;
        while ((stub = br.readLine()) != null) sb.append(stub + "\n");
        br.close();
        return sb.toString();
    }

    private static void pasteBin(String url, OnMediaCallBack onMediaCallBack, Message message) throws IOException {
        if (url.contains("pastebin.com")) {
            String pasteStub = String.format(pastebin_stub, url.substring(url.lastIndexOf("/") + 1));
            String content = getUrlLine(pasteStub, ACCEPT.VIDEO);
            onMediaCallBack.onPasteInfoFound(PasteInfo.Builder().setLink(url).setContent(content), message);
        } else if (url.contains("pastie.org/pastes/")) {
            String pastieStub = url + "/download";
            String content = getUrlLine(pastieStub, ACCEPT.VIDEO);
            onMediaCallBack.onPasteInfoFound(PasteInfo.Builder().setLink(url).setContent(content), message);
        } else {
            onMediaCallBack.onNothingFound(url);
        }
    }

    private static StreamableVideo streamableParseJson(String url) throws IOException, JSONException {
        StreamableVideo streamableVideo = StreamableVideo.Builder();
        String stub = String.format(streamable_stub, url.substring(url.lastIndexOf("/", url.length()) + 1));

        String json = getUrl(stub, ACCEPT.VIDEO);
        if (json == null) return null;
        JSONObject jsonObject = new JSONObject(json);
        if (jsonObject.getInt("status") == 2) {
            if (jsonObject.has("files")) {
                JSONObject files = jsonObject.getJSONObject("files");
                if (files.has("mp4")) {
                    String mp4 = files.getJSONObject("mp4").getString("url");

                    aLog.e("URL", mp4);
                    streamableVideo.addVideo(StreamableVideo.VIDEO_TYPE.mp4, mp4);
                    streamableVideo.setInit(true);
                }
                if (files.has("mp4-mobile")) {
                    String mp4_mobile = files.getJSONObject("mp4-mobile").getString("url");

                    aLog.e("URL", mp4_mobile);
                    streamableVideo.addVideo(StreamableVideo.VIDEO_TYPE.mp4_mobile, mp4_mobile);
                    streamableVideo.setInit(true);
                }
                if (files.has("webm")) {
                    String webm = files.getJSONObject("webm").getString("url");

                    aLog.e("URL", webm);
                    streamableVideo.addVideo(StreamableVideo.VIDEO_TYPE.webm, webm);
                    streamableVideo.setInit(true);
                }
                if (files.has("webm-mobile")) {
                    String webm_mobile = files.getJSONObject("webm-mobile").getString("url");

                    aLog.e("URL", webm_mobile);
                    streamableVideo.addVideo(StreamableVideo.VIDEO_TYPE.webm_mobile, webm_mobile);
                    streamableVideo.setInit(true);
                }
            }
            if (jsonObject.has("thumbnail_url")) {
                String thumnail_url = jsonObject.getString("thumbnail_url");
                streamableVideo.setJpg(thumnail_url);
                streamableVideo.setInit(true);
            }
            if (jsonObject.has("title")) {
                String title = jsonObject.optString("title", url.substring(url.lastIndexOf("/") + 1));
                if (title.length() == 0)
                    streamableVideo.setTitle(url.substring(url.lastIndexOf("/") + 1));
                else streamableVideo.setTitle(title);
                streamableVideo.setInit(true);
            }
            if (jsonObject.has("message")) {
                String message = jsonObject.optString("message", url.substring(url.lastIndexOf("/") + 1));
                if (message == null || message.length() == 0)
                    streamableVideo.setTitle(url.substring(url.lastIndexOf("/") + 1));
                else streamableVideo.setMessage(message);
                streamableVideo.setInit(true);
            }
        }
        ;
        if (streamableVideo.isInit()) return streamableVideo;
        else return null;
    }

    private static ArrayList<StreamableVideo> streamableParseHtml(String url) throws IOException {
        ArrayList<StreamableVideo> streamableVideos = new ArrayList<>();
        Document d = Jsoup.parse(new URL(url), 3000);
        Elements e = d.getElementsByTag("video");
        for (Element video : e) {
            if (video.hasAttr("poster")) {
                String poster = video.attr("poster"), src = null;
                for (Element source : video.getElementsByTag("source")) {
                    if (source.hasAttr("src")) {
                        src = source.attr("src");
                        String type = source.attr("type");

                        if (type.contains("mp4")) {
                            streamableVideos.add(StreamableVideo.Builder().addVideo(StreamableVideo.VIDEO_TYPE.mp4, src).setJpg(poster).setInit(true));
                        } else if (type.contains("webm")) {
                            streamableVideos.add(StreamableVideo.Builder().addVideo(StreamableVideo.VIDEO_TYPE.webm, src).setJpg(poster).setInit(true));
                        }
                    }
                }
            }
        }
        return streamableVideos;
    }
}



