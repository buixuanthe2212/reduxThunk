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
import android.widget.LinearLayout;

import com.jinjerkeihi.R;

import java.util.Objects;

/**
 * XUAN_THE on 5/8/2018.
 */

public class ScanSuccessDialog extends Dialog {
    private Context mContext;
    private Button mBtnCancel;

    public ScanSuccessDialog(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        setContentView(R.layout.dialog_scan_success);
        findViewById();
        initData();
        registerEvent();
    }

    private void findViewById() {
        mBtnCancel = findViewById(R.id.btn_cancel);
    }

    private void initData() {
    }

    private void registerEvent() {
        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

}
