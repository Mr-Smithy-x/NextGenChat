package co.yoprice.nextgenchat;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import uk.co.senab.photoview.PhotoViewAttacher;

public class ImageActivity extends AppCompatActivity {

    private ImageView img;
    private PhotoViewAttacher mAttacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        img = (ImageView) findViewById(R.id.image_view);
        if(getIntent() != null && getIntent().hasExtra("url")){
            String url = getIntent().getStringExtra("url");
            Picasso.with(this).load(url).into(img, new Callback() {
                @Override
                public void onSuccess() {
                    mAttacher = new PhotoViewAttacher(img);

                }

                @Override
                public void onError() {

                }
            });
        }
    }
}
