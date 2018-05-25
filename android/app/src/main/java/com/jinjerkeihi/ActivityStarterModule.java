package com.jinjerkeihi;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.jinjerkeihi.scancard.ScanICCardActivity;
import com.jinjerkeihi.scancard.SupportedCardsActivity;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class ActivityStarterModule extends ReactContextBaseJavaModule {
    private static DeviceEventManagerModule.RCTDeviceEventEmitter eventEmitter = null;

    ActivityStarterModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public void initialize() {
        super.initialize();
        eventEmitter = getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
    }

    @Override
    public String getName() {
        return "ActivityStarter";
    }

    @ReactMethod
    void navigateToExample(String token) {
        ReactApplicationContext context = getReactApplicationContext();
        Intent intent = new Intent(context, ScanICCardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
          intent.putExtra("TOKEN",token);
        context.startActivity(intent);
    }

    @ReactMethod
    void navigateToSupportCard() {
        ReactApplicationContext context = getReactApplicationContext();
        Intent intent = new Intent(context, SupportedCardsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
        context.startActivity(intent);
    }

    @Nullable
    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put("MyEventName", "MyEventValue");
        return constants;
    }

    @ReactMethod
    void dialNumber(@Nonnull String number) {
        Activity activity = getCurrentActivity();
        if (activity != null) {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
            activity.startActivity(intent);
        }
    }

    @ReactMethod
    void getActivityName(@Nonnull Callback callback) {
        Activity activity = getCurrentActivity();
        if (activity != null) {
            callback.invoke("Tuan");
        }
    }

    static void triggerAlert(@Nonnull String message) {
        eventEmitter.emit("MyEventValue", message);
    }
}