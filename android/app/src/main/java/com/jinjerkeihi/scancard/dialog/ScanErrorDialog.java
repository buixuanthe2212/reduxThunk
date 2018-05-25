package com.jinjerkeihi.scancard.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.jinjerkeihi.R;

import java.util.Objects;

/**
 * XUAN_THE on 5/8/2018.
 */

public class ScanErrorDialog extends Dialog {
    private Context mContext;
    private String mMessageError;
    private IListener mIListener;
    private TextView mTvError;
    private Button mBtnCancel, mBtnTryAgain;

    public ScanErrorDialog(@NonNull Context context, String messageError, IListener iListener) {
        super(context);
        this.mContext = context;
        this.mIListener = iListener;
        this.mMessageError = messageError;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        setContentView(R.layout.dialog_scan_error);
        findViewById();
        initData();
        registerEvent();
    }

    private void findViewById() {
        mTvError = findViewById(R.id.tv_error);
        mBtnCancel = findViewById(R.id.btn_cancel);
        mBtnTryAgain = findViewById(R.id.btn_try_again);
    }

    private void initData() {
        if (Objects.equals(mMessageError, "")) {
            mTvError.setVisibility(View.GONE);
        } else {
            mTvError.setVisibility(View.VISIBLE);
            mTvError.setText(mMessageError);
        }

    }

    private void registerEvent() {
        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIListener != null) {
                    mIListener.onCancel();
                    dismiss();
                }
            }
        });

        mBtnTryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    @Override
    public void dismiss() {
        mIListener = null;
        super.dismiss();
    }

    public interface IListener {
        void onCancel();
    }
}
