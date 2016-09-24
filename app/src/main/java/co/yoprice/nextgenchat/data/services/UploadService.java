package co.yoprice.nextgenchat.data.services;

import android.content.Context;



import java.lang.ref.WeakReference;

import co.yoprice.nextgenchat.data.Constants;
import co.yoprice.nextgenchat.helpers.NotificationHelper;
import co.yoprice.nextgenchat.data.imgurmodel.ImageResponse;
import co.yoprice.nextgenchat.data.imgurmodel.ImgurAPI;
import co.yoprice.nextgenchat.data.imgurmodel.Upload;
import co.yoprice.nextgenchat.utils.NetworkUtils;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class UploadService {
    public final static String TAG = UploadService.class.getSimpleName();

    private WeakReference<Context> mContext;

    public UploadService(Context context) {
        this.mContext = new WeakReference<>(context);
    }

    public void Execute(Upload upload, Callback<ImageResponse> callback) {
        final Callback<ImageResponse> cb = callback;

        if (!NetworkUtils.isConnected(mContext.get())) {
            //Callback will be called, so we prevent a unnecessary notification
            cb.onFailure(null, null);
            return;
        }

        final NotificationHelper notificationHelper = new NotificationHelper(mContext.get());
        notificationHelper.createUploadingNotification();

        Retrofit restAdapter = buildRestAdapter();

        ImgurAPI imgurAPI = restAdapter.create(ImgurAPI.class);

        Call<ImageResponse> imageResponseCall = imgurAPI.postImage(
                Constants.getClientAuth(),
                upload.title,
                upload.description,
                upload.albumId,
                null,
                RequestBody.create(MediaType.parse("image/*"), upload.image));
        imageResponseCall.enqueue(new Callback<ImageResponse>() {

            @Override
            public void onResponse(Call<ImageResponse> call, Response<ImageResponse> response) {
                if (cb != null) cb.onResponse(call, response);
                if (response == null) {
                    notificationHelper.createFailedUploadNotification();
                    return;
                }
                if (response.body() != null && response.body().success) {
                    notificationHelper.createUploadedNotification(response.body());
                }
            }

            @Override
            public void onFailure(Call<ImageResponse> call, Throwable t) {
                if (cb != null) cb.onFailure(call, t);
                notificationHelper.createFailedUploadNotification();
            }
        });
    }

    private Retrofit buildRestAdapter() {

        Retrofit.Builder imgurAdapter = new Retrofit.Builder()
                .baseUrl(ImgurAPI.server)
                .addConverterFactory(GsonConverterFactory.create());

        /*
        Set rest adapter logging if we're already logging
        */
        if (Constants.LOGGING) {

            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
            imgurAdapter.client(client);
        }
        return imgurAdapter.build();
    }
}
