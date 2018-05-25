package com.jinjerkeihi.scancard.resultcard;

import com.google.gson.Gson;
import com.jinjerkeihi.nfcfelica.transit.Trip;
import com.jinjerkeihi.scancard.BaseView;
import com.jinjerkeihi.scancard.api.Presenter;
import com.jinjerkeihi.scancard.api.core.ApiCallback;
import com.jinjerkeihi.scancard.api.core.ApiClient;
import com.jinjerkeihi.scancard.api.response.ListICCardResponse;
import com.jinjerkeihi.scancard.api.response.SendCardResponse;

import java.util.ArrayList;

/**
 * XUAN_THE on 5/15/2018.
 */

public class ResultICCardPresenter extends Presenter<ResultICCardPresenter.IView> {

    public interface IView extends BaseView {

        void onSendDataSuccess(String messageSuccess);

        void onSendDataFailed(String messageError);

        void onRetrofitError();
    }

    public void sendDataICCard(int userId, int transportationExpenseId, ArrayList<Trip> trips) {

        final ResultICCardPresenter.IView iView = view();
        iView.onShowLoading();
        ApiClient.getService().sendDataICCard(userId, transportationExpenseId, new Gson().toJson(trips)).enqueue(new ApiCallback<SendCardResponse>() {
            @Override
            public void onResponse(SendCardResponse response) {
                iView.onHideLoading();
                if (response != null) {
                    if (response.code == 0) {
                        iView.onSendDataSuccess(response.message);
                    } else {
                        iView.onSendDataFailed(response.error);
                    }

                } else {
                    iView.onRetrofitError();
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                iView.onHideLoading();
                iView.onRetrofitError();
            }
        });

    }

    public void getListICCard() {
        final ResultICCardPresenter.IView iView = view();
        iView.onShowLoading();
        ApiClient.getService().getListICCard().enqueue(new ApiCallback<ListICCardResponse>() {
            @Override
            public void onResponse(ListICCardResponse response) {
                iView.onHideLoading();
                if (response != null) {
                    if (response.code == 0) {
                        iView.onSendDataSuccess(response.message);
                    } else {
                        iView.onSendDataFailed(response.error);
                    }

                } else {
                    iView.onRetrofitError();
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                iView.onHideLoading();
                iView.onRetrofitError();
            }
        });

    }


}
