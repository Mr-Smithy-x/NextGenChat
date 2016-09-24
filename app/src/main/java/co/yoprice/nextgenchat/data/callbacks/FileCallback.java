package co.yoprice.nextgenchat.data.callbacks;

import java.io.File;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by cj on 5/16/16.
 */
public interface FileCallback<T> {
    void onResponse(Call<T> call, Response<T> response, File file);
    void onFailure(Call<T> call, Throwable throwable, File file);
}
