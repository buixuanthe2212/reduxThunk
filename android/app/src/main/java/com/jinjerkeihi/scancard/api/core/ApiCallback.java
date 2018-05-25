package com.jinjerkeihi.scancard.api.core;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A custom Callback.
 *
 * @param <T> expected response mediaContentType
 */
public abstract class ApiCallback<T> implements Callback<T> {

    public abstract void onResponse(T response);
    public abstract void onFailure(Throwable throwable);

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (response.isSuccessful()) {
            onResponse(response.body());
        } else {
            onFailure(null);
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        onFailure(t);
    }

}
