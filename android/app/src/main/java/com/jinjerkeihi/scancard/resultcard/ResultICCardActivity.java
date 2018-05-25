package com.jinjerkeihi.scancard.resultcard;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jinjerkeihi.R;
import com.jinjerkeihi.nfcfelica.transit.TransitData;
import com.jinjerkeihi.nfcfelica.transit.Trip;
import com.jinjerkeihi.nfcfelica.util.Utils;
import com.jinjerkeihi.scancard.adapter.ResultAdapter;
import com.jinjerkeihi.scancard.api.core.ApiClient;
import com.jinjerkeihi.scancard.api.core.ApiConfig;
import com.jinjerkeihi.scancard.dialog.LoadingDialog;
import com.jinjerkeihi.scancard.dialog.ScanSuccessDialog;
import com.jinjerkeihi.scancard.util.StringUtil;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * XuanThe on 3/21/2018.
 */

public class ResultICCardActivity extends AppCompatActivity implements ResultICCardPresenter.IView, ResultAdapter.IListener {

    private TextView mTvTitle, mTvMessageNoData;
    private ImageView mIvBack;
    private RecyclerView mRecyclerView;
    private Button mBtnSubmit;
    private LinearLayout mLnData;
    private TransitData mTransitData;
    private String mIdCard;
    private LoadingDialog mLoadingDialog;
    private ResultICCardPresenter mPresenter;
    private ArrayList<Trip> mTripsSelect = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_result);
        createService();
        mTransitData = getIntent().getExtras().getParcelable("DATA");
        mIdCard = getIntent().getStringExtra("ID");
        findViewByid();
        initData();
        registerEvent();
        mLoadingDialog = new LoadingDialog(this);
        mTvTitle.setText(getString(R.string.title_card_result));

        new ScanSuccessDialog(ResultICCardActivity.this).show();
        mPresenter = new ResultICCardPresenter();
        mPresenter.bindView(this);

        mPresenter.getListICCard();

    }

    @Override
    public void onHideLoading() {
        mLoadingDialog.hide();
    }

    @Override
    public void onShowLoading() {
        mLoadingDialog.show();
    }

    @Override
    public void onSendDataFailed(String messageError) {
        mLoadingDialog.hide();
        showAertMessage(messageError);
        Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRetrofitError() {
        showAertMessage(getString(R.string.error_try_again));
    }

    @Override
    public void onSendDataSuccess(String messageSuccess) {
        mLoadingDialog.hide();
        showAertMessage(messageSuccess);
        Toast.makeText(this, "success", Toast.LENGTH_SHORT).show();

    }

    private void findViewByid() {
        mIvBack = findViewById(R.id.ivBack);
        mTvTitle = findViewById(R.id.tvTitle);
        mTvMessageNoData = findViewById(R.id.tv_message_no_data);
        mRecyclerView = findViewById(R.id.recycler_result_card);
        mBtnSubmit = findViewById(R.id.btn_submit);
        mLnData = findViewById(R.id.ln_data);
    }

    private void initData() {
        if (mTransitData == null) {
            mTvMessageNoData.setVisibility(View.VISIBLE);
            mLnData.setVisibility(View.GONE);
            mTvMessageNoData.setText(getString(R.string.no_data));
        } else {
            mTvMessageNoData.setVisibility(View.GONE);
            mLnData.setVisibility(View.VISIBLE);
            Trip[] mTrips = mTransitData.getTrips();

            try {
                for (Trip trip : mTrips) {
                    Gson gson = new Gson();
                    String jsonTrip = gson.toJson(trip);
                    JSONObject jsonObject = new JSONObject(jsonTrip);

                    String railEntranceLineCode = jsonObject.getString("mRailEntranceLineCode");
                    String railEntranceStationCode = jsonObject.getString("mRailEntranceStationCode");
                    String railExitLineCode = jsonObject.getString("mRailExitLineCode");
                    String railExitStationCode = jsonObject.getString("mRailExitStationCode");
                    String balance = jsonObject.getString("mBalance");

                    Calendar calendarStartTimestamp = trip.getStartTimestamp();
                    calendarStartTimestamp.set(Calendar.SECOND, 0);

                    switch (trip.getMode()) {
                        case BUS:
                            trip.setTypeTrip("0");
                            break;

                        case TRAIN:
                            trip.setTypeTrip("1");
                            break;

                        case TRAM:
                            trip.setTypeTrip("2");
                            break;

                        case METRO:
                            trip.setTypeTrip("3");
                            break;

                        case FERRY:
                            trip.setTypeTrip("4");
                            break;

                        case TICKET_MACHINE:
                            trip.setTypeTrip("5");
                            break;

                        case VENDING_MACHINE:
                            trip.setTypeTrip("6");
                            break;

                        case POS:
                            trip.setTypeTrip("7");
                            break;

                        case BANNED:
                            trip.setTypeTrip("8");
                            break;

                        default:
                            break;
                    }

                    String contentId = mIdCard + "-" + Utils.isoDateTimeFormat((GregorianCalendar) calendarStartTimestamp) + "-" + trip.getTypeTrip() + "-" + railEntranceLineCode + "-" + railEntranceStationCode + "-" + railExitLineCode + "-" + railExitStationCode + "-" + trip.getFare() + "-" + balance;
                    trip.setTripId(contentId);
                    trip.setTripIdMd5(StringUtil.md5(contentId));

                    mTripsSelect.add(trip);
                }

            } catch (Exception ignored) {
            }
            ResultAdapter mAdapter = new ResultAdapter(this, mTrips, mTransitData, this);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    @Override
    public void onSelectItem(Trip trip, boolean isSelect) {
        if (isSelect) {
            mTripsSelect.add(trip);
        } else {
            mTripsSelect.remove(trip);
        }
    }

    private void registerEvent() {
        mIvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mBtnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendDataICCard();
            }
        });
    }

    private void sendDataICCard() {
        if (StringUtil.isNetworkAvailable(this)) {
            if (mTripsSelect.size() > 0)
                mPresenter.sendDataICCard(1, 1, mTripsSelect);
        } else {
            Toast.makeText(this, getString(R.string.check_network), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * The method is used to create service
     */
    private void createService() {
        ApiConfig apiConfig = new ApiConfig();
        apiConfig.context = getApplicationContext();
        apiConfig.baseUrl = "https://api.keihi-dev-web.integ.jinjer.biz";
        ApiClient.getInstance().init(apiConfig);
    }

    private void showAertMessage(String message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                })
                .show();
    }

}
