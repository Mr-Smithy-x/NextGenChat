package com.example;

public class MyClass {
    private static String yt2Image(String url){
        if(url.toLowerCase().contains("watch") && url.toLowerCase().contains("?v=")) {
            String youtube_vid = url.substring(url.indexOf("?v=") + 3);
            int index = youtube_vid.indexOf("&");
            if (index != -1) {
                youtube_vid = youtube_vid.substring(0, index);
            }
            return String.format("https://i.ytimg.com/vi/%s/hqdefault.jpg", youtube_vid);
        }else if(url.contains("youtu.be")){
            String vid = url.substring(url.lastIndexOf("/")+1);
            return String.format("https://i.ytimg.com/vi/%s/hqdefault.jpg", vid);
        }
        return null;
    }

    public static void main(String[] args){
        String url = "https://www.youtube.com/watch?v=Y4OFv9eWIKY&bitch=yo";
        String url2 = "https://youtu.be/Y4OFv9eWIKY";
        String image = yt2Image(url2);
        System.out.println(image);
    }
}
