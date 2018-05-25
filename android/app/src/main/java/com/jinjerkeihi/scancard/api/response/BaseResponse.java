package com.jinjerkeihi.scancard.api.response;

import com.google.gson.annotations.Expose;

/**
 * Description
 *
 * @author Pika Long.
 */
public class BaseResponse {

    @Expose
    public int code;
    @Expose
    public String error;
    @Expose
    public String message;


}
