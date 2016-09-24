package co.yoprice.nextgenchat.utils;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.text.Html;
import android.view.View;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import co.yoprice.nextgenchat.data.models.NGUSmilies;

/**
 * Created by cj on 5/8/16.
 */
public class URLImageParser implements Html.ImageGetter {
    Context c;
    View container;
    int WIDTH = 128, HEIGHT = 128;
    private URLImageParserCallBack urlImageParserCallBack;

    /***
     * Construct the URLImageParser which will execute AsyncTask and refresh the container
     *
     * @param t
     * @param c
     */
    public URLImageParser(View t, Context c, URLImageParserCallBack urlImageParserCallBack) {
        this.c = c;
        this.container = t;
        this.urlImageParserCallBack = urlImageParserCallBack;
    }

    public Drawable getDrawable(String source) {
        NGUSmilies nguSmilies = MediaParser.getSmilies(source);
        if(nguSmilies != null){
            aLog.e("Smilies", nguSmilies.getFile_name());
            try {
                int drawable = MediaParser.getSmliesId(nguSmilies);
                if(drawable != -1){
                    aLog.e("Smilie ID", String.valueOf(drawable));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        return c.getDrawable(drawable);
                    }else{
                        return c.getResources().getDrawable(drawable);
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        URLDrawable urlDrawable = new URLDrawable();

        // get the actual source
        ImageGetterAsyncTask asyncTask =
                new ImageGetterAsyncTask(urlDrawable);

        asyncTask.execute(source);

        // return reference to URLDrawable where I will change with actual image from
        // the src tag
        return urlDrawable;
    }


    public interface URLImageParserCallBack{
        void onLoaded(String url, Drawable drawable);
    }

    public class ImageGetterAsyncTask extends AsyncTask<String, Void, Drawable> {
        URLDrawable urlDrawable;

        public ImageGetterAsyncTask(URLDrawable d) {
            this.urlDrawable = d;
        }

        @Override
        protected Drawable doInBackground(String... params) {
            String source = params[0];
            return fetchDrawable(source);
        }

        @Override
        protected void onPostExecute(Drawable result) {

                // set the correct bound according to the result from HTTP call
                urlDrawable.setBounds(0, 0, result.getIntrinsicWidth(), result.getIntrinsicHeight());
                // change the reference of the current drawable to the result
                // from the HTTP call
                urlDrawable.drawable = result;
                // redraw the image by invalidating the container
                URLImageParser.this.container.invalidate();
                urlImageParserCallBack.onLoaded("",urlDrawable);
        }


        /***
         * Get the Drawable from URL
         *
         * @param urlString
         * @return
         */
        public Drawable fetchDrawable(String urlString) {
            try {
                System.out.println(urlString);
                Drawable drawable = new BitmapDrawable(container.getResources(), Picasso.with(container.getContext()).load(urlString).get());
               drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                return drawable;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        private InputStream fetch(String urlString) throws IOException {
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(urlString).openConnection();
            httpURLConnection.connect();
            return httpURLConnection.getInputStream();
        }
    }
}