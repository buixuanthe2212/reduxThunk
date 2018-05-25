package com.jinjerkeihi.scancard.api;

import com.jinjerkeihi.scancard.api.core.ApiCallback;
import com.jinjerkeihi.scancard.api.response.SendCardResponse;

/**
 * Use to request data from server.
 *
 * @author Pika
 */
public final class ApiRequest {

    /**
     * Register user information to get token
     *
     * @param deviceId   DeviceId
     * @param callback   Callback
     */
    public static void sendDataICCard(String deviceId,  ApiCallback<SendCardResponse> callback) {
//        ApiClient.getService().register(deviceId, deviceName, callback);
    }

}
